package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

@Value
public class ForecastCacheKey implements Serializable {

    private Location location;
    private LocalDate date;

}
