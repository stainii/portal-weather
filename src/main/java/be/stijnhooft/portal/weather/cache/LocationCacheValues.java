package be.stijnhooft.portal.weather.cache;

import lombok.NonNull;

import java.util.ArrayList;

/**
 * ArrayList of LocationCacheValues.
 * Needed to create this because I could not ask EHCache to create a cache with a value of type
 * Collection<LocationCacheValue>, due to Java's generic's limitation.
 */
public class LocationCacheValues extends ArrayList<LocationCacheValue> {
    public void addIfAbsent(@NonNull LocationCacheValue locationCacheValue) {
        if (!this.contains(locationCacheValue)) {
            this.add(locationCacheValue);
        }
    }
}
