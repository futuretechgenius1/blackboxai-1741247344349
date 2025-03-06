package com.ems.util;

import com.ems.dto.MonthlyWorkSummary;
import com.ems.dto.worklog.WorkLogResponse;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.regex.Pattern;

/**
 * Custom Hamcrest matchers for testing EMS application components
 */
public final class TestMatchers {

    private TestMatchers() {
        // Private constructor to prevent instantiation
    }

    // User matchers
    public static Matcher<User> isUser(User expected) {
        return new TypeSafeMatcher<User>() {
            @Override
            protected boolean matchesSafely(User actual) {
                return expected.getId().equals(actual.getId()) &&
                       expected.getUsername().equals(actual.getUsername()) &&
                       expected.getEmail().equals(actual.getEmail()) &&
                       expected.getRole().equals(actual.getRole());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a user matching ")
                          .appendValue(expected);
            }
        };
    }

    public static Matcher<User> hasRole(Role role) {
        return new TypeSafeMatcher<User>() {
            @Override
            protected boolean matchesSafely(User user) {
                return user.getRole().equals(role);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a user with role ")
                          .appendValue(role);
            }
        };
    }

    // WorkLog matchers
    public static Matcher<WorkLog> isWorkLog(WorkLog expected) {
        return new TypeSafeMatcher<WorkLog>() {
            @Override
            protected boolean matchesSafely(WorkLog actual) {
                return expected.getId().equals(actual.getId()) &&
                       expected.getUser().getId().equals(actual.getUser().getId()) &&
                       expected.getDate().equals(actual.getDate()) &&
                       expected.getHoursWorked().equals(actual.getHoursWorked());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a work log matching ")
                          .appendValue(expected);
            }
        };
    }

    public static Matcher<WorkLogResponse> isWorkLogResponse(WorkLogResponse expected) {
        return new TypeSafeMatcher<WorkLogResponse>() {
            @Override
            protected boolean matchesSafely(WorkLogResponse actual) {
                return expected.getId().equals(actual.getId()) &&
                       expected.getUserId().equals(actual.getUserId()) &&
                       expected.getDate().equals(actual.getDate()) &&
                       expected.getHoursWorked().equals(actual.getHoursWorked());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a work log response matching ")
                          .appendValue(expected);
            }
        };
    }

    // MonthlyWorkSummary matchers
    public static Matcher<MonthlyWorkSummary> isMonthlyWorkSummary(MonthlyWorkSummary expected) {
        return new TypeSafeMatcher<MonthlyWorkSummary>() {
            @Override
            protected boolean matchesSafely(MonthlyWorkSummary actual) {
                return expected.getYearMonth().equals(actual.getYearMonth()) &&
                       expected.getTotalHoursWorked().equals(actual.getTotalHoursWorked()) &&
                       expected.getWorkDaysCount() == actual.getWorkDaysCount();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a monthly work summary matching ")
                          .appendValue(expected);
            }
        };
    }

    // Date matchers
    public static Matcher<LocalDate> isDate(LocalDate expected) {
        return new TypeSafeMatcher<LocalDate>() {
            @Override
            protected boolean matchesSafely(LocalDate actual) {
                return expected.equals(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("date ")
                          .appendValue(expected);
            }
        };
    }

    public static Matcher<YearMonth> isYearMonth(YearMonth expected) {
        return new TypeSafeMatcher<YearMonth>() {
            @Override
            protected boolean matchesSafely(YearMonth actual) {
                return expected.equals(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("year month ")
                          .appendValue(expected);
            }
        };
    }

    // String matchers
    public static Matcher<String> isValidJwt() {
        return new TypeSafeMatcher<String>() {
            private final Pattern JWT_PATTERN = 
                Pattern.compile("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$");

            @Override
            protected boolean matchesSafely(String token) {
                return JWT_PATTERN.matcher(token).matches();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a valid JWT token");
            }
        };
    }

    public static Matcher<String> isValidEmail() {
        return new TypeSafeMatcher<String>() {
            private final Pattern EMAIL_PATTERN = 
                Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

            @Override
            protected boolean matchesSafely(String email) {
                return EMAIL_PATTERN.matcher(email).matches();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a valid email address");
            }
        };
    }

    // Numeric matchers
    public static Matcher<Double> isMoneyAmount(double expected) {
        return new TypeSafeMatcher<Double>() {
            @Override
            protected boolean matchesSafely(Double actual) {
                return Math.abs(expected - actual) < 0.01;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("money amount ")
                          .appendValue(expected);
            }
        };
    }

    public static Matcher<Double> isHoursWorked(double expected) {
        return new TypeSafeMatcher<Double>() {
            @Override
            protected boolean matchesSafely(Double actual) {
                return Math.abs(expected - actual) < 0.01;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("hours worked ")
                          .appendValue(expected);
            }
        };
    }

    // Collection matchers
    public static Matcher<Iterable<?>> hasSize(int size) {
        return new TypeSafeMatcher<Iterable<?>>() {
            @Override
            protected boolean matchesSafely(Iterable<?> items) {
                int count = 0;
                for (Object item : items) {
                    count++;
                }
                return count == size;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("collection of size ")
                          .appendValue(size);
            }
        };
    }
}
