package be.stijnhooft.portal.weather.forecasts.services.impl;

import be.stijnhooft.portal.weather.cache.CacheService;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
@Slf4j
public class FirstCachedForecastService implements ForecastService {

    @Value("${be.stijnhooft.portal.weather.cache.forecasts.hours-considered-up-to-date:1}")
    private int hoursConsideredUpToDate;

    private final Clock clock;
    private final CacheService cacheService;

    public FirstCachedForecastService(Clock clock, CacheService cacheService) {
        this.clock = clock;
        this.cacheService = cacheService;
    }

    @Override
    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        var cachedForecasts = cacheService.findForecasts(location, intervals);
        var upToDateCachedForecasts = cachedForecasts
                .stream()
                .filter(forecast -> forecast.getCreatedAt().isAfter(LocalDateTime.now(clock).minus(hoursConsideredUpToDate, HOURS)))
                .collect(Collectors.toList());
        log.info("Found {} cached forecasts of which {} are still considered up to date for {} at {}", cachedForecasts.size(), upToDateCachedForecasts.size(), location.getUserInput(), intervals);
        return upToDateCachedForecasts;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

}
