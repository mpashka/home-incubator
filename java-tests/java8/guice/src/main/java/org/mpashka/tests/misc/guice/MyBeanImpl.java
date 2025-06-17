package org.mpashka.tests.misc.guice;

public class MyBeanImpl implements MyBean {
    private final String data;

    public MyBeanImpl(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MyBeanImpl{" +
                "hash=" + hashCode() +
                ", data='" + data + '\'' +
                '}';
    }
}
