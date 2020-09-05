package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.forecasts.Forecast;
import be.stijnhooft.portal.weather.forecasts.Interval;
import be.stijnhooft.portal.weather.forecasts.services.CachedForecastService;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
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

            // find location or skip to next forecast service
            Class<? extends Location> locationType = forecastService.supportedLocationType();
            Optional<? extends Location> location = mapLocation(locationUserInput, locationType);
            if (location.isEmpty()) {
                continue;
            }
            cachedLocationService.addToCacheIfNotPresent(locationUserInput, locationType, location.get());

            // find forecasts
            Collection<Interval> intervals = dateHelper.determineIntervals(remainingDays);
            var foundForecasts = forecastService.query(location.get(), intervals);
            cachedForecastService.addToCacheIfNotPresent(location.get(), foundForecasts);
            forecasts.addAll(foundForecasts);

            // any days without results remaining?
            remainingDays = dateHelper.determineMissingDays(forecasts, remainingDays);
        }

        return forecasts;
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
