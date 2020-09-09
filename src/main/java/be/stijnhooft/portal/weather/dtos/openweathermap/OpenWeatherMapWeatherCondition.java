package be.stijnhooft.portal.weather.dtos.openweathermap;

import be.stijnhooft.portal.weather.forecasts.types.PrecipitationType;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Source: https://openweathermap.org/weather-conditions#Weather-Condition-Codes-2
 **/
@Getter
public enum OpenWeatherMapWeatherCondition {

    THUNDERSTORM_WITH_LIGHT_RAIN(200, PrecipitationType.RAIN, 50),
    THUNDERSTORM_WITH_RAIN(201, PrecipitationType.RAIN, 70),
    THUNDERSTORM_WITH_HEAVY_RAIN(202, PrecipitationType.RAIN, 90),
    LIGHT_THUNDERSTORM(210, PrecipitationType.RAIN, 50),
    THUNDERSTORM(211, PrecipitationType.RAIN, 70),
    HEAVY_THUNDERSTORM(212, PrecipitationType.RAIN, 90),
    RAGGED_THUNDERSTORM(221, PrecipitationType.RAIN, 90),
    THUNDERSTORM_WITH_LIGHT_DRIZZLE(230, PrecipitationType.RAIN, 50),
    THUNDERSTORM_WITH_DRIZZLE(231, PrecipitationType.RAIN, 70),
    THUNDERSTORM_WITH_HEAVY_DRIZZLE(231, PrecipitationType.RAIN, 90),

    LIGHT_INTENSITY_DRIZZLE(300, PrecipitationType.RAIN, 30),
    DRIZZLE(301, PrecipitationType.RAIN, 40),
    HEAVY_INTENSITY_DRIZZLE(302, PrecipitationType.RAIN, 70),
    LIGHT_INTENSITY_DRIZZLE_RAIN(310, PrecipitationType.RAIN, 40),
    DRIZZLE_RAIN(311, PrecipitationType.RAIN, 50),
    HEAVY_INTENSITY_DRIZZLE_RAIN(312, PrecipitationType.RAIN, 80),
    SHOWER_RAIN_AND_DRIZZLE(313, PrecipitationType.RAIN, 70),
    HEAVY_SHOWER_RAIN_AND_DRIZZLE(314, PrecipitationType.RAIN, 80),
    SHOWER_DRIZZLE(321, PrecipitationType.RAIN, 60),

    LIGHT_RAIN(500, PrecipitationType.RAIN, 30),
    MODERATE_RAIN(501, PrecipitationType.RAIN, 50),
    HEAVY_INTENSITY_RAIN(502, PrecipitationType.RAIN, 70),
    VERY_HEAVY_RAIN(503, PrecipitationType.RAIN, 90),
    EXTREME_RAIN(504, PrecipitationType.RAIN, 100),
    FREEZING_RAIN(511, PrecipitationType.RAIN, 100),
    LIGHT_INTENSITY_SHOWER_RAIN(511, PrecipitationType.RAIN, 40),
    SHOWER_RAIN(511, PrecipitationType.RAIN, 50),
    HEAVY_INTENSITY_SHOWER_RAIN(511, PrecipitationType.RAIN, 70),
    RAGGED_SHOWER_RAIN(511, PrecipitationType.RAIN, 60),


    LIGHT_SNOW(600, PrecipitationType.SNOW, 25),
    SNOW(601, PrecipitationType.SNOW, 50),
    HEAVY_SNOW(602, PrecipitationType.SNOW, 75),
    SLEET(611, PrecipitationType.SNOW, 50),
    LIGHT_SHOWER_SLEET(612, PrecipitationType.SNOW, 30),
    SHOWER_SLEET(613, PrecipitationType.SNOW, 50),
    LIGHT_RAIN_AND_SNOW(615, PrecipitationType.SNOW, 30),
    RAIN_AND_SNOW(616, PrecipitationType.SNOW, 50),
    LIGHT_SHOWER_SNOW(620, PrecipitationType.SNOW, 40),
    SHOWER_SNOW(621, PrecipitationType.SNOW, 75),
    HEAVY_SHOWER_SNOW(622, PrecipitationType.SNOW, 100),

    MIST(701, PrecipitationType.FOG, 50),
    FOG(741, PrecipitationType.FOG, 50),

    CLEAR(800, PrecipitationType.NOTHING, 0),

    FEW_CLOUDS(801, PrecipitationType.NOTHING, 0),
    SCATTERED_CLOUDS(802, PrecipitationType.NOTHING, 0),
    BROKEN_CLOUDS(803, PrecipitationType.NOTHING, 0),
    OVERCAST_CLOUDS(804, PrecipitationType.NOTHING, 0),
    ;

    private final int code;
    private final PrecipitationType type;
    private final int intensity;


    OpenWeatherMapWeatherCondition(int code, PrecipitationType type, int intensity) {
        this.code = code;
        this.type = type;
        this.intensity = intensity;
    }

    public static Optional<OpenWeatherMapWeatherCondition> forCode(int code) {
        return Arrays.stream(OpenWeatherMapWeatherCondition.values())
                .filter(condition -> condition.getCode() == code)
                .findFirst();
    }
}
