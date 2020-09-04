package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.locations.types.LatitudeLongitude;
import be.stijnhooft.portal.weather.locations.types.OpenWeatherMapCityId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

}