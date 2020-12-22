package be.stijnhooft.portal.weather.forecasts.services;

import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.locations.Location;

import java.util.Collection;

public interface ForecastService {

    Collection<Forecast> query(Location location, Collection<Interval> intervals);

    /**
     * APIs are called in a certain order, just until all requests have been fulfilled.
     * In a perfect world, only the API with order = 1 is called, because it can serve all calls and the other API's shouldn't be bothered.
     *
     * @return configuration of the user with property. If no such configuration is found, please return 1.
     */
    int order();

    /**
     * Is this service enabled?
     *
     * @return configuration of the user with property. If no such configuration is found, please return true.
     */
    boolean enabled();

    String name();

}
