package be.stijnhooft.portal.weather.helpers;

import be.stijnhooft.portal.weather.dtos.Interval;
import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import be.stijnhooft.portal.weather.integration.stubs.AdaptableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DateHelperTest {

    private DateHelper dateHelper;
    private AdaptableClock clock;

    @BeforeEach
    void setUp() {
        clock = new AdaptableClock(Instant.parse("2020-12-22T00:00:00Z"));
        dateHelper = new DateHelper(clock);
    }

    @Test
    void getDaysBetweenWhenEndTimeIsGreaterThanStartTime() {
        var dateTime1 = LocalDateTime.of(2020, 9, 4, 10, 0);
        var dateTime2 = LocalDateTime.of(2020, 9, 9, 20, 0);
        assertThat(dateHelper.getDaysBetween(dateTime1, dateTime2)).isEqualTo(List.of(
                LocalDate.of(2020, 9, 4),
                LocalDate.of(2020, 9, 5),
                LocalDate.of(2020, 9, 6),
                LocalDate.of(2020, 9, 7),
                LocalDate.of(2020, 9, 8),
                LocalDate.of(2020, 9, 9)
        ));
    }

    @Test
    void getDaysBetweenWhenEndTimeIsSmallerThanStartTime() {
        var dateTime1 = LocalDateTime.of(2020, 9, 4, 10, 0);
        var dateTime2 = LocalDateTime.of(2020, 9, 9, 5, 0);
        assertThat(dateHelper.getDaysBetween(dateTime1, dateTime2)).isEqualTo(List.of(
                LocalDate.of(2020, 9, 4),
                LocalDate.of(2020, 9, 5),
                LocalDate.of(2020, 9, 6),
                LocalDate.of(2020, 9, 7),
                LocalDate.of(2020, 9, 8),
                LocalDate.of(2020, 9, 9)
        ));
    }

    @Test
    void getDaysBetweenWhenEndTimeIsMidnight() {
        var dateTime1 = LocalDateTime.of(2020, 9, 4, 10, 0);
        var dateTime2 = LocalDateTime.of(2020, 9, 9, 0, 0);
        assertThat(dateHelper.getDaysBetween(dateTime1, dateTime2)).isEqualTo(List.of(
                LocalDate.of(2020, 9, 4),
                LocalDate.of(2020, 9, 5),
                LocalDate.of(2020, 9, 6),
                LocalDate.of(2020, 9, 7),
                LocalDate.of(2020, 9, 8)
        ));
    }

    @Test
    void getDaysBetweenWhenStartIsEqualToEnd() {
        var dateTime1 = LocalDateTime.of(2020, 9, 4, 10, 0);
        var dateTime2 = LocalDateTime.of(2020, 9, 4, 20, 0);
        assertThat(dateHelper.getDaysBetween(dateTime1, dateTime2)).isEqualTo(List.of(LocalDate.of(2020, 9, 4)));
    }

    @Test
    void getDaysBetweenWhenStartIsSmallerThanEnd() {
        var dateTime1 = LocalDateTime.of(2020, 9, 10, 10, 0);
        var dateTime2 = LocalDateTime.of(2020, 9, 1, 20, 0);
        assertThat(dateHelper.getDaysBetween(dateTime1, dateTime2)).isEmpty();
    }

    @Test
    void toDates() {
        var date1 = LocalDate.of(2020, 9, 1);
        var date2 = LocalDate.of(2020, 9, 2);

        var forecast1 = Forecast.builder()
                .date(date1)
                .source("")
                .location("")
                .createdAt(LocalDateTime.now())
                .build();
        var forecast2 = Forecast.builder()
                .date(date2)
                .source("")
                .location("")
                .createdAt(LocalDateTime.now())
                .build();

        var results = dateHelper.forecastsToDates(List.of(forecast1, forecast2));
        assertThat(results).isEqualTo(List.of(date1, date2));
    }

    @Test
    void determineMissingDays() {
        var date1 = LocalDate.of(2020, 9, 1);
        var date2 = LocalDate.of(2020, 9, 2);
        var date3 = LocalDate.of(2020, 9, 3);
        var date4 = LocalDate.of(2020, 9, 4);

        var forecast1 = Forecast.builder()
                .date(date1)
                .source("")
                .location("")
                .createdAt(LocalDateTime.now())
                .build();
        var forecast2 = Forecast.builder()
                .date(date3)
                .source("")
                .location("")
                .createdAt(LocalDateTime.now())
                .build();

        var results = dateHelper.determineMissingDays(List.of(forecast1, forecast2), List.of(date1, date2, date3, date4));
        assertThat(results).isEqualTo(List.of(date2, date4));
    }

    @Test
    void determineMissingDaysWhenForecastsListIsEmpty() {
        var date1 = LocalDate.of(2020, 9, 1);
        var date2 = LocalDate.of(2020, 9, 2);
        var date3 = LocalDate.of(2020, 9, 3);
        var date4 = LocalDate.of(2020, 9, 4);

        var results = dateHelper.determineMissingDays(new ArrayList<>(), List.of(date1, date2, date3, date4));
        assertThat(results).isEqualTo(List.of(date1, date2, date3, date4));
    }

    @Test
    void determineIntervalsWhenListIsEmpty() {
        assertThat(dateHelper.determineIntervals(new ArrayList<>())).isEmpty();
    }

    @Test
    void determineIntervalsWhenOneDate() {
        assertThat(dateHelper.determineIntervals(List.of(LocalDate.of(2020, 6, 6))))
                .containsExactlyInAnyOrder(new Interval(LocalDate.of(2020, 6, 6), LocalDate.of(2020, 6, 6)));
    }

    @Test
    void determineIntervalsWhenOneInterval() {
        assertThat(dateHelper.determineIntervals(List.of(
                LocalDate.of(2020, 6, 6),
                LocalDate.of(2020, 6, 7),
                LocalDate.of(2020, 6, 8))
        )).containsExactlyInAnyOrder(new Interval(LocalDate.of(2020, 6, 6), LocalDate.of(2020, 6, 8)));
    }

    @Test
    void determineIntervalsWhenMultipleIntervals() {
        assertThat(dateHelper.determineIntervals(List.of(
                LocalDate.of(2020, 6, 6),
                LocalDate.of(2020, 6, 7),
                LocalDate.of(2020, 6, 8),
                LocalDate.of(2020, 7, 1),
                LocalDate.of(2020, 7, 2),
                LocalDate.of(2020, 7, 14))
        )).containsExactlyInAnyOrder(
                new Interval(LocalDate.of(2020, 6, 6), LocalDate.of(2020, 6, 8)),
                new Interval(LocalDate.of(2020, 7, 1), LocalDate.of(2020, 7, 2)),
                new Interval(LocalDate.of(2020, 7, 14), LocalDate.of(2020, 7, 14))
        );
    }

    @Test
    void intervalsToDates() {
        assertThat(dateHelper.intervalsToDates(List.of(
                new Interval(LocalDate.of(2020, 6, 6), LocalDate.of(2020, 6, 8)),
                new Interval(LocalDate.of(2020, 7, 1), LocalDate.of(2020, 7, 2)),
                new Interval(LocalDate.of(2020, 7, 14), LocalDate.of(2020, 7, 14))
        ))).containsAll(List.of(
                LocalDate.of(2020, 6, 6),
                LocalDate.of(2020, 6, 7),
                LocalDate.of(2020, 6, 8),
                LocalDate.of(2020, 7, 1),
                LocalDate.of(2020, 7, 2),
                LocalDate.of(2020, 7, 14))
        );
    }

    @Test
    void xDaysAgo() {
        var fiveDaysAgo = dateHelper.xDaysAgo(5);
        assertThat(fiveDaysAgo.toString()).isEqualTo("2020-12-17");
    }

    @Test
    void xDaysInTheFuture() {
        var fourDaysInTheFuture = dateHelper.xDaysInTheFuture(4);
        assertThat(fourDaysInTheFuture.toString()).isEqualTo("2020-12-26");
    }
}