package be.stijnhooft.portal.weather.locations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Optional;

@Service
@Slf4j
public class LocationService {

    private final RestTemplate restTemplate;
    private final String urlTemplate;

    public LocationService(RestTemplate restTemplate,
                           @Value("${be.stijnhooft.portal.weather.service.location.url}") String urlTemplate) {
        this.restTemplate = restTemplate;
        this.urlTemplate = urlTemplate;
    }

    public Optional<Location> map(String locationUserInput) {
        var url = MessageFormat.format(urlTemplate, locationUserInput);
        ResponseEntity<Location> response = restTemplate.getForEntity(url, Location.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.warn("Location service responded with HTTP status {}, body: {}", response.getStatusCode(), response.getBody());
            return Optional.empty();
        } else {
            return Optional.of(response.getBody());
        }
    }
}
