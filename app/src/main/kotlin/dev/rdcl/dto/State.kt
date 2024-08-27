package dev.rdcl.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

data class State(
    @JacksonXmlText
    val state: String,
) {
    // https://github.com/FasterXML/jackson-dataformat-xml/issues/615
    @JsonCreator
    constructor(map: Map<String, String>) : this(
        state = map[""]!!,
    )
}
