package be.stijnhooft.portal.weather.forecasts.services.impl;

import be.stijnhooft.portal.weather.cache.CacheService;
import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class LastCachedForecastService implements ForecastService<Location> {

    private final CacheService cacheService;

    public LastCachedForecastService(CacheService cacheService) {
        this.cacheService = cacheService;
    }


    @Override
    public Class<Location> supportedLocationType() {
        return Location.class;
    }

    @Override
    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        var forecasts = cacheService.find(location, intervals);
        log.info("Found {} possibly outdated forecasts in cache for {} between {}.", forecasts.size(), location, intervals);
        return forecasts;
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
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
