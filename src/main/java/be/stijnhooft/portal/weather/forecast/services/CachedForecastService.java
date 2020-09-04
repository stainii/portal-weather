package be.stijnhooft.portal.weather.forecast.services;

import be.stijnhooft.portal.weather.DateHelper;
import be.stijnhooft.portal.weather.cache.ForecastCacheKey;
import be.stijnhooft.portal.weather.forecast.Forecast;
import be.stijnhooft.portal.weather.forecast.Interval;
import be.stijnhooft.portal.weather.locations.types.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CachedForecastService implements ForecastService<Location> {

    private final Cache<ForecastCacheKey, Forecast> forecastsCache;
    private final DateHelper dateHelper;

    public CachedForecastService(@Qualifier("forecastsCache") Cache<ForecastCacheKey, Forecast> forecastsCache,
                                 DateHelper dateHelper) {
        this.forecastsCache = forecastsCache;
        this.dateHelper = dateHelper;
    }

    @Override
    public Class<Location> supportedLocationType() {
        return Location.class;
    }

    @Override
    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        Collection<LocalDate> dates = dateHelper.intervalsToDates(intervals);

        return dates.stream()
                .map(date -> new ForecastCacheKey(location, date))
                .map(forecastsCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    public void clear() {
        forecastsCache.clear();
    }

    public void addToCacheIfNotPresent(Location location, Collection<Forecast> forecasts) {
        forecasts.forEach(forecast -> addToCacheIfNotPresent(location, forecast.getDate(), forecast));
    }

    public void addToCacheIfNotPresent(Location location, LocalDate date, Forecast forecast) {
        final String sourceSuffix = " (cached)";
        forecastsCache.putIfAbsent(new ForecastCacheKey(location, date), forecast.withSource(forecast.getSource().concat(sourceSuffix)));
    }
}
