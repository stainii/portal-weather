package be.stijnhooft.portal.weather.dtos.openweathermap;

import lombok.Data;

@Data
public class OpenWeatherMapWeather {
    private int id;
    private String main;
    private String description;
    private String icon;
}
