package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

@Slf4j
public class MyListenerTest3_IT {
    @BeforeTest
    public void beforeMethod() {
        log.info("::BeforeMethod");
    }

    @AfterTest
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
}
