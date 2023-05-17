package org.test.mpashka.spring.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
public class TestSpring {

    @Autowired
    private MyProperties myProperties;

    @Test
    public void test() {
        assertThat(myProperties.getProp1(), is("my-prop-1"));
    }
}
