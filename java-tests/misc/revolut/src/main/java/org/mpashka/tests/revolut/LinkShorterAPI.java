package org.mpashka.tests.revolut;

import java.net.URI;

public interface LinkShorterAPI {
    /**
     *
     * @param longLink
     * @throws NullPointerException in case of
     * @throws IllegalStateException in case of
     */
    URI addLink(URI longLink) throws IllegalStateException;

    URI getLink(URI shortLink) throws IllegalArgumentException;
}
