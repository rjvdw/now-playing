package dev.rdcl.dto

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Playlist(

    val id: Int,

    val length: Int,

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "song")
    val songs: List<PlaylistEntry>,
)
