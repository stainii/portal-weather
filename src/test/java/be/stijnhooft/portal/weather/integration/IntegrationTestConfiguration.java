package be.stijnhooft.portal.weather.integration;

import be.stijnhooft.portal.weather.integration.stubs.AdaptableClock;
import be.stijnhooft.portal.weather.integration.stubs.FakeLocationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IntegrationTestConfiguration {

    @Bean
    @Primary
    public AdaptableClock adaptableClock() {
        return new AdaptableClock();
    }

    @Bean
    @Primary
    public FakeLocationService locationService() {
        return new FakeLocationService();
    }
}
