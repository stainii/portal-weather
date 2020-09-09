package be.stijnhooft.portal.weather.locations.types.impl;

import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.*;

@Value
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class LatitudeLongitude implements Location {

    String userInput;
    String latitude;
    String longitude;

}
