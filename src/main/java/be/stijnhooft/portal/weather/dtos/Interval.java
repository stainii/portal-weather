package be.stijnhooft.portal.weather.dtos;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.DAYS;

@SuppressWarnings("RedundantModifiersValueLombok")
@Value
public class Interval {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public Interval(LocalDateTime start, LocalDateTime end) {
        this.startDateTime = start;
        this.endDateTime = end;
    }

    public Interval(LocalDate inclusiveStart, LocalDate inclusiveEnd) {
        this(inclusiveStart.atStartOfDay(), inclusiveEnd.plus(1, DAYS).atStartOfDay());
    }
}
