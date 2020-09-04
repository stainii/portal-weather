package be.stijnhooft.portal.weather.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ForecastRequests {

    private List<ForecastRequest> forecastRequests;

}
