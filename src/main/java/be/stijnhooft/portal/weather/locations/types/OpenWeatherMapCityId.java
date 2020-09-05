package be.stijnhooft.portal.weather.locations.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenWeatherMapCityId implements Location {
    private String cityId;
}
