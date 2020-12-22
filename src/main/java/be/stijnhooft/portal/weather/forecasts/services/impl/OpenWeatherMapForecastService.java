package be.stijnhooft.portal.weather.forecasts.services.impl;

import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.dtos.openweathermap.OpenWeatherMapResponse;
import be.stijnhooft.portal.weather.forecasts.services.ForecastService;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.helpers.DateHelper;
import be.stijnhooft.portal.weather.locations.Location;
import be.stijnhooft.portal.weather.mappers.openweathermap.OpenWeatherMapMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;

// TODO: test
@Service
@Slf4j
public class OpenWeatherMapForecastService implements ForecastService {

    private final boolean enabled;
    private final int order;
    private final String apiKey;
    private final DateHelper dateHelper;
    private final RestTemplate restTemplate;
    private final OpenWeatherMapMapper openWeatherMapMapper;

    public final static String ONE_CALL_API = "https://api.openweathermap.org/data/2.5/onecall?lat=%s&lon=%s&exclude=current,minutely,hourly&appid=%s&units=metric";

    public OpenWeatherMapForecastService(@Value("${be.stijnhooft.portal.weather.service.OpenWeatherMap.enabled:true}") boolean enabled,
                                         @Value("${be.stijnhooft.portal.weather.service.OpenWeatherMap.order:1}") int order,
                                         @Value("${be.stijnhooft.portal.weather.service.OpenWeatherMap.api-key:#{null}}") String apiKey,
                                         DateHelper dateHelper,
                                         RestTemplate restTemplate,
                                         OpenWeatherMapMapper openWeatherMapMapper) {
        this.enabled = enabled;
        this.order = order;
        this.apiKey = apiKey;
        this.dateHelper = dateHelper;
        this.restTemplate = restTemplate;
        this.openWeatherMapMapper = openWeatherMapMapper;
    }

    @PostConstruct
    public void init() {
        if (enabled && StringUtils.isEmpty(apiKey)) {
            throw new IllegalStateException("Please provide an API key for OpenWeatherMap");
        }
    }

    public Collection<Forecast> query(Location location, Collection<Interval> intervals) {
        // OpenWeatherMap doesn't provide date search functions for its free tiers. It always returns the upcoming
        // 5 days, so I always return what the API gives back. When no interval lies within the next 5 days,
        // I don't even bother calling the API.
        boolean noForecastsCanBeRetrievedForAnyOfTheDates = dateHelper.intervalsToDates(intervals).stream()
                .noneMatch(date -> date.isAfter(dateHelper.xDaysAgo(1)) && date.isBefore(dateHelper.xDaysInTheFuture(6)));

        if (noForecastsCanBeRetrievedForAnyOfTheDates) {
            return new ArrayList<>();
        }

        var url = String.format(ONE_CALL_API, location.getLatitude(), location.getLongitude(), apiKey);
        ResponseEntity<OpenWeatherMapResponse> response = restTemplate.getForEntity(url, OpenWeatherMapResponse.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.warn("OpenWeatherMap responded with HTTP status {}, body: {}", response.getStatusCode(), response.getBody());
            return new ArrayList<>();
        }

        var forecasts = openWeatherMapMapper.map(location.getUserInput(), response.getBody());

        log.info("Found {} forecasts by OpenWeatherMapForecastService", forecasts.size());
        return forecasts;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String name() {
        return "OpenWeatherMap";
    }
}
