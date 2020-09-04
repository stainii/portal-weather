package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.weather.locations.types.Location;
import lombok.Value;

import java.io.Serializable;

@SuppressWarnings("RedundantModifiersValueLombok")
@Value
public class LocationCacheValue implements Serializable {

    private Class<? extends Location> locationType;
    private Location location;

}
