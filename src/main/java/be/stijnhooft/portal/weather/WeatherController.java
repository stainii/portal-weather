package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.dtos.ForecastRequests;
import be.stijnhooft.portal.weather.dtos.ForecastResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

// TODO: test
@Slf4j
@RestController
public class WeatherController {

    private final WeatherFacade weatherFacade;

    public WeatherController(WeatherFacade weatherFacade) {
        this.weatherFacade = weatherFacade;
    }

    @PostMapping("/forecasts")
    public ForecastResponse retrieveForecasts(@RequestBody ForecastRequests forecastRequests) {
        log.info("Received forecast requests: {}.", forecastRequests);
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
