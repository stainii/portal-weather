package be.stijnhooft.portal.weather.cache;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

@Value
public class ForecastCacheKey implements Serializable {

    String location;
    LocalDate date;

}
