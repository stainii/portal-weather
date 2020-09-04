package be.stijnhooft.portal.weather.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastRequest {

    private String location;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

}
