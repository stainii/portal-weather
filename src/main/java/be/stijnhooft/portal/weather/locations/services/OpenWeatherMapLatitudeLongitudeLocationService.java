package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.locations.types.Location;
import be.stijnhooft.portal.weather.locations.types.impl.LatitudeLongitude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class OpenWeatherMapLatitudeLongitudeLocationService implements LocationService {

    public static final String CITY_LIST_GZ_FILE = "openweathermap/city.list.json.gz";

    @Override
    public boolean canProvide(Class<? extends Location> locationType) {
        return locationType.isAssignableFrom(LatitudeLongitude.class);
    }

    @Override
    @SneakyThrows
    public Collection<Location> map(String location, Class<? extends Location> locationType) {
        InputStream unzippedCityListStream = unzip(CITY_LIST_GZ_FILE);
        return streamToFind(unzippedCityListStream, location)
                .stream()
                .collect(Collectors.toList());
    }

    private Optional<Location> streamToFind(InputStream inputStream, String location) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(inputStream)) {

            boolean locationFound = false;
            String latitude = null;
            String longitude = null;

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {

                // is it the end of the json object? Did we found anything? If not, reset.
                if (jsonParser.getCurrentToken() == JsonToken.END_OBJECT) {
                    if (locationFound && latitude != null && longitude != null) {
                        log.info("Found a location for type {} and user input {}", LatitudeLongitude.class, location);
                        return Optional.of(new LatitudeLongitude(location, latitude, longitude));
                    } else {
                        latitude = null;
                        longitude = null;
                    }
                }

                // is this the token we're looking for?
                String key = jsonParser.getCurrentName();
                if ("name".equals(key)) {
                    String tokenValue = jsonParser.getValueAsString();
                    if (location.equals(tokenValue)) {
                        locationFound = true;
                    }
                } else if ("lat".equals(key)) {
                    latitude = jsonParser.getValueAsString();
                } else if ("lon".equals(key)) {
                    longitude = jsonParser.getValueAsString();
                }
            }
        }

        log.info("Found no location for type {} and user input {}", LatitudeLongitude.class, location);
        return Optional.empty();
    }

    private InputStream unzip(String fileLocation) throws IOException {
        InputStream cityListResourceStream = this.getClass().getResourceAsStream("/" + fileLocation);
        return new GZIPInputStream(cityListResourceStream);
    }

    @Override
    public String name() {
        return "OpenWeatherMap Latitude Longitude";
    }
}
