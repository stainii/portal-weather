package be.stijnhooft.portal.weather.dtos.openweathermap;

import lombok.Data;

import java.util.List;

@Data
public class OpenWeatherMapResponse {
    private List<OpenWeatherMapForecast> daily;
}
