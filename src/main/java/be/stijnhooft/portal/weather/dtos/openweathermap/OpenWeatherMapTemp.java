package be.stijnhooft.portal.weather.dtos.openweathermap;

import lombok.Data;

@Data
public class OpenWeatherMapTemp {
    private double day;
    private double min;
    private double max;
    private double night;
    private double eve;
    private double morn;
}
