package com.gorest.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestResult;

import java.io.File;
import java.util.Arrays;

/** Central Extent Reports setup and current-test holder. */
public final class ExtentReportManager {

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static ExtentReports extentReports;

    private ExtentReportManager() {
    }

    public static synchronized void startReport() {
        if (extentReports != null) {
            return;
        }

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(
                new File("target/extent-reports/extent-report.html"));
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("GoREST API Automation Report");
        sparkReporter.config().setReportName("GoREST API Test Execution");
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Java", System.getProperty("java.version"));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Test framework", "TestNG");
        extentReports.setSystemInfo("API", "GoREST /public/v2");
    }

    public static void startTest(ITestResult result) {
        startReport();

        String methodName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        String testName = description == null || description.isBlank()
                ? methodName
                : methodName + " - " + description;

        ExtentTest test = extentReports.createTest(testName);
        test.info("Test class: " + result.getTestClass().getName());
        test.info("Test method: " + methodName);

        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            test.assignCategory(groups);
            test.info("Groups: " + String.join(", ", groups));
        }

        Object[] parameters = result.getParameters();
        if (parameters.length > 0) {
            test.info("Parameters: " + Arrays.deepToString(parameters));
        }

        CURRENT_TEST.set(test);
    }

    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static void clearTest() {
        CURRENT_TEST.remove();
    }

    public static synchronized void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
