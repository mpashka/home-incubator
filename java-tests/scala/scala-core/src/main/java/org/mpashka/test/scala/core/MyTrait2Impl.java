package org.mpashka.test.scala.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTrait2Impl implements MyTrait2 {
    private static final Logger log = LoggerFactory.getLogger(MyTrait2Impl.class);

    public void myVar_$eq(String myVar) {
        log.info("MyTrait2Impl::myVar_$eq {}", myVar);
    }

    public String myVar() {
        log.info("MyTrait2Impl::myVar");
        return null;
    }
}
