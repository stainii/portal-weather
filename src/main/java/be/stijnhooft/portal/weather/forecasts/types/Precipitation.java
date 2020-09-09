package be.stijnhooft.portal.weather.forecasts.types;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Precipitation implements Serializable {

    /** intensity of the rain/snow/... between 0 (no rain at all) and 100 (the world is going to end) **/
    private int intensity;

    /** probability of the rain/snow/... between 0 and 100 **/
    private double probability;

    private PrecipitationType type;

}
