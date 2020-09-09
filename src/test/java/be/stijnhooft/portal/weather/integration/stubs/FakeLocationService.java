package be.stijnhooft.portal.weather.integration.stubs;

import be.stijnhooft.portal.weather.locations.services.LocationService;
import be.stijnhooft.portal.weather.locations.types.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FakeLocationService implements LocationService {

    private final Class<? extends Location> canProvide;
    private final List<String> ignoredLocations;
    private final String name;
    private final List<String> queriedLocations;

    public FakeLocationService(String name, Class<Location> canProvide) {
        this.name = name;
        this.canProvide = canProvide;
        this.ignoredLocations = new ArrayList<>();
        this.queriedLocations = new ArrayList<>();
    }

    @Override
    public boolean canProvide(Class<? extends Location> locationType) {
        return locationType.isAssignableFrom(canProvide);
    }

    @Override
    public Collection<Location> map(String locationUserInput, Class<? extends Location> locationType) {
        queriedLocations.add(locationUserInput);
        if (ignoredLocations.contains(locationUserInput)) {
            return new ArrayList<>();
        } else {
            return List.of(new FakeLocation(locationUserInput));
        }
    }

    @Override
    public String name() {
        return name;
    }

    public void doNotProvideFor(String location) {
        this.ignoredLocations.add(location);
    }

    public boolean hasNeverBeenQueried() {
        return queriedLocations.isEmpty();
    }

    public boolean hasBeenQueriedFor(String location) {
        return queriedLocations.contains(location);
    }

    public void resetQueries() {
        queriedLocations.clear();
    }
}
