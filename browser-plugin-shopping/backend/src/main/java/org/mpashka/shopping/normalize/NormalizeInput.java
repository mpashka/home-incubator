package org.mpashka.shopping.normalize;

/** A capture ready for LLM extraction, decoupled from the JPA entity (no open tx needed). */
public record NormalizeInput(String id, String marketplace, String kind, String searchQuery,
                             String payloadJson) {
}
