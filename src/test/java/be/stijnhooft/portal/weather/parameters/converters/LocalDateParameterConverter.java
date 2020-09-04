package be.stijnhooft.portal.weather.parameters.converters;

import org.jbehave.core.steps.ParameterConverters;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LocalDateParameterConverter extends ParameterConverters.AbstractParameterConverter<LocalDate> {

    @Override
    public LocalDate convertValue(String value, Type type) {
        return LocalDate.parse(value.trim());
    }

}