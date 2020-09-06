package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.types.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class CacheService {

    private final Cache<ForecastCacheKey, Forecast> forecastsCache;
    private final Cache<String, LocationCacheValues> locationsCache;

    public CacheService(@Qualifier("forecastsCache") Cache<ForecastCacheKey, Forecast> forecastsCache,
                        @Qualifier("locationsCache") Cache<String, LocationCacheValues> locationsCache) {
        this.forecastsCache = forecastsCache;
        this.locationsCache = locationsCache;
    }

    public void clear() {
        forecastsCache.clear();
        locationsCache.clear();
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

    public void addToCacheIfNotPresent(Location location, Collection<Forecast> forecasts) {
        forecasts.forEach(forecast -> addToCacheIfNotPresent(location, forecast.getDate(), forecast));
    }

    public void addToCacheIfNotPresent(Location location, LocalDate date, Forecast forecast) {
        final String sourceSuffix = " (cached)";
        forecastsCache.putIfAbsent(new ForecastCacheKey(location, date), forecast.withSource(forecast.getSource().concat(sourceSuffix)));
    }

}
