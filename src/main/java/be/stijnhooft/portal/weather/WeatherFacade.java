package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.cache.CacheService;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.locations.services.LocationService;
import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherFacade {

    @Setter
    private Collection<ForecastService<?>> forecastServices;

    @Setter
    private Collection<LocationService> locationServices;

    private final CacheService cacheService;
    private final DateHelper dateHelper;

    public WeatherFacade(List<ForecastService<?>> forecastServices, List<LocationService> locationServices,
                         CacheService cacheService, DateHelper dateHelper) {
        this.forecastServices = forecastServices;
        this.locationServices = locationServices;
        this.cacheService = cacheService;
        this.dateHelper = dateHelper;
    }

    @NotNull
    public Collection<Forecast> retrieveForecastsFor(@NotNull String locationUserInput, @NotNull LocalDateTime startDateTime, @NotNull LocalDateTime endDateTime) {
        List<Forecast> forecasts = new ArrayList<>();

        Collection<LocalDate> remainingDays = dateHelper.getDaysBetween(startDateTime, endDateTime);

        Iterator<ForecastService<?>> forecastServiceIterator = sortedAndEnabledForecastServicesIterator();

        while (!remainingDays.isEmpty() && forecastServiceIterator.hasNext()) {
            ForecastService<?> forecastService = forecastServiceIterator.next();

            Collection<? extends Location> locations = findAndCacheLocation(locationUserInput, forecastService);
            if (locations.isEmpty()) {
                continue;
            }

            for (Location location : locations) {
                if (!remainingDays.isEmpty()) {
                    Collection<Forecast> foundForecasts = findAndCacheForecasts(forecastService, remainingDays, location);
                    forecasts.addAll(foundForecasts);

                    remainingDays = dateHelper.determineMissingDays(forecasts, remainingDays);
                    log.info("At this moment we've found {} out of {} forecasts for {}. Still looking for {}", forecasts.size(), forecasts.size() + remainingDays.size(), locationUserInput, remainingDays);
                }
            }
        }

        if (!remainingDays.isEmpty()) {
            log.info("Found no forecasts for {} at {}", locationUserInput, remainingDays);
        }

        return forecasts;
    }

    @NotNull
    private Collection<? extends Location> findAndCacheLocation(String locationUserInput, ForecastService<?> forecastService) {
        var locationType = forecastService.supportedLocationType();
        var locations = locationServices.stream()
                .filter(locationService -> locationService.canProvide(locationType))
                .map(locationService -> locationService.map(locationUserInput, locationType))
                .findFirst()
                .orElse(new ArrayList<>());
        cacheService.addToCacheIfNotPresent(locationUserInput, locations);
        return locations;
    }

    @NotNull
    private Collection<Forecast> findAndCacheForecasts(ForecastService<?> forecastService, Collection<LocalDate> remainingDays, Location location) {
        Collection<Interval> intervals = dateHelper.determineIntervals(remainingDays);
        var foundForecasts = forecastService.query(location, intervals);

        // Forecast services are allowed to return more days than requested, if that helps them make less (though more course-grained) requests.
        // We cache these extra results, but don't send them back to the user at this point
        // TODO test this behaviour
        cacheService.addToCacheIfNotPresent(location, foundForecasts);
        return foundForecasts.stream()
                .filter(forecast -> remainingDays.contains(forecast.getDate()))
                .collect(Collectors.toList());
    }


    public void registerForecastService(ForecastService<? extends Location> forecastService) {
        forecastServices.add(forecastService);
    }

    public void registerLocationService(LocationService locationService) {
        locationServices.add(locationService);
    }

    private Iterator<ForecastService<?>> sortedAndEnabledForecastServicesIterator() {
        return forecastServices.stream()
                .filter(ForecastService::enabled)
                .sorted(Comparator.comparingInt(ForecastService::order))
                .iterator();
    }

}
