package dev.rdcl.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import dev.rdcl.deserializers.BitToBooleanDeserializer

data class Volume(

    @JsonDeserialize(using = BitToBooleanDeserializer::class)
    val mute: Boolean,

    @JacksonXmlText
    val level: Int,
) {
    // https://github.com/FasterXML/jackson-dataformat-xml/issues/615
    @JsonCreator
    constructor(map: Map<String, String>) : this(
        mute = map["mute"]!! == "1",
        level = map[""]!!.toInt(),
    )
}
