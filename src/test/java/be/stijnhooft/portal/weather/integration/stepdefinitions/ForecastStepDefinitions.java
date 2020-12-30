package be.stijnhooft.portal.weather.integration.stepdefinitions;

import be.stijnhooft.portal.model.weather.Forecast;
import be.stijnhooft.portal.model.weather.ForecastRequest;
import be.stijnhooft.portal.weather.PortalWeatherApplication;
import be.stijnhooft.portal.weather.WeatherFacade;
import be.stijnhooft.portal.weather.cache.CacheService;
import be.stijnhooft.portal.weather.forecasts.services.impl.FirstCachedForecastService;
import be.stijnhooft.portal.weather.forecasts.services.impl.LastCachedForecastService;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.integration.IntegrationTestConfiguration;
import be.stijnhooft.portal.weather.integration.parameters.ForecastResultTable;
import be.stijnhooft.portal.weather.integration.stubs.AdaptableClock;
import be.stijnhooft.portal.weather.integration.stubs.FakeForecastService;
import be.stijnhooft.portal.weather.integration.stubs.FakeLocationService;
import lombok.extern.slf4j.Slf4j;
import org.jbehave.core.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unused"})
@Component
@Slf4j
@SpringBootTest(classes = PortalWeatherApplication.class)
@Import({IntegrationTestConfiguration.class})
public class ForecastStepDefinitions {

    @Autowired
    private WeatherFacade weatherFacade;

    @Autowired
    private FirstCachedForecastService firstCachedForecastService;

    @Autowired
    private LastCachedForecastService lastCachedForecastService;

    @Autowired
    private FakeLocationService locationService;

    @Autowired
    private DateHelper dateHelper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private AdaptableClock clock;

    private Map<String, FakeForecastService> forecastServices;
    private ForecastRequest lastForecastRequest;
    private Collection<Forecast> lastForecastResults;

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario() {
        forecastServices = new HashMap<>();
        lastForecastRequest = null;
        lastForecastResults = new ArrayList<>();

        weatherFacade.setForecastServices(new ArrayList<>(List.of(firstCachedForecastService, lastCachedForecastService)));
        cacheService.clear();
    }

    @Given("I have a forecast service ${name} with order ${preference}")
    public void given_I_have_a_forecast_service_with_order(String name, int order) {
        FakeForecastService forecastService = new FakeForecastService(name, order, dateHelper, clock);
        forecastServices.put(name, forecastService);
        weatherFacade.registerForecastService(forecastService);
    }

    @Given("${location} cannot be mapped")
    public void given_location_cannot_be_mapped(String location) {
        locationService.doNotMap(location);
    }

    @Given("${minutesAgo} minutes ago I have queried a forecast for ${location} between ${startDateTime} and ${endDateTime} and I got a forecast result on ${dateOfResult}")
    public void given_minute_ago_I_have_queried_a_forecast_for_location_between_startDateTime_endDateTime(int minutesAgo, String location, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDate dateOfResult) {
        given_I_have_queried_a_forecast_for_location_between_startDateTime_endDateTime(minutesAgo, MINUTES, location, startDateTime, endDateTime, dateOfResult);
    }

    @Given("${hoursAgo} hours ago I have queried a forecast for ${locationUserInput} between ${startDateTime} and ${endDateTime} and I got a forecast result on ${dateOfResult}")
    public void given_hours_ago_I_have_queried_a_forecast_for_location_between_startDateTime_endDateTime(int hoursAgo, String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDate dateOfResult) {
        given_I_have_queried_a_forecast_for_location_between_startDateTime_endDateTime(hoursAgo, HOURS, locationUserInput, startDateTime, endDateTime, dateOfResult);
    }

    private void given_I_have_queried_a_forecast_for_location_between_startDateTime_endDateTime(int timeAmount, ChronoUnit timeUnit, String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDate dateOfResult) {
        if (forecastServices.isEmpty()) {
            throw new UnsupportedOperationException("Test setup failure: please define a forecast service before defining you have a queried for a forecast in the past.");
        }

        // put the forecast result in cache
        clock.tickBack(timeAmount, timeUnit);
        weatherFacade.retrieveForecastsFor(locationUserInput, startDateTime, endDateTime);
        clock.tickForward(timeAmount, timeUnit);

        // all preparations are done, delete any queries that have been made earlier
        forecastServices.values().forEach(FakeForecastService::resetQueries);
        locationService.resetQueries();
    }

    @Given("no forecast for ${location} at ${date} can be provided by forecast service ${forecastServiceName}")
    public void given_no_forecast_for_location_at_date_can_be_provided_by_forecast_service_forecastServiceName(String location, LocalDate date, String forecastServiceName) {
        forecastServices.get(forecastServiceName)
                .doNotProvideFor(ForecastResultTable.builder()
                        .location(location)
                        .date(date)
                        .source(forecastServiceName)
                        .build());
    }

    @When("I query the forecast for ${locationUserInput} between ${startDateTime} and ${endDateTime}")
    public void when_i_query_the_forecast_for_location_between_startDateType_and_endDateTime(String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime) {
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
            assertThat(lastForecastResults.stream()
                    .filter(forecast -> forecast.getLocation().equals(forecastResultTable.getLocation()))
                    .filter(forecast -> forecast.getDate().isEqual(forecastResultTable.getDate()))
                    .anyMatch(forecast -> forecast.getSource().equals(forecastResultTable.getSource()))
            ).withFailMessage(String.format("Expecting a forecast with location %s, date %s and source %s. Actual forecasts: %s", forecastResultTable.getLocation(), forecastResultTable.getDate(), forecastResultTable.getSource(), lastForecastResults))
                    .isTrue();

        }
    }

    @Then("I get no forecast results")
    public void then_I_get_no_forecast_results() {
        assertThat(lastForecastResults.isEmpty()).isTrue();
    }

    @Then("forecast service ${name} has not been tried")
    public void then_forecast_service_name_has_not_been_tried(String name) {
        FakeForecastService forecastService = forecastServices.get(name);
        assertThat(forecastService.hasNeverBeenQueried()).isTrue();
    }

    @Then("the location service has not been tried")
    public void then_location_service_name_has_not_been_tried() {
        assertThat(locationService.hasNeverBeenQueried()).isTrue();
    }

    @Then("forecast service ${name} has been tried for ${locationUserInput} between ${startDateTime} and ${endDateTime}")
    public void then_forecast_service_name_has_been_tried_for_location_between(String name, String locationUserInput, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        FakeForecastService forecastService = forecastServices.get(name);
        assertThat(forecastService.hasBeenQueriedFor(locationUserInput, startDateTime, endDateTime)).isTrue();
    }

    @Then("the location service has been tried for ${location}")
    public void then_location_service_name_has_been_tried_for_location(String location) {
        assertThat(locationService.hasBeenQueriedFor(location)).isTrue();
    }

}
