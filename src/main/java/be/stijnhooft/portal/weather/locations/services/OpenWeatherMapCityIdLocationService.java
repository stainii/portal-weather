package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.locations.types.Location;
import be.stijnhooft.portal.weather.locations.types.OpenWeatherMapCityId;
import org.springframework.stereotype.Service;

import java.util.Optional;

// TODO implement
@Service
public class OpenWeatherMapCityIdLocationService implements LocationService {

    @Override
    public boolean canProvide(Class<? extends Location> locationType) {
        return locationType.isAssignableFrom(OpenWeatherMapCityId.class);
    }

    @Override
    public Optional<Location> map(String location, Class<? extends Location> locationType) {
        return Optional.empty();
    }

    @Override
    public String name() {
        return "OpenWeatherMap CityId";
    }
}
