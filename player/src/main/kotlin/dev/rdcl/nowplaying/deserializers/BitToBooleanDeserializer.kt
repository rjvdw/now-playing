package dev.rdcl.nowplaying.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class BitToBooleanDeserializer : JsonDeserializer<Boolean?>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?) =
        if (p?.currentTokenId() == JsonTokenId.ID_STRING) {
            when (p.text) {
                "0" -> false
                "1" -> true
                else -> null
            }
        } else {
            null
        }
}
