package be.stijnhooft.portal.weather.dtos;

import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class ForecastResponse {

    private Collection<Forecast> forecasts;

}
