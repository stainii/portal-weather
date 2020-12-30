package be.stijnhooft.portal.weather.mappers.openweathermap;

import be.stijnhooft.portal.weather.dtos.openweathermap.OpenWeatherMapResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;

import static be.stijnhooft.portal.model.weather.PrecipitationType.RAIN;
import static be.stijnhooft.portal.model.weather.WindDirection.SOUTH_WEST;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OpenWeatherMapMapperTest {

    @Autowired
    private OpenWeatherMapMapper openWeatherMapMapper;

    @Test
    void map() {
        OpenWeatherMapResponse openWeatherMapResponse = readOpenWeatherMapResponse();

        var mappedResults = new ArrayList<>(openWeatherMapMapper.map("Zottegem", openWeatherMapResponse));

        assertThat(mappedResults).hasSize(8);

        var firstMappedResult = mappedResults.get(0);
        assertThat(firstMappedResult.getDate()).isEqualTo(LocalDate.of(2020, 12, 30));
        assertThat(firstMappedResult.getSource()).isEqualTo("OpenWeatherMapForecastService");
        assertThat(firstMappedResult.getCreatedAt()).isNotNull();
        assertThat(firstMappedResult.getLocation()).isEqualTo("Zottegem");
        assertThat(firstMappedResult.getCloudiness()).isEqualTo(95);
        assertThat(firstMappedResult.getPrecipitation().getIntensity()).isEqualTo(30);
        assertThat(firstMappedResult.getPrecipitation().getProbability()).isEqualTo(63.0);
        assertThat(firstMappedResult.getPrecipitation().getType()).isEqualTo(RAIN);
        assertThat(firstMappedResult.getTemperature().getMaxTemperature()).isEqualTo(4.9);
        assertThat(firstMappedResult.getTemperature().getMinTemperature()).isEqualTo(0.86);
        assertThat(firstMappedResult.getTemperature().getFeelsLike()).isEqualTo(-0.81);
        assertThat(firstMappedResult.getWind().getBeaufort()).isEqualTo(3);
        assertThat(firstMappedResult.getWind().getDirection()).isEqualTo(SOUTH_WEST);
    }

    @SneakyThrows
    private OpenWeatherMapResponse readOpenWeatherMapResponse() {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();
        return objectMapper.readValue(getClass().getResource("/open_weather_map_example_response.json"), OpenWeatherMapResponse.class);
    }

}