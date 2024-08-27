package dev.rdcl.nowplaying.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaylistEntry(

    val id: Int,

    val title: String?,

    @JsonProperty("art")
    val artist: String?,

    @JsonProperty("alb")
    val album: String?,

    val composer: String?,
)
