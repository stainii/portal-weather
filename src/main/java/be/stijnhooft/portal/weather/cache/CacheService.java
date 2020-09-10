package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.locations.types.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CacheService {

    public static final String SOURCE_SUFFIX = " (cached)";

    private final Cache<ForecastCacheKey, Forecast> forecastsCache;
    private final Cache<String, LocationCacheValues> locationsCache;
    private final DateHelper dateHelper;

    public CacheService(@Qualifier("forecastsCache") Cache<ForecastCacheKey, Forecast> forecastsCache,
                        @Qualifier("locationsCache") Cache<String, LocationCacheValues> locationsCache,
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

    public void addToCacheIfNotPresent(String locationUserInput, Collection<Location> locations) {
        locations.forEach(location -> addToCacheIfNotPresent(locationUserInput, location.getClass(), location));
    }

    public void addToCacheIfNotPresent(String locationUserInput, Class<? extends Location> locationType, Location location) {
        LocationCacheValue locationCacheValue = new LocationCacheValue(locationType, location);

        LocationCacheValues locationCacheValues;
        if (locationsCache.containsKey(locationUserInput)) {
            locationCacheValues = locationsCache.get(locationUserInput);
        } else {
            locationCacheValues = new LocationCacheValues();
        }
        locationCacheValues.addIfAbsent(locationCacheValue);

        locationsCache.put(locationUserInput, locationCacheValues);
    }

    public Collection<Forecast> find(Location location, Collection<Interval> intervals) {
        Collection<LocalDate> dates = dateHelper.intervalsToDates(intervals);

        return dates.stream()
                .map(date -> new ForecastCacheKey(location.getUserInput(), date))
                .map(forecastsCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
