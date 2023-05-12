package org.mpashka.test.jaxb;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestCollapse {
    private CollapsedStringAdapter adapter = new CollapsedStringAdapter();

    @Test
    public void test() {
//        assertThat(adapter.unmarshal("hello"), is("   "));
//        assertThat(adapter.unmarshal("   "), is("   "));
        assertThat(adapter.unmarshal("   he   llo      "), is("   "));

    }
}
