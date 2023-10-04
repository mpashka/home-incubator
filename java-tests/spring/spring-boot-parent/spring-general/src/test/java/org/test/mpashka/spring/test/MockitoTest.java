package org.test.mpashka.spring.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        MockitoTest.MyBean.class
})
@Slf4j
public class MockitoTest {

    @Autowired
    private MyBean myBean;

    @MockBean
    private MyService myService;

    @Test
    public void test() {
        Mockito.when(myService.myMethod(1)).thenReturn(2);
        int out = myBean.myMethod(1);
        assertThat(out, is(2));
//        Mockito.verify(myService, )
        Mockito.verifyNoMoreInteractions(myService);
    }

    @Service
    public static class MyBean {

        private final MyService myService;

        @Autowired
        public MyBean(MyService myService) {
            this.myService = myService;
        }

        public int myMethod(int in) {
            int out = myService.myMethod(in);
            log.info("Core In: {} -> {}", in, out);
            return out;
        }
    }

    @Service
    public static class MyService {
        public int myMethod(int in) {
            log.info("Service In: {}", in);
            return in+1;
        }
    }
}
