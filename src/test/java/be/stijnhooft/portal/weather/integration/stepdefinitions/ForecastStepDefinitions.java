package be.stijnhooft.portal.weather.integration.stepdefinitions;

import be.stijnhooft.portal.weather.DateHelper;
import be.stijnhooft.portal.weather.PortalWeatherApplication;
import be.stijnhooft.portal.weather.WeatherFacade;
import be.stijnhooft.portal.weather.cache.LocationCacheValue;
import be.stijnhooft.portal.weather.dtos.ForecastRequest;
import be.stijnhooft.portal.weather.forecasts.Forecast;
import be.stijnhooft.portal.weather.forecasts.Interval;
import be.stijnhooft.portal.weather.forecasts.services.CachedForecastService;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.integration.parameters.ForecastResultTable;
import be.stijnhooft.portal.weather.locations.services.CachedLocationService;
import be.stijnhooft.portal.weather.locations.services.LocationService;
import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.MultiValueMap;
import org.jbehave.core.annotations.*;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
@Component
@Slf4j
@SpringBootTest(classes = PortalWeatherApplication.class)
public class ForecastStepDefinitions {

    @Autowired
    private WeatherFacade weatherFacade;

    @Autowired
    private CachedForecastService cachedForecastService;

    @Autowired
    private CachedLocationService cachedLocationService;

    @Autowired
    private DateHelper dateHelper;

    private Map<String, ForecastService<? extends Location>> forecastServices = new HashMap<>();
    private Map<String, LocationService> locationServicesByName = new HashMap<>();
    private Map<Class, LocationService> locationServicesByLocationType = new HashMap<>();

    private MultiValueMap locationsThatCannotBeMappedByLocationServices;
    private MultiValueMap locationsAndDatesForWhichAForecastServiceCannotProvideAForecast;
    private Map<String, List<LocationCacheValue>> mockedLocations = new HashMap<>();

    private ForecastRequest lastForecastRequest = null;
    private Collection<Forecast> lastForecastResults = new ArrayList<>();

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario() {
        forecastServices = new HashMap<>();
        locationServicesByName = new HashMap<>();
        locationServicesByLocationType = new HashMap<>();
        locationsAndDatesForWhichAForecastServiceCannotProvideAForecast = new MultiValueMap();
        locationsThatCannotBeMappedByLocationServices = new MultiValueMap();
        lastForecastRequest = null;
        lastForecastResults = new ArrayList<>();
        mockedLocations = new HashMap<>();

        weatherFacade.setForecastServices(new ArrayList<>(List.of(cachedForecastService)));
        weatherFacade.setLocationServices(new ArrayList<>(List.of(cachedLocationService)));

        cachedForecastService.clear();
        cachedLocationService.clear();
    }

    @Given("I have a forecast service ${name} which expects location type ${locationType} with order ${preference}")
    public void given_I_have_a_forecast_service_which_expects_location_type_locationType_with_order(String name, String locationType, int order) {
        // create forecast service mock
        ForecastService forecastService = mock(ForecastService.class);

        // configure
        Class locationTypeClass = createLocationTypeFor(locationType);
        when(forecastService.supportedLocationType()).thenReturn(locationTypeClass);
        when(forecastService.order()).thenReturn(order);
        when(forecastService.enabled()).thenReturn(true);
        when(forecastService.name()).thenReturn(name);

        // register
        forecastServices.put(name, forecastService);
        weatherFacade.registerForecastService(forecastService);
    }

    @Given("I have a location service ${name} that provides location type ${locationType}")
    public void given_I_have_a_location_service_that_provides_location_type_locationType(String name, String locationType) {
        // create forecast service mock
        LocationService locationService = mock(LocationService.class);

        // configure
        Class locationTypeClass = createLocationTypeFor(locationType);
        when(locationService.canProvide(locationTypeClass)).thenReturn(true);
        when(locationService.name()).thenReturn(name);

        // register
        locationServicesByName.put(name, locationService);
        locationServicesByLocationType.put(locationTypeClass, locationService);
        weatherFacade.registerLocationService(locationService);

    }

    @Given("${location} cannot be mapped by location service ${locationServiceName}")
    public void given_location_cannot_be_mapped_by_location_service_locationServiceName(String location, String locationServiceName) {
        locationsThatCannotBeMappedByLocationServices.put(location, locationServiceName);
    }

    @Given("I have queried a forecast for ${locationUserInput} between ${startDateTime} and ${endDateTime} and I got a forecast result on ${dateOfResult}")
    public void given_I_have_queried_a_forecast_for_location_between_startDateTime_endDateTime(String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDate dateOfResult) {
        if (forecastServices.isEmpty() || locationServicesByName.isEmpty()) {
            throw new UnsupportedOperationException("Test setup failure: please define a forecast and corresponding location service before defining you have a queried for a forecast in the past.");
        }

        // create query and forecast result
        ForecastService forecastService = forecastServices.values().iterator().next();

        Location locationInstance = mockLocation(locationUserInput, forecastService);

        Forecast forecast = Forecast.builder()
                .location(locationUserInput)
                .date(dateOfResult)
                .source(forecastService.name())
                .build();

        when(forecastService.query(locationInstance, List.of(new Interval(startDateTime, endDateTime))))
                .thenReturn(List.of(forecast));

        // put the forecast result in cache
        weatherFacade.retrieveForecastsFor(locationUserInput, startDateTime, endDateTime);
    }

    @Given("no forecast for ${location} at ${date} can be provided by forecast service ${forecastServiceName}")
    public void given_no_forecast_for_location_at_date_can_be_provided_by_forecast_service_forecastServiceName(String location, LocalDate date, String forecastServiceName) {
        locationsAndDatesForWhichAForecastServiceCannotProvideAForecast.put(forecastServiceName, ForecastResultTable.builder()
                .location(location)
                .date(date)
                .source(forecastServiceName)
                .build());
    }

    @When("I query the forecast for ${locationUserInput} between ${startDateTime} and ${endDateTime}")
    public void when_i_query_the_forecast_for_location_between_startDateType_and_endDateTime(String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<LocalDate> days = dateHelper.getDaysBetween(startDateTime, endDateTime);

        // prepare location and forecast services for a possible query
        for (Map.Entry<String, ForecastService<? extends Location>> forecastServiceEntry : forecastServices.entrySet()) {
            String forecastServiceName = forecastServiceEntry.getKey();
            ForecastService forecastService = forecastServiceEntry.getValue();

            Location locationInstance = mockLocation(locationUserInput, forecastService);

            when(forecastService.query(eq(locationInstance), any()))
                    .thenAnswer(invocation -> {
                        Collection<Interval> intervals = invocation.getArgument(1, Collection.class);
                        return determineForecastsThatShouldBeReturned(forecastServiceName, locationUserInput, dateHelper.intervalsToDates(intervals));
                    });
        }

        // all preparations are done, reset the invocations of the mocks
        Mockito.clearInvocations(forecastServices.values().toArray());
        Mockito.clearInvocations(locationServicesByName.values().toArray());

        // execute request and keep results
        lastForecastResults = weatherFacade.retrieveForecastsFor(locationUserInput, startDateTime, endDateTime);

        // keep last request
        lastForecastRequest = ForecastRequest.builder()
                .location(locationUserInput)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .build();

    }

    @Then("I get forecast results: ${forecastResults}")
    public void then_i_get_forecast_results(List<ForecastResultTable> forecastResultTables) {
        assertThat(lastForecastResults).hasSize(forecastResultTables.size());

        for (ForecastResultTable forecastResultTable : forecastResultTables) {
            assertThat(lastForecastResults.contains(Forecast.builder()
                    .location(forecastResultTable.getLocation())
                    .date(forecastResultTable.getDate())
                    .source(forecastResultTable.getSource())
                    .build()));
        }
    }

    @Then("I get no forecast results")
    public void then_I_get_no_forecast_results() {
        assertThat(lastForecastResults.isEmpty());
    }

    @Then("forecast service ${name} has not been tried")
    public void then_forecast_service_name_has_not_been_tried(String name) {
        ForecastService forecastService = forecastServices.get(name);
        verify(forecastService, never()).query(any(), any());
    }

    @Then("location service ${name} has not been tried")
    public void then_location_service_name_has_not_been_tried(String name) {
        LocationService locationService = locationServicesByName.get(name);
        verify(locationService, never()).map(any(), any());
    }

    @Then("forecast service ${name} has been tried for ${locationUserInput} between ${startDateTime} and ${endDateTime}")
    public void then_forecast_service_name_has_been_tried_for_location_at_date(String name, String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ForecastService forecastService = forecastServices.get(name);
        var locationInstance = mockLocation(locationUserInput, forecastService);
        verify(forecastService).query(locationInstance, List.of(new Interval(startDateTime, endDateTime)));
    }

    @Then("location service ${name} has been tried for ${location}")
    public void then_location_service_name_has_been_tried_for_location(String name, String location) {
        LocationService locationService = locationServicesByName.get(name);
        verify(locationService, atLeastOnce()).map(eq(location), any());
    }

    @NotNull
    @SneakyThrows
    private Class createLocationTypeFor(String locationType) {
        return Class.forName("be.stijnhooft.portal.weather.locations.types." + locationType);
    }

    @NotNull
    private List<Forecast> determineForecastsThatShouldBeReturned(String forecastServiceName, String location, Collection<LocalDate> days) {
        List<Forecast> forecasts = new ArrayList<>();

        for (LocalDate day : days) {
            var forecastResultsThatShouldNotBeReturned = (Collection<ForecastResultTable>) locationsAndDatesForWhichAForecastServiceCannotProvideAForecast.getCollection(forecastServiceName);
            boolean forecastCanBeReturned = forecastResultsThatShouldNotBeReturned == null ||
                    forecastResultsThatShouldNotBeReturned.stream()
                            .filter(forecastResult -> forecastResult.getLocation().equals(location))
                            .noneMatch(forecastResult -> forecastResult.getDate().isEqual(day));

            if (forecastCanBeReturned) {
                forecasts.add(Forecast.builder()
                        .location(location)
                        .date(day)
                        .source(forecastServiceName)
                        .build());
            }
        }
        return forecasts;
    }

    @NotNull
    private Location mockLocation(String locationUserInput, ForecastService forecastService) {
        Class<? extends Location> locationType = forecastService.supportedLocationType();

        // retrieve mock from cache if possible, else create a mock and put it in cache
        Location locationMock;
        Optional<Location> cachedLocationMock = mockedLocations.getOrDefault(locationUserInput, new ArrayList<>()).stream()
                .filter(locationCacheValue -> locationCacheValue.getLocationType().isAssignableFrom(locationType))
                .map(LocationCacheValue::getLocation)
                .findFirst();

        if (cachedLocationMock.isPresent()) {
            locationMock = cachedLocationMock.get();
        } else {
            locationMock = mock(locationType);
            var locationCacheValues = mockedLocations.getOrDefault(locationUserInput, new ArrayList<>());
            locationCacheValues.add(new LocationCacheValue(locationType, locationMock));
            mockedLocations.put(locationUserInput, locationCacheValues);
        }

        // ok, so now we have a mock for the location.
        // configure location services to return the mock, but only if the test did not declare that the location service should not find the location
        LocationService locationService = locationServicesByLocationType.get(locationType);
        if ((locationsThatCannotBeMappedByLocationServices.containsKey(locationUserInput)
                && locationsThatCannotBeMappedByLocationServices.getCollection(locationUserInput).contains(locationService.name()))) {
            when(locationService.map(locationUserInput, locationType)).thenReturn(Optional.empty());
        } else {
            when(locationService.map(locationUserInput, locationType)).thenReturn(Optional.of(locationMock));
        }

        return locationMock;
    }

}
