package be.stijnhooft.portal.weather.mappers.openweathermap;

import be.stijnhooft.portal.model.weather.Forecast;
import be.stijnhooft.portal.model.weather.Precipitation;
import be.stijnhooft.portal.model.weather.Temperature;
import be.stijnhooft.portal.model.weather.Wind;
import be.stijnhooft.portal.weather.dtos.openweathermap.OpenWeatherMapForecast;
import be.stijnhooft.portal.weather.dtos.openweathermap.OpenWeatherMapResponse;
import be.stijnhooft.portal.weather.dtos.openweathermap.OpenWeatherMapWeather;
import be.stijnhooft.portal.weather.dtos.openweathermap.OpenWeatherMapWeatherCondition;
import be.stijnhooft.portal.weather.forecasts.services.impl.OpenWeatherMapForecastService;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.helpers.MetricsHelper;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OpenWeatherMapMapper {

    private final Clock clock;
    private final DateHelper dateHelper;
    private final MetricsHelper metricsHelper;

    public OpenWeatherMapMapper(Clock clock, DateHelper dateHelper, MetricsHelper metricsHelper) {
        this.clock = clock;
        this.dateHelper = dateHelper;
        this.metricsHelper = metricsHelper;
    }

    public Collection<Forecast> map(@NonNull String locationUserInput, @NonNull OpenWeatherMapResponse openWeatherMapResponse) {
        return openWeatherMapResponse.getDaily().stream()
                .map(openWeatherMapForecast -> map(locationUserInput, openWeatherMapForecast))
                .collect(Collectors.toList());
    }

    private Forecast map(String locationUserInput, OpenWeatherMapForecast openWeatherMapForecast) {
        return Forecast.builder()
                .createdAt(LocalDateTime.now(clock))
                .source(OpenWeatherMapForecastService.class.getSimpleName())
                .location(locationUserInput)
                .date(dateHelper.toLocalDate(openWeatherMapForecast.getDt()))
                .temperature(mapTemperature(openWeatherMapForecast))
                .cloudiness(mapCloudiness(openWeatherMapForecast))
                .wind(mapWind(openWeatherMapForecast))
                .precipitation(mapPrecipitation(openWeatherMapForecast))
                .build();
    }

    private Precipitation mapPrecipitation(OpenWeatherMapForecast openWeatherMapForecast) {
        if (openWeatherMapForecast.getWeather() == null || openWeatherMapForecast.getWeather().isEmpty()) {
            return null;
        }

        OpenWeatherMapWeather weather = openWeatherMapForecast.getWeather().get(0);
        Optional<OpenWeatherMapWeatherCondition> condition = OpenWeatherMapWeatherCondition.forCode(weather.getId());
        if (condition.isEmpty()) {
            return null;
        }

        float probability = openWeatherMapForecast.getPop() * 100;
        return Precipitation.builder()
                .type(condition.get().getType())
                .intensity(condition.get().getIntensity())
                .probability(probability)
                .build();
    }

    private Wind mapWind(OpenWeatherMapForecast openWeatherMapForecast) {
        return Wind.builder()
                .beaufort(metricsHelper.metersPerSecondToBeaufort(openWeatherMapForecast.getWind_speed()))
                .direction(metricsHelper.degreesToWindDirection(openWeatherMapForecast.getWind_deg()))
                .build();
    }

    private Integer mapCloudiness(OpenWeatherMapForecast openWeatherMapForecast) {
        return Math.round(openWeatherMapForecast.getClouds());
    }

    private Temperature mapTemperature(OpenWeatherMapForecast openWeatherMapForecast) {
        if (openWeatherMapForecast.getTemp() == null) {
            return null;
        }
        return Temperature.builder()
                .minTemperature(openWeatherMapForecast.getTemp().getMin())
                .maxTemperature(openWeatherMapForecast.getTemp().getMax())
                .feelsLike(openWeatherMapForecast.getFeels_like() != null ? openWeatherMapForecast.getFeels_like().getDay() : null)
                .build();
    }
}
