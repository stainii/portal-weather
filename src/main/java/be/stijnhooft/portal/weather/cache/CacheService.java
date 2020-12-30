package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.model.weather.Forecast;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.locations.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CacheService {

    public static final String SOURCE_SUFFIX = " (cached)";

    private final Cache<ForecastCacheKey, Forecast> forecastsCache;
    private final Cache<String, Location> locationsCache;
    private final DateHelper dateHelper;

    public CacheService(@Qualifier("forecastsCache") Cache<ForecastCacheKey, Forecast> forecastsCache,
                        @Qualifier("locationsCache") Cache<String, Location> locationsCache,
                        DateHelper dateHelper) {
        this.forecastsCache = forecastsCache;
        this.locationsCache = locationsCache;
        this.dateHelper = dateHelper;
    }

    public void clear() {
        forecastsCache.clear();
        locationsCache.clear();
    }

    public void addToCacheIfNotPresent(Location location, Collection<Forecast> forecasts) {
        forecasts.forEach(forecast -> addToCacheIfNotPresent(location, forecast.getDate(), forecast));
    }

    public void addToCacheIfNotPresent(Location location, LocalDate date, Forecast forecast) {
        var forecastCacheKey = new ForecastCacheKey(location.getUserInput(), date);
        var adaptedForecast = forecast.withSource(forecast.getSource().concat(SOURCE_SUFFIX));
        forecastsCache.putIfAbsent(forecastCacheKey, adaptedForecast);
    }

    public void addToCacheIfNotPresent(String locationUserInput, Location location) {
        locationsCache.putIfAbsent(locationUserInput, location);
    }

    public Optional<Location> findLocation(String locationUserInput) {
        return Optional.ofNullable(locationsCache.get(locationUserInput));
    }

    public Collection<Forecast> findForecasts(Location location, Collection<Interval> intervals) {
        Collection<LocalDate> dates = dateHelper.intervalsToDates(intervals);

        return dates.stream()
                .map(date -> new ForecastCacheKey(location.getUserInput(), date))
                .map(forecastsCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
