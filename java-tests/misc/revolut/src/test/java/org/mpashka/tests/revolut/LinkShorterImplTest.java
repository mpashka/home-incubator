package org.mpashka.tests.revolut;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LinkShorterImplTest {

    private MockRandomGenerator randomGeneratorMock;
    private MockPoolGenerator poolGeneratorMock;
    private int random;
    private int randomMax;
    private List<URI> pool;

    @BeforeEach
    public void init() {
        randomGeneratorMock = new MockRandomGenerator();
        poolGeneratorMock = new MockPoolGenerator();
    }
    
    @Test
    public void testNulls() {
        LinkShorterAPI linkShorter = new LinkShorterImpl(10, poolGeneratorMock, randomGeneratorMock);
        try {
            linkShorter.addLink(null);
            fail();
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    @Test
    public void testNormal() {
        pool = List.of(URI.create("https://www.com/1"), URI.create("https://www.com/2"), URI.create("https://www.com/3"));
        LinkShorterAPI linkShorter = new LinkShorterImpl(3, poolGeneratorMock, randomGeneratorMock);

        random = 2;
        URI longLink1 = URI.create("https://www.com/long_link1");
        URI shortLink1 = linkShorter.addLink(longLink1);
        assertEquals(randomMax, 3);
        assertEquals(shortLink1, URI.create("https://www.com/3"));

        random = 0;
        URI longLink2 = URI.create("https://www.com/long_link2");
        URI shortLink2 = linkShorter.addLink(longLink2);
        assertEquals(randomMax, 2);
        assertEquals(shortLink2, URI.create("https://www.com/1"));
    }
    
    @Test
    public void testAbsent() {
        LinkShorterAPI linkShorter = new LinkShorterImpl(10, poolGeneratorMock, randomGeneratorMock);
        URI shortLink = URI.create("https://www.com/short");
        try {
            linkShorter.getLink(shortLink);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
    
    @Test
    public void testMoreThan100() {
        LinkShorterAPI linkShorter = new LinkShorterImpl(10, poolGeneratorMock, randomGeneratorMock);
        for (int i = 0; i < 100; i++) {
            URI shortLink = URI.create("https://www.com/short" + i);
            URI longLink = URI.create("https://www.com/long_link" + i);
            linkShorter.addLink(longLink);
            assertEquals(longLink, linkShorter.getLink(shortLink));
        }
        
        for (int i = 0; i < 100; i++) {
            URI shortLink = URI.create("https://www.com/short" + i);
            URI longLink = URI.create("https://www.com/long_link" + i);
            assertEquals(longLink, linkShorter.getLink(shortLink));
        }

        try {
            linkShorter.addLink(URI.create("https://a.b.com/long_extra"));
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    private final class MockRandomGenerator implements LinkShorterImpl.RandomGenerator {
        @Override
        public int random(int max) {
            randomMax = max;
            return random;
        }
    }

    private final class MockPoolGenerator implements LinkShorterImpl.PoolGenerator {
        @Override
        public List<URI> shortLinks(int size) {
            return pool;
        }
    }
}
