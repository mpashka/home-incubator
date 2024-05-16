package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

@Slf4j
public class MyFailTest {

    private static final Context context = new Context();

    @BeforeClass
    public void init() {
        log.info("MyFailTest.Before class");
    }

    @AfterClass
    public void tearDown(ITestContext testContext, XmlTest test) {
        log.info("MyFailTest.After class");
    }

    @BeforeMethod
    public void beforeMethod() {
        log.info("MyFailTest.Before method");
    }

    @AfterMethod
    public void afterMethod(ITestContext testContext, XmlTest test) {
        log.info("MyFailTest.After method");
    }

    @Test
    public void rerun() {
        int iteration = context.iteration++;
        boolean fail = iteration < 4;
        log.info("MyFailTest.rerun. Instance: {}", this);
        log.info("    Iteration: {}. Test fail: {}", iteration, fail);
        if (fail) {
            Assert.fail("Expected fail on iteration " + iteration);
        }
    }

    @Test
    public void rerun2() {
        int iteration = context.iteration2++;
        boolean fail = iteration < 4;
        log.info("MyFailTest.rerun2. 2Iteration: {}. Test fail: {}", iteration, fail);
        if (fail) {
            Assert.fail("2Expected fail on iteration " + iteration);
        }
    }

    @Test
    @Ignore
    public void rerunFail() {
        int iteration = context.iteration++;
        boolean fail = iteration < 100;
        log.info("MyFailTest.rerunFail. Iteration: {}. Test fail: {}", iteration, fail);
        if (fail) {
            Assert.fail("Expected fail on iteration " + iteration);
        }
    }

    private static class Context {
        private int iteration;
        private int iteration2;
    }
}
