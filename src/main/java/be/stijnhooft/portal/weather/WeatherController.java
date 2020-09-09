package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.dtos.ForecastRequests;
import be.stijnhooft.portal.weather.dtos.ForecastResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

// TODO: test
@RestController
public class WeatherController {

    private final WeatherFacade weatherFacade;

    public WeatherController(WeatherFacade weatherFacade) {
        this.weatherFacade = weatherFacade;
    }

    @PostMapping("/forecasts")
    public ForecastResponse retrieveForecasts(@RequestBody ForecastRequests forecastRequests) {
        var forecasts = forecastRequests
                .getForecastRequests()
                .stream()
                .flatMap(req -> weatherFacade.retrieveForecastsFor(req.getLocation(), req.getStartDateTime(), req.getEndDateTime()).stream())
                .collect(Collectors.toList());

        return ForecastResponse.builder()
                .forecasts(forecasts)
                .build();
    }
}
