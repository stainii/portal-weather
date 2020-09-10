package be.stijnhooft.portal.weather.helpers;

import be.stijnhooft.portal.weather.forecasts.types.WindDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricsHelperTest {

    private MetricsHelper metricsHelper;

    @BeforeEach
    void init() {
        metricsHelper = new MetricsHelper();
    }

    @ParameterizedTest
    @CsvSource({"-1,0", "0,0", "0.1,0", "0.2,1", "1.4,1", "1.5,2", "3.0,2", "3.299999,2", "3.3,3", "4.5,3",
            "5.3999999999999,3", "5.4,4", "6.3,4", "7.9,5", "10.0,5", "10.7,6", "12.7,6", "13.8,7", "15.6,7",
            "17.1,8", "20.3,8", "20.7,9", "22.3,9", "24.4,10", "26.8,10", "28.4,11", "30.1,11", "32.6,12", "100,12"})
    void metersPerSecondToBeaufort(double metersPerSecond, int beaufort) {
        int actualValue = metricsHelper.metersPerSecondToBeaufort(metersPerSecond);
        assertThat(actualValue).isEqualTo(beaufort);
    }

    @ParameterizedTest
    @CsvSource({"0,NORTH", "360,NORTH", "32,NORTH", "45,NORTH_EAST", "56,NORTH_EAST", "90,EAST", "111,EAST",
            "135,SOUTH_EAST", "167,SOUTH_EAST", "180,SOUTH", "222,SOUTH", "225,SOUTH_WEST", "255,SOUTH_WEST",
            "270,WEST", "298,WEST", "315,NORTH_WEST", "359,NORTH_WEST"})
    void degreesToWindDirection(double degrees, WindDirection windDirection) {
        WindDirection actualValue = metricsHelper.degreesToWindDirection(degrees);
        assertThat(actualValue).isEqualTo(windDirection);
    }

    void degreesToWindDirectionWhenDegreesIsSmallerThan0() {
        assertThrows(IllegalArgumentException.class, () -> metricsHelper.degreesToWindDirection(-1));
    }

    void degreesToWindDirectionWhenDegreesIsLargerThan360() {
        assertThrows(IllegalArgumentException.class, () -> metricsHelper.degreesToWindDirection(361));
    }
}