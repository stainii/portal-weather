package be.stijnhooft.portal.weather.forecasts.types;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@With
public class Forecast implements Serializable {

    @NonNull private String location;
    @NonNull private LocalDate date;
    @NonNull private String source;
    @NonNull private LocalDateTime createdAt;

    /** temperature in celsius **/
    private Temperature temperature;

    /** cloudiness between 0 and 100 **/
    private Integer cloudiness;

    private Precipitation precipitation;

    private Wind wind;

}
