package be.stijnhooft.portal.weather.locations.services.impl;

import be.stijnhooft.portal.weather.locations.services.OpenWeatherMapLatitudeLongitudeLocationService;
import be.stijnhooft.portal.weather.locations.types.impl.LatitudeLongitude;
import be.stijnhooft.portal.weather.locations.types.impl.OpenWeatherMapCityId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OpenWeatherMapLatitudeLongitudeLocationServiceTest {

    @InjectMocks
    private OpenWeatherMapLatitudeLongitudeLocationService service;

    @Test
    void canProvideWhenTrue() {
        assertThat(service.canProvide(LatitudeLongitude.class)).isTrue();
    }

    @Test
    void canProvideWhenFalse() {
        assertThat(service.canProvide(OpenWeatherMapCityId.class)).isFalse();
    }

    @Test
    void mapWhenFound() {
        assertThat(service.map("Zottegem", LatitudeLongitude.class))
                .contains(new LatitudeLongitude("Zottegem", "50.86956", "3.81052"));
    }

    @Test
    void mapWhenNotFound() {
        assertThat(service.map("Gekkegem", LatitudeLongitude.class)).isEmpty();
    }

}