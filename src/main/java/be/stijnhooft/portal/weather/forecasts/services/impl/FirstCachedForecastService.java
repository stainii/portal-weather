package be.stijnhooft.portal.weather.forecasts.services.impl;

import be.stijnhooft.portal.weather.DateHelper;
import be.stijnhooft.portal.weather.cache.ForecastCacheKey;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.types.Location;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class FirstCachedForecastService extends LastCachedForecastService {

    @Value("${be.stijnhooft.portal.weather.cache.forecasts.hours-considered-up-to-date:1}")
    private int hoursConsideredUpToDate;
    private final Clock clock;

    public FirstCachedForecastService(Cache<ForecastCacheKey, Forecast> forecastsCache, DateHelper dateHelper, Clock clock) {
        super(forecastsCache, dateHelper);
        this.clock = clock;
    }

    // TODO: test
    @Override
    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        return super.query(location, intervals)
                .stream()
                .filter(forecast -> forecast.getCreatedAt().isAfter(LocalDateTime.now(clock).minus(hoursConsideredUpToDate, HOURS)))
                .collect(Collectors.toList());
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
