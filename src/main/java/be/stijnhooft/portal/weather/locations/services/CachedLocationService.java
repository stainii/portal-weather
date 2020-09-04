package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.cache.LocationCacheValue;
import be.stijnhooft.portal.weather.cache.LocationCacheValues;
import be.stijnhooft.portal.weather.locations.types.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Service
public class CachedLocationService implements LocationService {

    private final Cache<String, LocationCacheValues> locationsCache;

    public CachedLocationService(@Qualifier("locationsCache") Cache<String, LocationCacheValues> locationsCache) {
        this.locationsCache = locationsCache;
    }

    @Override
    public boolean canProvide(Class<? extends Location> locationType) {
        var iterator = locationsCache.iterator();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .flatMap(cacheEntry -> cacheEntry.getValue().stream())
                .anyMatch(locationCacheValue -> locationType.isAssignableFrom(locationCacheValue.getLocationType()));
    }

    @Override
    public Optional<Location> map(String locationUserInput, Class<? extends Location> locationType) {
        var locationCacheValues = locationsCache.get(locationUserInput);
        return locationCacheValues.stream()
                .filter(locationCacheValue -> locationType.isAssignableFrom(locationCacheValue.getLocationType()))
                .map(LocationCacheValue::getLocation)
                .findFirst();
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    public void clear() {
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
}
