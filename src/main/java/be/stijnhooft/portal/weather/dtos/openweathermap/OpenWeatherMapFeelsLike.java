package be.stijnhooft.portal.weather.dtos.openweathermap;

import lombok.Data;

@Data
public class OpenWeatherMapFeelsLike {
    private double day;
    private double night;
    private double eve;
    private double morn;
}
