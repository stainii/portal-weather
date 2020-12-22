package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.cache.CacheService;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.locations.Location;
import be.stijnhooft.portal.weather.locations.LocationService;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class WeatherFacade {

    @Setter
    private Collection<ForecastService> forecastServices;

    private final CacheService cacheService;
    private final DateHelper dateHelper;
    private final LocationService locationService;

    @NotNull
    public Collection<Forecast> retrieveForecastsFor(@NotNull String locationUserInput, @NotNull LocalDateTime startDateTime, @NotNull LocalDateTime endDateTime) {
        List<Forecast> forecasts = new ArrayList<>();

        var location = findAndCacheLocation(locationUserInput).orElse(null);
        if (location == null) {
            return forecasts;
        }

        Iterator<ForecastService> forecastServiceIterator = sortedAndEnabledForecastServicesIterator();
        Collection<LocalDate> remainingDays = dateHelper.getDaysBetween(startDateTime, endDateTime);
        while (!remainingDays.isEmpty() && forecastServiceIterator.hasNext()) {
            ForecastService forecastService = forecastServiceIterator.next();

            if (!remainingDays.isEmpty()) {
                Collection<Forecast> foundForecasts = findAndCacheForecasts(forecastService, remainingDays, location);
                forecasts.addAll(foundForecasts);

                remainingDays = dateHelper.determineMissingDays(forecasts, remainingDays);
                log.info("At this moment we've found {} out of {} forecasts for {}. Still looking for {}", forecasts.size(), forecasts.size() + remainingDays.size(), locationUserInput, remainingDays);
            }
        }

        if (!remainingDays.isEmpty()) {
            log.info("Found no forecasts for {} at {}", locationUserInput, remainingDays);
        }

        return forecasts;
    }

    @NotNull
    private Optional<Location> findAndCacheLocation(String locationUserInput) {
        return cacheService.findLocation(locationUserInput)
                .or(() -> {
                    var locationOptional = locationService.map(locationUserInput);
                    locationOptional.ifPresent(location -> cacheService.addToCacheIfNotPresent(locationUserInput, location));
                    return locationOptional;
                });
    }

    @NotNull
    private Collection<Forecast> findAndCacheForecasts(ForecastService forecastService, Collection<LocalDate> remainingDays, Location location) {
        Collection<Interval> intervals = dateHelper.determineIntervals(remainingDays);

        try {
            var foundForecasts = forecastService.query(location, intervals);

            // Forecast services are allowed to return more days than requested, if that helps them make less (though more course-grained) requests.
            // We cache these extra results, but don't send them back to the user at this point
            cacheService.addToCacheIfNotPresent(location, foundForecasts);
            return foundForecasts.stream()
                    .filter(forecast -> remainingDays.contains(forecast.getDate()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception during usage of {}", forecastService.name(), e);
            return new ArrayList<>();
        }
    }

    public void registerForecastService(ForecastService forecastService) {
        forecastServices.add(forecastService);
    }

    private Iterator<ForecastService> sortedAndEnabledForecastServicesIterator() {
        return forecastServices.stream()
                .filter(ForecastService::enabled)
                .sorted(Comparator.comparingInt(ForecastService::order))
                .iterator();
    }

}
