package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.IClassListener;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.xml.XmlTest;

@Slf4j
public class TestListener extends TestListenerAdapter implements IClassListener, IInvokedMethodListener, ISuiteListener, IConfigurationListener {
    public static final boolean showException = false;

    @Override
    public void onTestStart(ITestResult result) {
        log.warn("ITestListener.onTestStart [{}.{}]",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
        ITestNGMethod method = result.getMethod();
        if (method.getRetryAnalyzer(result) == null) {
            method.setRetryAnalyzerClass(MyRetryAnalyzer.class);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.warn("ITestListener.onTestSuccess [{}.{}]",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
        ITestContext testContext = result.getTestContext();
        log.warn("    Context [{}.{}] success", testContext.getName(), testContext.getCurrentXmlTest().getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.warn("ITestListener.onTestFailure [{}.{}]",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
    }

    @Override
    public void onBeforeClass(ITestClass testClass) {
        log.info("IClassListener.onBeforeClass [{}]", testClass.getName());
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        log.info("IClassListener.onAfterClass [{}]", testClass.getName());
    }

    @Override
    public void onStart(ITestContext testContext) {
        log.warn("ITestListener.onStart [{}]", testContext.getName());
        XmlTest xmlTest = testContext.getCurrentXmlTest();
        log.warn("    XML Test {} / {}/ {} / {}", xmlTest.getName(), xmlTest.getIndex(), xmlTest.getClasses(), xmlTest.getXmlClasses());
    }

    @Override
    public void onFinish(ITestContext testContext) {
        log.warn("ITestListener.onFinish [{}]", testContext.getName());
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
        log.info("IInvokedMethodListener.beforeInvocation: {} = {}", method, testResult);
        log.info("    method.testmethod.testclass.realclass: {}", method.getTestMethod().getTestClass().getRealClass());
        log.info("    method.testmethod.realclass: {}", method.getTestMethod().getRealClass());
        log.info("    context: {}", context);
    }

    @Override
    public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult, ITestContext context) {
        log.info("IInvokedMethodListener.afterInvocation", currentStacktrace());
        log.info("    method: {}", invokedMethod);
        log.info("    testResult: {}", testResult);
        log.info("    method.testmethod: {}", invokedMethod.getTestMethod());
        log.info("    context: {}", context);
        ITestNGMethod testMethod = testResult.getMethod();
        boolean isTest = testMethod.isTest();
        log.info("    testResult.method.Test: {}", isTest);
        log.info("    testResult.method.AfterTestConfiguration: {}", testMethod.isAfterTestConfiguration());
        log.info("    testResult.method.AfterMethodConfiguration: {}", testMethod.isAfterMethodConfiguration());
        ITestNGMethod[] afterTestMethods = testMethod.getTestClass().getAfterTestMethods();
        boolean last = isTest && afterTestMethods.length == 0;
        if (testMethod.isAfterMethodConfiguration() && afterTestMethods.length > 0) {
            ITestNGMethod lastTestMethod = afterTestMethods[afterTestMethods.length-1];
            last = lastTestMethod == testMethod;
        }
        log.info("    last: {}", last);
    }

    public static Throwable currentStacktrace() {
        return showException ? new Throwable() : null;
    }

    @Override
    public void onStart(ISuite suite) {
        log.info("ISuiteListener.onStart");
        log.info("    suite: {}", suite);
        // todo [!] set group here
//        suite.getAllMethods().get(0).getGroups()
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("ISuiteListener.onFinish");
        log.info("    suite: {}", suite);
    }

    @Override
    public void beforeConfiguration(ITestResult tr, ITestNGMethod tm) {
        log.info("IConfigurationListener.beforeConfiguration");
        log.info("    testResult: {}", tr);
        logMethod(tr);
        log.info("    testMethod: {}", tm);
        if (tm != null) {
            log.info("    testMethod.method: {}", tm.getConstructorOrMethod());
        }
    }

    @Override
    public void onConfigurationSuccess(ITestResult tr, ITestNGMethod tm) {
        log.info("IConfigurationListener.onConfigurationSuccess");
        log.info("    testResult: {}", tr);
        logMethod(tr);
        log.info("    testMethod: {}", tm);
        if (tm != null) {
            log.info("    testMethod.method: {}", tm.getConstructorOrMethod());
        }
    }

    private static void logMethod(ITestResult tr) {
        log.info("    testResult.method.beforeGroupsConfiguration: {}", tr.getMethod().isBeforeGroupsConfiguration());
        log.info("    testResult.method.beforeSuiteConfiguration: {}", tr.getMethod().isBeforeSuiteConfiguration());
        log.info("    testResult.method.beforeClassConfiguration: {}", tr.getMethod().isBeforeClassConfiguration());
        log.info("    testResult.method.beforeTestConfiguration: {}", tr.getMethod().isBeforeTestConfiguration());
        log.info("    testResult.method.beforeMethodConfiguration: {}", tr.getMethod().isBeforeMethodConfiguration());
        log.info("    testResult.method.test: {}", tr.getMethod().isTest());
        log.info("    testResult.method.afterMethodConfiguration: {}", tr.getMethod().isAfterMethodConfiguration());
        log.info("    testResult.method.afterTestConfiguration: {}", tr.getMethod().isAfterTestConfiguration());
        log.info("    testResult.method.afterClassConfiguration: {}", tr.getMethod().isAfterClassConfiguration());
        log.info("    testResult.method.afterSuiteConfiguration: {}", tr.getMethod().isAfterSuiteConfiguration());
        log.info("    testResult.method.afterGroupsConfiguration: {}", tr.getMethod().isAfterGroupsConfiguration());
    }

    @Override
    public void onConfigurationFailure(ITestResult tr, ITestNGMethod tm) {
        log.info("IConfigurationListener.onConfigurationFailure");
        log.info("    testResult: {}", tr);
        log.info("    testMethod: {}", tm);
        if (tm != null) {
            log.info("    testMethod.method: {}", tm.getConstructorOrMethod());
        }
    }

    public static class MyRetryAnalyzer implements IRetryAnalyzer {
        private static final String RETRY_COUNT = "iss-retry-count";

        private int iteration = 0;

        @Override
        public boolean retry(ITestResult result) {
//            int retry = Optional.ofNullable(result.getAttribute(RETRY_COUNT)).map(r -> (Integer) r).orElse(0);
            log.info("MyRetryAnalyzer.retry. Check retry [{}] iteration: {}", hashCode(), iteration);
//            result.setAttribute(RETRY_COUNT, retry + 1);
            return iteration++ < 5;
        }
    }
}
