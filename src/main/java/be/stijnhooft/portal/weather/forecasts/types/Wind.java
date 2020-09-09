package be.stijnhooft.portal.weather.forecasts.types;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Wind implements Serializable {
    private int beaufort;
    private WindDirection direction;
}
