package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

@Slf4j
@Listeners({TestListener.class})
public class MyTest {

    @BeforeClass
    public void init() {
        log.info("MyTest.Before class");
    }

    @AfterClass
    public void tearDown(ITestContext testContext, XmlTest test) {
        log.info("MyTest.After class");
    }

    @Test
    public void test() {
        log.info("MyTest.test() Must be ok");
    }
}
