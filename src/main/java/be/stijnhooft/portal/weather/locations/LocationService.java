package be.stijnhooft.portal.weather.locations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class LocationService {

    public static final String SERVICE_ID = "location";

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public Optional<Location> map(String locationUserInput) {
        String url = UriComponentsBuilder.fromHttpUrl(findPortalLocationUrl())
                .path("geocode")
                .queryParam("query", locationUserInput)
                .build()
                .encode()
                .toString();
        ResponseEntity<Location> response = restTemplate.getForEntity(url, Location.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.warn("Location service responded with HTTP status {}, body: {}", response.getStatusCode(), response.getBody());
            return Optional.empty();
        } else {
            return Optional.of(response.getBody());
        }
    }

    private String findPortalLocationUrl() {
        List<ServiceInstance> portalImageInstances = discoveryClient.getInstances(SERVICE_ID);
        if (portalImageInstances != null && !portalImageInstances.isEmpty()) {
            return portalImageInstances.get(0).getUri().toString() + "/";
        } else {
            throw new IllegalStateException("No instance of " + SERVICE_ID + " registered with Eureka");
        }
    }
}
