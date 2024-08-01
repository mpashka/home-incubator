package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import static org.testng.Assert.fail;

@Slf4j
//@Listeners({TestListener.class})
public class MyListenerTest extends MyTestBase {
    @BeforeTest
    public void beforeTest() {
        log.info("::BeforeTest");
    }

    @AfterTest
    public void afterTest(ITestContext testContext, XmlTest test) {
        log.info("::AfterTest");
    }

    @BeforeClass
    public void init() {
        log.info("::BeforeClass");
    }

    @AfterClass
    public void tearDown(ITestContext testContext, XmlTest test) {
        log.info("::AfterClass");
    }

    @BeforeMethod
    public void beforeMethod() {
        log.info("::BeforeMethod");
    }

    @AfterMethod
    public void afterMethod(ITestContext testContext, XmlTest test) {
        log.info("::AfterMethod");
    }

    @Test
    public void test1() {
        log.info("::test1");
    }

    @Test
    public void test2() {
        log.info("::test2");
    }

    @Test
    public void testFail() {
        log.info("::testFail");
        fail("Fail");
    }
}
