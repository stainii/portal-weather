package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.forecasts.types.WindDirection;
import org.springframework.stereotype.Component;

@Component
public class MetricsHelper {

    public int metersPerSecondToBeaufort(double metersPerSecond) {
        if (metersPerSecond < 0.2) {
            return 0;
        }
        if (metersPerSecond < 1.5) {
            return 1;
        }
        if (metersPerSecond < 3.3) {
            return 2;
        }
        if (metersPerSecond < 5.4) {
            return 3;
        }
        if (metersPerSecond < 7.9) {
            return 4;
        }
        if (metersPerSecond < 10.7) {
            return 5;
        }
        if (metersPerSecond < 13.8) {
            return 6;
        }
        if (metersPerSecond < 17.1) {
            return 7;
        }
        if (metersPerSecond < 20.7) {
            return 8;
        }
        if (metersPerSecond < 24.4) {
            return 9;
        }
        if (metersPerSecond < 28.4) {
            return 10;
        }
        if (metersPerSecond < 32.6) {
            return 11;
        }
        return 12;
    }

    public WindDirection degreesToWindDirection(double degrees) {
        if (degrees < 0 || degrees > 360) {
            throw new IllegalArgumentException("Degrees cannot be smaller than 0 or larger than 360. Actual: " + degrees);
        }
        if (degrees < 45 || degrees == 360) {
            return WindDirection.NORTH;
        }
        if (degrees < 90) {
            return WindDirection.NORTH_EAST;
        }
        if (degrees < 135) {
            return WindDirection.EAST;
        }
        if (degrees < 180) {
            return WindDirection.SOUTH_EAST;
        }
        if (degrees < 225) {
            return WindDirection.SOUTH;
        }
        if (degrees < 270) {
            return WindDirection.SOUTH_WEST;
        }
        if (degrees < 315) {
            return WindDirection.WEST;
        }
        return WindDirection.NORTH_WEST;
    }
}
