package com.gorest.reporting;

import com.aventstack.extentreports.Status;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Records every TestNG test result, including failures and skips. */
public class ExtentReportListener implements ITestListener, ISuiteListener, IConfigurationListener {

    @Override
    public void onStart(ISuite suite) {
        ExtentReportManager.startReport();
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentReportManager.flushReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentReportManager.startTest(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logResult(result, Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logResult(result, Status.FAIL, "Test failed");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logResult(result, Status.SKIP, "Test skipped");
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        logConfigurationResult(result, Status.FAIL, "Configuration failed");
    }

    @Override
    public void onConfigurationSkip(ITestResult result) {
        logConfigurationResult(result, Status.SKIP, "Configuration skipped");
    }

    @Override
    public void onConfigurationSuccess(ITestResult result) {
        // Configuration successes do not need a separate report entry.
    }

    private void logResult(ITestResult result, Status status, String message) {
        if (ExtentReportManager.getTest() == null) {
            ExtentReportManager.startTest(result);
        }

        if (result.getThrowable() == null) {
            ExtentReportManager.getTest().log(status, message);
        } else {
            logThrowable(status, message, result.getThrowable());
        }
        ExtentReportManager.getTest().info("Duration: "
                + (result.getEndMillis() - result.getStartMillis()) + " ms");
        ExtentReportManager.clearTest();
    }

    private void logConfigurationResult(ITestResult result, Status status, String message) {
        if (ExtentReportManager.getTest() == null) {
            ExtentReportManager.startTest(result);
        }

        if (result.getThrowable() == null) {
            ExtentReportManager.getTest().log(status, message);
        } else {
            logThrowable(status, message, result.getThrowable());
        }
    }

    private void logThrowable(Status status, String message, Throwable throwable) {
        ExtentReportManager.getTest().log(status, message);
        ExtentReportManager.getTest().info("Exception:\n" + stackTrace(throwable));
    }

    private String stackTrace(Throwable throwable) {
        java.io.StringWriter output = new java.io.StringWriter();
        throwable.printStackTrace(new java.io.PrintWriter(output));
        return output.toString();
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
    @Override public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }
    @Override public void onStart(ITestContext context) { }
    @Override public void onFinish(ITestContext context) { }
}
