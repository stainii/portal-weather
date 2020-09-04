package be.stijnhooft.portal.weather.forecast.services;

import be.stijnhooft.portal.weather.forecast.Forecast;
import be.stijnhooft.portal.weather.forecast.Interval;
import be.stijnhooft.portal.weather.locations.types.Location;
import be.stijnhooft.portal.weather.locations.types.OpenWeatherMapCityId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class OpenWeatherMapForecastService implements ForecastService<OpenWeatherMapCityId> {

    @Value("${be.stijnhooft.portal.weather.service.OpenWeatherMap.enabled:true}")
    private boolean enabled;

    @Value("${be.stijnhooft.portal.weather.service.OpenWeatherMap.order:1}")
    private int order;

    @Override
    public Class<OpenWeatherMapCityId> supportedLocationType() {
        return OpenWeatherMapCityId.class;
    }

    @Override
    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        return null;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String name() {
        return "OpenWeatherMap";
    }
}
