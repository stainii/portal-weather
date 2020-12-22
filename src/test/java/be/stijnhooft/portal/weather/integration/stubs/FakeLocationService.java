package be.stijnhooft.portal.weather.integration.stubs;

import be.stijnhooft.portal.weather.locations.Location;
import be.stijnhooft.portal.weather.locations.LocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeLocationService extends LocationService {

    private final List<String> queries;
    private final List<String> ignoredQueries;

    public FakeLocationService() {
        super(null, null);
        this.queries = new ArrayList<>();
        this.ignoredQueries = new ArrayList<>();
    }

    @Override
    public Optional<Location> map(String locationUserInput) {
        this.queries.add(locationUserInput);

        if (ignoredQueries.contains(locationUserInput)) {
            return Optional.empty();
        }

        return Optional.of(Location.builder()
                .userInput(locationUserInput)
                .latitude("1")
                .longitude("2")
                .build());
    }

    public boolean hasNeverBeenQueried() {
        return queries.isEmpty();
    }

    public boolean hasBeenQueriedFor(String location) {
        return queries.contains(location);
    }

    public void resetQueries() {
        queries.clear();
        ignoredQueries.clear();
    }

    public void doNotMap(String location) {
        this.ignoredQueries.add(location);
    }
}
