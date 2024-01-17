package com.braintreepayments.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BrowserSwitchRequestSerializer : KSerializer<BrowserSwitchRequest> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BrowserSwitchRequest", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BrowserSwitchRequest {
        return BrowserSwitchRequest.fromJson(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: BrowserSwitchRequest) {
        encoder.encodeString(value.toJson())
    }
}