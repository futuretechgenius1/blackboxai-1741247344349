package com.ems.dto;

import com.ems.model.WorkLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MonthlyWorkSummaryTest {

    private MonthlyWorkSummary summary;
    private LocalDate date;
    private List<WorkLog> workLogs;

    @BeforeEach
    void setUp() {
        date = LocalDate.of(2023, 12, 1);
        YearMonth yearMonth = YearMonth.from(date);

        WorkLog log1 = WorkLog.builder()
                .date(date)
                .hoursWorked(8.0)
                .status(WorkLog.WorkLogStatus.APPROVED)
                .build();

        WorkLog log2 = WorkLog.builder()
                .date(date.plusDays(1))
                .hoursWorked(9.0)
                .status(WorkLog.WorkLogStatus.APPROVED)
                .build();

        WorkLog log3 = WorkLog.builder()
                .date(date.plusDays(2))
                .hoursWorked(7.5)
                .status(WorkLog.WorkLogStatus.PENDING)
                .build();

        workLogs = Arrays.asList(log1, log2, log3);

        summary = new MonthlyWorkSummary(yearMonth, workLogs);
    }

    @Test
    void calculateTotalHoursWorked() {
        // Act
        double totalHours = summary.getTotalHoursWorked();

        // Assert
        assertEquals(24.5, totalHours); // 8 + 9 + 7.5
    }

    @Test
    void calculateApprovedHours() {
        // Act
        double approvedHours = summary.getApprovedHours();

        // Assert
        assertEquals(17.0, approvedHours); // 8 + 9
    }

    @Test
    void calculatePendingHours() {
        // Act
        double pendingHours = summary.getPendingHours();

        // Assert
        assertEquals(7.5, pendingHours); // 7.5
    }

    @Test
    void calculateAverageHoursPerDay() {
        // Act
        double averageHours = summary.getAverageHoursPerDay();

        // Assert
        assertEquals(8.17, averageHours, 0.01); // 24.5 / 3
    }

    @Test
    void calculateWorkDaysCount() {
        // Act
        int workDays = summary.getWorkDaysCount();

        // Assert
        assertEquals(3, workDays);
    }

    @Test
    void handleEmptyWorkLogs() {
        // Arrange
        MonthlyWorkSummary emptySummary = new MonthlyWorkSummary(YearMonth.from(date), List.of());

        // Assert
        assertEquals(0.0, emptySummary.getTotalHoursWorked());
        assertEquals(0.0, emptySummary.getApprovedHours());
        assertEquals(0.0, emptySummary.getPendingHours());
        assertEquals(0.0, emptySummary.getAverageHoursPerDay());
        assertEquals(0, emptySummary.getWorkDaysCount());
    }

    @Test
    void calculateOvertimeHours() {
        // Arrange
        WorkLog overtimeLog = WorkLog.builder()
                .date(date)
                .hoursWorked(10.0)
                .status(WorkLog.WorkLogStatus.APPROVED)
                .build();

        MonthlyWorkSummary overtimeSummary = new MonthlyWorkSummary(
                YearMonth.from(date),
                List.of(overtimeLog)
        );

        // Act
        double overtimeHours = overtimeSummary.getOvertimeHours();

        // Assert
        assertEquals(2.0, overtimeHours); // 10 - 8 (standard work day)
    }

    @Test
    void yearMonthIsCorrect() {
        // Assert
        assertEquals(YearMonth.from(date), summary.getYearMonth());
    }

    @Test
    void calculateTotalWorkLogs() {
        // Assert
        assertEquals(3, summary.getTotalWorkLogs());
    }

    @Test
    void calculateApprovedWorkLogs() {
        // Assert
        assertEquals(2, summary.getApprovedWorkLogs());
    }

    @Test
    void calculatePendingWorkLogs() {
        // Assert
        assertEquals(1, summary.getPendingWorkLogs());
    }

    @Test
    void handleNullWorkLogs() {
        // Act
        MonthlyWorkSummary nullSummary = new MonthlyWorkSummary(YearMonth.from(date), null);

        // Assert
        assertEquals(0.0, nullSummary.getTotalHoursWorked());
        assertEquals(0, nullSummary.getWorkDaysCount());
    }

    @Test
    void equalsAndHashCode() {
        // Arrange
        MonthlyWorkSummary sameSummary = new MonthlyWorkSummary(YearMonth.from(date), workLogs);
        MonthlyWorkSummary differentSummary = new MonthlyWorkSummary(
                YearMonth.from(date.plusMonths(1)),
                workLogs
        );

        // Assert
        assertEquals(summary, sameSummary);
        assertEquals(summary.hashCode(), sameSummary.hashCode());
        assertNotEquals(summary, differentSummary);
        assertNotEquals(summary.hashCode(), differentSummary.hashCode());
    }

    @Test
    void toStringContainsRelevantInformation() {
        // Act
        String stringRepresentation = summary.toString();

        // Assert
        assertTrue(stringRepresentation.contains(YearMonth.from(date).toString()));
        assertTrue(stringRepresentation.contains(String.valueOf(summary.getTotalHoursWorked())));
        assertTrue(stringRepresentation.contains(String.valueOf(summary.getWorkDaysCount())));
    }
}
