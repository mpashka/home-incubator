package org.test.mpashka.jackson.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SimpleData @JsonCreator constructor(
    @JsonProperty("a")
    val a: String,
    @JsonProperty("b")
    val b: Long,
    @JsonProperty("helloWorld")
    val helloWorld: Int
)
