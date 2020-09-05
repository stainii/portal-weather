package be.stijnhooft.portal.weather.locations.services.impl;

import be.stijnhooft.portal.weather.locations.services.OpenWeatherMapCityIdLocationService;
import be.stijnhooft.portal.weather.locations.types.impl.LatitudeLongitude;
import be.stijnhooft.portal.weather.locations.types.impl.OpenWeatherMapCityId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OpenWeatherMapCityIdLocationServiceTest {

    @InjectMocks
    private OpenWeatherMapCityIdLocationService service;

    @Test
    void canProvideWhenTrue() {
        assertThat(service.canProvide(OpenWeatherMapCityId.class)).isTrue();
    }

    @Test
    void canProvideWhenFalse() {
        assertThat(service.canProvide(LatitudeLongitude.class)).isFalse();
    }

    @Test
    void mapWhenFound() {
        assertThat(service.map("Zottegem", OpenWeatherMapCityId.class)).contains(new OpenWeatherMapCityId("2783175"));
    }

    @Test
    void mapWhenNotFound() {
        assertThat(service.map("Gekkegem", OpenWeatherMapCityId.class)).isEmpty();
    }
}