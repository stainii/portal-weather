package be.stijnhooft.portal.weather.locations.types.impl;

import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenWeatherMapCityId implements Location {
    private String userInput;
    private String cityId;
}
