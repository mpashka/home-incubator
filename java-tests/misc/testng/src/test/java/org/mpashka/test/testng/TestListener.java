package org.mpashka.test.testng;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.testng.IClassListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.xml.XmlTest;

@Slf4j
public class TestListener extends TestListenerAdapter implements IClassListener, IInvokedMethodListener {
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
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        log.info("IInvokedMethodListener.beforeInvocation: {} = {}", method, testResult);
        log.info("    method.testmethod.testclass.realclass: {}", method.getTestMethod().getTestClass().getRealClass());
        log.info("    method.testmethod.realclass: {}", method.getTestMethod().getRealClass());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        log.info("IInvokedMethodListener.afterInvocation: {} = {}", method, testResult);
        log.info("    testresult.success: {}", testResult.isSuccess());
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
