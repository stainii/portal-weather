package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.locations.types.Location;

import java.util.Collection;

public interface LocationService {

    boolean canProvide(Class<? extends Location> locationType);

    /**
     * Map to one or more locations which are assignable to the given location type.
     *
     * Why one or more and not just one? Most location services will map to one specific type only.
     * However, there are more generic location services, which can return multiple locations assignable to a super Location type.
     * An example: the {@link CachedLocationService}. You can ask the CachedLocationService to map "Zottegem" to type {@link Location}.
     * The cache contains both an OpenWeatherMapCityId as a LatitudeLongitude for Zottegem. Both location instances will be returned.
     *
     * @param location the location user input
     * @param locationType class to which the returned locations should be assignable.
     * @return one or more locations which are assignable to the given location type
     */
    Collection<Location> map(String location, Class<? extends Location> locationType);

    String name();
}
