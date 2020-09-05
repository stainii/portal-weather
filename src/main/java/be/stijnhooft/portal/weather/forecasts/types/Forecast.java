package be.stijnhooft.portal.weather.forecasts.types;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@With
public class Forecast implements Serializable {
    @NonNull private String location;
    @NonNull private LocalDate date;
    @NonNull private String source;
    // TODO ...
}
