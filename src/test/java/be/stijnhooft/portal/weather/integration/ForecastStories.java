package be.stijnhooft.portal.weather.integration;

import be.stijnhooft.portal.weather.integration.parameters.converters.LocalDateParameterConverter;
import be.stijnhooft.portal.weather.integration.parameters.converters.LocalDateTimeParameterConverter;
import net.serenitybdd.jbehave.SerenityStories;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.jbehave.core.configuration.Configuration;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * JBehave test which tests the framework, but mocks actual implementations of location/forecast services.
 * This means that the framework + the cached location+forecast services are the real thing, but
 * no actual external API calls are made. The user can declare mock location/forecast services.
 *
 * Story file: {@link ../../../../../../resources/stories/forecasts/query/query-forecasts.story}
 * Step definitions: {@see be.stijnhooft.portal.weather.integration.stepdefinitions.ForecastStepDefinitions}
 */
@RunWith(SerenityRunner.class)
public class ForecastStories extends SerenityStories {

    @Rule
    public SpringIntegrationMethodRule springIntegrationMethodRule = new SpringIntegrationMethodRule();

    public ForecastStories() {
        runSerenity().inASingleSession();
    }

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        configuration.parameterConverters()
                .addConverters(new LocalDateParameterConverter(), new LocalDateTimeParameterConverter());
        return configuration;
    }

}
