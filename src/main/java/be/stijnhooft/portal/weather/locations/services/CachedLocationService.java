package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.cache.LocationCacheValue;
import be.stijnhooft.portal.weather.cache.LocationCacheValues;
import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class CachedLocationService implements LocationService {

    private final Cache<String, LocationCacheValues> locationsCache;

    public CachedLocationService(@Qualifier("locationsCache") Cache<String, LocationCacheValues> locationsCache) {
        this.locationsCache = locationsCache;
    }

    @Override
    public boolean canProvide(Class<? extends Location> locationType) {
        var iterator = locationsCache.iterator();
        boolean canProvide = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .flatMap(cacheEntry -> cacheEntry.getValue().stream())
                .anyMatch(locationCacheValue -> locationType.isAssignableFrom(locationCacheValue.getLocationType()));

        if (!canProvide) {
            log.info("CachedLocationService does not have any locations in cache with type {}", locationType);
        }

        return canProvide;
    }

    @Override
    public Collection<Location> map(String locationUserInput, Class<? extends Location> locationType) {
        var locationCacheValues = locationsCache.get(locationUserInput);
        var locations = locationCacheValues.stream()
                .filter(locationCacheValue -> locationType.isAssignableFrom(locationCacheValue.getLocationType()))
                .map(LocationCacheValue::getLocation)
                .collect(Collectors.toList());
        log.info("Found {} cached locations for type {} and user input {}", locations.size(), locationType, locationUserInput);

        return locations;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

}
