package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.weather.locations.types.LatitudeLongitude;
import be.stijnhooft.portal.weather.locations.types.OpenWeatherMapCityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationCacheValuesTest {

    private LocationCacheValues locationCacheValues;

    @BeforeEach
    void init() {
        locationCacheValues = new LocationCacheValues();
    }

    @Test
    void addIfAbsentWhenAbsent() {
        assertThat(locationCacheValues).isEmpty();

        LocationCacheValue locationCacheValue1 = new LocationCacheValue(OpenWeatherMapCityId.class, new OpenWeatherMapCityId(""));
        locationCacheValues.addIfAbsent(locationCacheValue1);
        assertThat(locationCacheValues).hasSize(1);

        LocationCacheValue locationCacheValue2 = new LocationCacheValue(LatitudeLongitude.class, new LatitudeLongitude());
        locationCacheValues.addIfAbsent(locationCacheValue2);
        assertThat(locationCacheValues).hasSize(2);
    }

    @Test
    void addIfAbsentWhenAlreadyThere() {
        assertThat(locationCacheValues).isEmpty();

        LocationCacheValue locationCacheValue1 = new LocationCacheValue(OpenWeatherMapCityId.class, new OpenWeatherMapCityId(""));
        locationCacheValues.addIfAbsent(locationCacheValue1);
        assertThat(locationCacheValues).hasSize(1);

        // add the same object twice
        locationCacheValues.addIfAbsent(locationCacheValue1);
        assertThat(locationCacheValues).hasSize(1);

        // add an equal object
        LocationCacheValue locationCacheValue2 = new LocationCacheValue(OpenWeatherMapCityId.class, new OpenWeatherMapCityId(""));
        locationCacheValues.addIfAbsent(locationCacheValue2);
        assertThat(locationCacheValues).hasSize(1);
    }

    @Test
    void addIfAbsentWhenNull() {
        assertThrows(NullPointerException.class, () -> locationCacheValues.addIfAbsent(null));
    }
}