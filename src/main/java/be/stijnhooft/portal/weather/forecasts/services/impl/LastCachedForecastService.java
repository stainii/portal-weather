package be.stijnhooft.portal.weather.forecasts.services.impl;

import be.stijnhooft.portal.weather.DateHelper;
import be.stijnhooft.portal.weather.cache.ForecastCacheKey;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.types.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LastCachedForecastService implements ForecastService<Location> {

    private final Cache<ForecastCacheKey, Forecast> forecastsCache;
    private final DateHelper dateHelper;

    public LastCachedForecastService(@Qualifier("forecastsCache") Cache<ForecastCacheKey, Forecast> forecastsCache,
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
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

}
