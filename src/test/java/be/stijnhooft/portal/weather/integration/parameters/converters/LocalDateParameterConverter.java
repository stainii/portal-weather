package be.stijnhooft.portal.weather.integration.parameters.converters;

import org.jbehave.core.steps.ParameterConverters;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class LocalDateParameterConverter extends ParameterConverters.AbstractParameterConverter<LocalDate> {

    @Override
    public LocalDate convertValue(String value, Type type) {
        return LocalDate.parse(value.trim());
    }

}