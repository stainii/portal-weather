package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.services.impl.CachedForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.services.CachedLocationService;
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

    private final CachedForecastService cachedForecastService;
    private final CachedLocationService cachedLocationService;
    private final DateHelper dateHelper;

    public WeatherFacade(List<ForecastService<?>> forecastServices, List<LocationService> locationServices,
                         CachedForecastService cachedForecastService, CachedLocationService cachedLocationService,
                         DateHelper dateHelper) {
        this.forecastServices = forecastServices;
        this.locationServices = locationServices;
        this.cachedForecastService = cachedForecastService;
        this.cachedLocationService = cachedLocationService;
        this.dateHelper = dateHelper;
    }

    @NotNull
    public Collection<Forecast> retrieveForecastsFor(@NotNull String locationUserInput, @NotNull LocalDateTime startDateTime, @NotNull LocalDateTime endDateTime) {
        List<Forecast> forecasts = new ArrayList<>();

        Collection<LocalDate> remainingDays = dateHelper.getDaysBetween(startDateTime, endDateTime);

        Iterator<ForecastService<?>> forecastServiceIterator = sortedAndEnabledForecastServicesIterator();

        while (!remainingDays.isEmpty() && forecastServiceIterator.hasNext()) {
            ForecastService<?> forecastService = forecastServiceIterator.next();

            Optional<? extends Location> location = findAndCacheLocation(locationUserInput, forecastService);
            if (location.isEmpty()) {
                continue;
            }

            Collection<Forecast> foundForecasts = findAndCacheForecasts(forecastService, remainingDays, location.get());
            forecasts.addAll(foundForecasts);

            remainingDays = dateHelper.determineMissingDays(forecasts, remainingDays);
        }

        return forecasts;
    }

    @NotNull
    private Optional<? extends Location> findAndCacheLocation(String locationUserInput, ForecastService<?> forecastService) {
        Class<? extends Location> locationType = forecastService.supportedLocationType();
        Optional<? extends Location> location = mapLocation(locationUserInput, locationType);
        location.ifPresent(value -> cachedLocationService.addToCacheIfNotPresent(locationUserInput, locationType, value));
        return location;
    }

    @NotNull
    private Collection<Forecast> findAndCacheForecasts(ForecastService<?> forecastService, Collection<LocalDate> remainingDays, Location location) {
        Collection<Interval> intervals = dateHelper.determineIntervals(remainingDays);
        var foundForecasts = forecastService.query(location, intervals);

        // Forecast services are allowed to return more days than requested, if that helps them make less (though more course-grained) requests.
        // We cache these extra results, but don't send them back to the user at this point
        // TODO test this behaviour
        cachedForecastService.addToCacheIfNotPresent(location, foundForecasts);
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

    private Optional<Location> mapLocation(String locationUserInput, Class<? extends Location> locationType) {
        return locationServices.stream()
                .filter(locationService -> locationService.canProvide(locationType))
                .flatMap(locationService -> locationService.map(locationUserInput, locationType).stream())
                .findFirst();
    }
}
