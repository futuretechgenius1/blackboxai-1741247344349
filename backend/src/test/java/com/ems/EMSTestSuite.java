package com.ems;

import com.ems.config.*;
import com.ems.controller.*;
import com.ems.dto.MonthlyWorkSummaryTest;
import com.ems.dto.auth.AuthDtoTest;
import com.ems.dto.worklog.WorkLogDtoTest;
import com.ems.exception.CustomExceptionsTest;
import com.ems.exception.GlobalExceptionHandlerTest;
import com.ems.integration.ApiIntegrationTest;
import com.ems.model.RoleTest;
import com.ems.model.UserTest;
import com.ems.model.WorkLogTest;
import com.ems.service.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Employee Management System Test Suite")
@SelectClasses({
    // Main Application Test
    EmployeeManagementSystemApplicationTest.class,

    // Configuration Tests
    SecurityConfigTest.class,
    WebConfigTest.class,
    OpenApiConfigTest.class,
    JwtAuthenticationFilterTest.class,
    CustomAuthenticationEntryPointTest.class,
    DatabaseSeederTest.class,
    TestConfig.class,

    // Controller Tests
    AuthControllerTest.class,
    UserControllerTest.class,
    WorkLogControllerTest.class,
    PayrollControllerTest.class,

    // Service Tests
    AuthServiceTest.class,
    UserServiceTest.class,
    WorkLogServiceTest.class,
    PayrollServiceTest.class,
    JwtServiceTest.class,

    // Model Tests
    UserTest.class,
    WorkLogTest.class,
    RoleTest.class,

    // DTO Tests
    AuthDtoTest.class,
    WorkLogDtoTest.class,
    MonthlyWorkSummaryTest.class,

    // Exception Tests
    CustomExceptionsTest.class,
    GlobalExceptionHandlerTest.class,

    // Integration Tests
    ApiIntegrationTest.class
})
public class EMSTestSuite {
    // This class serves as a test suite configuration
    // and doesn't need any implementation
}
