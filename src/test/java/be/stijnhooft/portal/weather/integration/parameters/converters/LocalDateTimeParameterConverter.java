package be.stijnhooft.portal.weather.integration.parameters.converters;

import org.jbehave.core.steps.ParameterConverters;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeParameterConverter extends ParameterConverters.AbstractParameterConverter<LocalDateTime>  {

    @Override
    public LocalDateTime convertValue(String value, Type type) {
        return LocalDateTime.parse(value.trim());
    }

}