package be.stijnhooft.portal.weather.parameters;

import lombok.*;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Parameter;

import java.time.LocalDate;

@AsParameters
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastResultTable {

    @NonNull
    @Parameter(name = "location")
    private String location;

    @NonNull
    @Parameter(name = "date")
    private LocalDate date;

    @NonNull
    @Parameter(name = "source")
    private String source;

}
