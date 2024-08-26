package dev.rdcl.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.rdcl.deserializers.BitToBooleanDeserializer

data class SyncStatus(

    val etag: String,

    val brand: String?,

    val group: String?,

    val icon: String?,

    val id: String?,

    @JsonDeserialize(using = BitToBooleanDeserializer::class)
    val initialized: Boolean?,

    val mac: String?,

    val model: String?,

    val modelName: String?,

    @JsonDeserialize(using = BitToBooleanDeserializer::class)
    val mute: Boolean?,

    val name: String?,

    val schemaVersion: String?,
)
