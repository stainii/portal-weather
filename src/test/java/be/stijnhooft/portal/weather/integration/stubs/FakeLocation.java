package be.stijnhooft.portal.weather.integration.stubs;

import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FakeLocation implements Location {
    private String userInput;
}
