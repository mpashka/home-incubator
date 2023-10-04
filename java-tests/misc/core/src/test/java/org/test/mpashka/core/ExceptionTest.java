package org.test.mpashka.core;

public class ExceptionTest {

    public void method1() throws MyThrowable1 {
        throw new MyThrowable1();
    }

    public static class MyThrowable1 extends Throwable {

    }
}
