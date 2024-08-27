package dev.rdcl.nowplaying.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dev.rdcl.nowplaying.dto.RepeatStatus

class RepeatStatusDeserializer : JsonDeserializer<RepeatStatus?>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?) =
        if (p?.currentTokenId() == JsonTokenId.ID_STRING) {
            when (p.text) {
                "0" -> RepeatStatus.QUEUE
                "1" -> RepeatStatus.TRACK
                "2" -> RepeatStatus.OFF
                else -> null
            }
        } else {
            null
        }
}
