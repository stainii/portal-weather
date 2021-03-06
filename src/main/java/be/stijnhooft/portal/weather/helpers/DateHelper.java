package be.stijnhooft.portal.weather.helpers;

import be.stijnhooft.portal.model.weather.Forecast;
import be.stijnhooft.portal.weather.dtos.Interval;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class DateHelper {

    private final Clock clock;

    public DateHelper(Clock clock) {
        this.clock = clock;
    }

    @NotNull
    public List<LocalDate> getDaysBetween(@NotNull LocalDateTime inclusiveStart, @NotNull LocalDateTime exclusiveEnd) {
        List<LocalDate> result = new ArrayList<>();

        if (exclusiveEnd.isBefore(inclusiveStart)) {
            return result;
        }

        LocalDate currentDay = inclusiveStart.toLocalDate();
        do {
            result.add(currentDay);
            currentDay = currentDay.plus(1, DAYS);
        } while (currentDay.atStartOfDay().isBefore(exclusiveEnd));

        return result;
    }

    public List<LocalDate> forecastsToDates(Collection<Forecast> forecastsFromServices) {
        return forecastsFromServices.stream().map(Forecast::getDate).collect(Collectors.toList());
    }

    public Collection<LocalDate> determineMissingDays(Collection<Forecast> forecasts, Collection<LocalDate> expectedDays) {
        List<LocalDate> foundDays = forecastsToDates(forecasts);

        ArrayList<LocalDate> missingDays = new ArrayList<>(expectedDays);
        missingDays.removeAll(foundDays);
        return missingDays;
    }

    public Collection<Interval> determineIntervals(@NotNull Collection<LocalDate> days) {
        return determineIntervals(new ArrayList<>(days));
    }

    public Collection<Interval> determineIntervals(@NotNull List<LocalDate> days) {
        if (days.isEmpty()) {
            return new ArrayList<>();
        }
        if (days.size() == 1) {
            return List.of(new Interval(days.get(0), days.get(0)));
        }

        days = days.stream()
                .sorted(LocalDate::compareTo)
                .collect(Collectors.toList());

        List<Interval> intervals = new ArrayList<>();
        LocalDate start = null;
        LocalDate previous = null;

        for (int i = 0; i < days.size(); i++) {
            var day = days.get(i);

            if (start == null) {
                start = day;
                continue;
            }

            LocalDate actualPrevious = previous != null ? previous : start;
            if (actualPrevious.plus(1, DAYS).isEqual(day)) {
                previous = day;
                if (i == days.size() - 1) {
                    intervals.add(new Interval(start, day));
                }
            } else {
                intervals.add(new Interval(start, actualPrevious));
                start = day;
                previous = null;
                if (i == days.size() - 1) {
                    intervals.add(new Interval(day, day));
                }
            }
        }

        return intervals;
    }

    public Set<LocalDate> intervalsToDates(Collection<Interval> intervals) {
        return intervals.stream()
                .flatMap(interval -> getDaysBetween(interval.getStartDateTime(), interval.getEndDateTime()).stream())
                .collect(Collectors.toSet());
    }

    public LocalDate xDaysAgo(int x) {
        return LocalDate.now(clock).minus(x, DAYS);
    }

    public LocalDate xDaysInTheFuture(int x) {
        return LocalDate.now(clock).plus(x, DAYS);
    }

    public LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public LocalDate toLocalDate(Instant instant) {
        return toLocalDateTime(instant).toLocalDate();
    }
}
