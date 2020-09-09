package be.stijnhooft.portal.weather.locations.services;

import be.stijnhooft.portal.weather.locations.types.Location;
import be.stijnhooft.portal.weather.locations.types.impl.OpenWeatherMapCityId;
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
public class OpenWeatherMapCityIdLocationService implements LocationService {

    public static final String CITY_LIST_GZ_FILE = "openweathermap/city.list.json.gz";

    @Override
    public boolean canProvide(Class<? extends Location> locationType) {
        return locationType.isAssignableFrom(OpenWeatherMapCityId.class);
    }

    @Override
    @SneakyThrows
    public Collection<Location> map(String location, Class<? extends Location> locationType) {
        InputStream unzippedCityListStream = unzip(CITY_LIST_GZ_FILE);
        return streamToFind(unzippedCityListStream, "name", location, "id")
                .map(cityId -> new OpenWeatherMapCityId(location, cityId))
                .stream()
                .collect(Collectors.toList());
    }

    private Optional<String> streamToFind(InputStream inputStream, String tokenKeyToFind, String tokenValueToFind, String neighbourTokenKeyForWhichTheValueShouldBeReturned) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(inputStream)) {

            String currentNeighbourTokenValue = null;
            boolean tokenValueFound = false;
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {

                // is it the end of the json object? Did we found anything? If not, reset.
                if (jsonParser.getCurrentToken() == JsonToken.END_OBJECT) {
                    if (tokenValueFound && currentNeighbourTokenValue != null) {
                        log.info("Found a location for type {} and user input {}", OpenWeatherMapCityId.class, tokenValueToFind);
                        return Optional.of(currentNeighbourTokenValue);
                    } else {
                        currentNeighbourTokenValue = null;
                    }
                }

                // is this the token we're looking for?
                String key = jsonParser.getCurrentName();
                if (tokenKeyToFind.equals(key)) {
                    String tokenValue = jsonParser.getValueAsString();
                    if (tokenValueToFind.equals(tokenValue)) {
                        tokenValueFound = true;
                    }
                }

                // or is this the neighbouring token which we should return the value of, if/when we find/have found
                // the token in this same json object?
                else if (neighbourTokenKeyForWhichTheValueShouldBeReturned.equals(key)) {
                    currentNeighbourTokenValue = jsonParser.getValueAsString();
                }
            }
        }

        log.info("Found no location for type {} and user input {}", OpenWeatherMapCityId.class, tokenValueToFind);
        return Optional.empty();
    }

    private InputStream unzip(String fileLocation) throws IOException {
        InputStream cityListResourceStream = this.getClass().getResourceAsStream("/" + fileLocation);
        return new GZIPInputStream(cityListResourceStream);
    }

    @Override
    public String name() {
        return "OpenWeatherMap CityId";
    }
}
