package org.mpashka.test.testng;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

@Slf4j
//@Listeners({TestListener.class})
public class MyTest extends MyTestBase {

    @BeforeClass
    public void init() {
        log.info("::BeforeClass");
    }

    @AfterClass
    public void tearDown(ITestContext testContext, XmlTest test) {
        log.info("::AfterClass");
    }

    @BeforeMethod
    public void beforeMethod1(ITestContext testContext, XmlTest test, ITestResult testResult, Method method, Object[] params) {
        log.info("::BeforeMethod1");
    }

    @AfterMethod
    public void afterMethod1(ITestContext testContext, XmlTest test, ITestResult testResult, Method method, Object[] params) {
        log.info("::AfterMethod1");
    }

    @AfterMethod
    public void afterMethod2(ITestContext testContext, XmlTest test, ITestResult testResult, Method method, Object[] params) {
        log.info("::AfterMethod2");
    }

    @Test
    public void test() {
        log.info("::test()");
    }
}
