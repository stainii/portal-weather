package be.stijnhooft.portal.weather.forecasts.types;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Temperature implements Serializable {

    private double minTemperature;
    private double maxTemperature;
    private Double feelsLike;

}
