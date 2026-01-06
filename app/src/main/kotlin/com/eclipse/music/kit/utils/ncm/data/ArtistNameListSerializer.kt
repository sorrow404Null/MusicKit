package com.eclipse.music.kit.utils.ncm.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object ArtistNameListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        ListSerializer(String.serializer()).descriptor

    override fun deserialize(decoder: Decoder): List<String> =
        (decoder as JsonDecoder).decodeJsonElement().jsonArray.map { it.jsonArray[0].jsonPrimitive.content }

    override fun serialize(encoder: Encoder, value: List<String>) =
        (encoder as JsonEncoder).encodeJsonElement(
            buildJsonArray { value.forEach { name -> add(buildJsonArray { add(name) }) } })
}