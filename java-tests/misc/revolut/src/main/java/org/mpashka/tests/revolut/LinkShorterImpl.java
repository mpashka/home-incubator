package org.mpashka.tests.revolut;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LinkShorterImpl implements LinkShorterAPI {

    public static final RandomGenerator DEFAULT_RANDOM_GENERATOR = max -> (int) Math.round(Math.random() * max);

    public static final PoolGenerator DEFAULT_POOL_GENERATOR = new PoolGenerator() {
        @Override
        public List<URI> shortLinks(int size) {
            List<URI> shortLinks = new ArrayList<>(size);
            for (int i = 0; i < MAX_LINKS; i++) {
                shortLinks.add(URI.create("https://my.com/" + i));
            }
            return shortLinks;
        }
    };
    
    private static final int MAX_LINKS = 100;
    
    private final Map<URI, URI> links = new HashMap<>();
    private final List<URI> shortLinks;
    private final RandomGenerator randomGenerator;

    public LinkShorterImpl(int size, PoolGenerator poolGenerator, RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
        this.shortLinks = new ArrayList<>(poolGenerator.shortLinks(size));
    }

    @Override
    public URI addLink(URI longLink) throws IllegalStateException {
        Objects.requireNonNull(longLink);
        if (shortLinks.isEmpty()) {
            throw new IllegalStateException("Max links reached");
        }
        URI shortLink = shortLinks.remove(randomGenerator.random(shortLinks.size()));
        links.put(shortLink, longLink);
        return shortLink;
    }

    @Override
    public URI getLink(URI shortLink) throws IllegalArgumentException {
        Objects.requireNonNull(shortLink);
        URI longLink = links.get(shortLink);
        if (longLink == null) {
            throw new IllegalArgumentException("Short link not found " + shortLink);
        }
        return longLink;
    }

    interface RandomGenerator {
        int random(int max);
    }

    interface PoolGenerator {
        List<URI> shortLinks(int size);
    }
}
