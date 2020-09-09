package be.stijnhooft.portal.weather.dtos.openweathermap;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OpenWeatherMapForecast {
    private Instant dt;
    private OpenWeatherMapTemp temp;
    private OpenWeatherMapFeelsLike feels_like;
    private double wind_speed;
    private double wind_deg;
    private int clouds;
    private float pop;
    private List<OpenWeatherMapWeather> weather;
}