package be.stijnhooft.portal.weather;

import be.stijnhooft.portal.weather.parameters.converters.LocalDateParameterConverter;
import be.stijnhooft.portal.weather.parameters.converters.LocalDateTimeParameterConverter;
import net.serenitybdd.jbehave.SerenityStories;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.jbehave.core.configuration.Configuration;
import org.junit.Rule;
import org.junit.runner.RunWith;

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
