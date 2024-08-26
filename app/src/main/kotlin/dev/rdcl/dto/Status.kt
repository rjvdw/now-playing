package dev.rdcl.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.rdcl.deserializers.BitToBooleanDeserializer
import dev.rdcl.deserializers.RepeatStatusDeserializer

data class Status(

    val etag: String,

    val album: String?,

    val artist: String?,

    val image: String?,

    @JsonDeserialize(using = BitToBooleanDeserializer::class)
    val mute: Boolean?,

    val name: String?,

    @JsonDeserialize(using = RepeatStatusDeserializer::class)
    val repeat: RepeatStatus?,

    val secs: Int?,

    @JsonDeserialize(using = BitToBooleanDeserializer::class)
    val shuffle: Boolean?,

    val song: Int?,

    val state: String?,

    val title1: String?,

    val title2: String?,

    val title3: String?,

    @JsonProperty("totlen")
    val totalLength: Int?,

    val volume: Int?,
) {
    fun getTitle(separator: String = " - ") = listOfNotNull(title1, title2, title3).joinToString(separator)

    fun isPlaying() = state == "play" || state == "stream"
}
