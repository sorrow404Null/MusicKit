package com.eclipse.music.kit.utils.ncm.data

import kotlinx.serialization.Serializable

@Serializable
data class NCMMetadata(
    val musicName: String,
    @Serializable(with = ArtistNameListSerializer::class)
    val artist: List<String>,
    val album: String,
    val musicId: Long,
    val format: String
)