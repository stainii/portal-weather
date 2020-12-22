package be.stijnhooft.portal.weather.integration.stubs;

import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.integration.parameters.ForecastResultTable;
import be.stijnhooft.portal.weather.locations.Location;
import lombok.Value;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FakeForecastService implements ForecastService {

    private final DateHelper dateHelper;
    private final Clock clock;

    private final List<ForecastResultTable> ignoredForecasts;
    private final String name;
    private final int order;
    private final List<ForecastQuery> queries;

    public FakeForecastService(String name, int order, DateHelper dateHelper, Clock clock) {
        this.name = name;
        this.order = order;
        this.clock = clock;
        this.ignoredForecasts = new ArrayList<>();
        this.queries = new ArrayList<>();
        this.dateHelper = dateHelper;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        String locationUserInput = location.getUserInput();

        intervals.forEach(interval -> queries.add(new ForecastQuery(locationUserInput, interval.getStartDateTime(), interval.getEndDateTime())));

        return dateHelper.intervalsToDates(intervals)
                .stream()
                .filter(date -> ignoredForecasts.stream().noneMatch(table -> table.getDate().isEqual(date) && table.getLocation().equals(locationUserInput)))
                .map(date -> Forecast.builder()
                        .location(locationUserInput)
                        .date(date)
                        .source(name)
                        .createdAt(LocalDateTime.now(clock))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return name;
    }

    public void doNotProvideFor(ForecastResultTable forecastResultTable) {
        this.ignoredForecasts.add(forecastResultTable);
    }

    public boolean hasNeverBeenQueried() {
        return queries.isEmpty();
    }

    public boolean hasBeenQueriedFor(String location, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return queries.contains(new ForecastQuery(location, startDateTime, endDateTime));
    }

    public void resetQueries() {
        queries.clear();
    }

    @Value
    private static class ForecastQuery {
        String location;
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
    }

    public String toString() {
        return "ForecastService " + name;
    }
}
