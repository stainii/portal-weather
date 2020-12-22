package be.stijnhooft.portal.weather.locations;

import lombok.*;

import java.io.Serializable;

@Value
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Location implements Serializable {

    String userInput;
    String latitude;
    String longitude;

}
