package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

@Slf4j
public abstract class MyTestBase {

    @BeforeTest
    public void baseBeforeTest() {
        log.info("::base-BeforeTest");
    }

    @AfterTest
    public void baseAfterTest(ITestContext testContext, XmlTest test) {
        log.info("::base-AfterTest");
    }

    @BeforeMethod
    public void baseBeforeMethod() {
        log.info("::base-BeforeMethod");
    }

    @AfterMethod
    public void baseAfterMethod(ITestContext testContext, XmlTest test) {
        log.info("::base-AfterMethod");
    }

    @Test
    public void baseTest1() {
        log.info("::base-test1");
    }
}
