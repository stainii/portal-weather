package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.locations.types.Location;

import java.util.Optional;

public interface LocationService {

    boolean canProvide(Class<? extends Location> locationType);
    Optional<Location> map(String location, Class<? extends Location> locationType);
    String name();
}
