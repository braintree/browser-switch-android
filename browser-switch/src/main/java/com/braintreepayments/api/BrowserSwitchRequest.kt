package com.braintreepayments.api

import android.net.Uri
import kotlinx.serialization.Serializable
import org.json.JSONException
import org.json.JSONObject

@Serializable(with = BrowserSwitchRequestSerializer::class)
class BrowserSwitchRequest internal constructor(
    val requestCode: Int,
    val url: Uri,
    val metadata: JSONObject?,
    private val returnUrlScheme: String,
    var shouldNotifyCancellation: Boolean
) {

    @Throws(JSONException::class)
    fun toJson(): String {
        val result = JSONObject()
        result.put("requestCode", requestCode)
        result.put("url", url.toString())
        result.put("returnUrlScheme", returnUrlScheme)
        result.put("shouldNotify", shouldNotifyCancellation)
        if (metadata != null) {
            result.put("metadata", metadata)
        }
        return result.toString()
    }

    fun matchesDeepLinkUrlScheme(url: Uri): Boolean {
        return url.scheme != null && url.scheme.equals(returnUrlScheme, ignoreCase = true)
    }

    companion object {
        @Throws(JSONException::class)
        fun fromJson(json: String?): BrowserSwitchRequest {
            val jsonObject = JSONObject(json)
            val requestCode = jsonObject.getInt("requestCode")
            val url = jsonObject.getString("url")
            val returnUrlScheme = jsonObject.getString("returnUrlScheme")
            val metadata = jsonObject.optJSONObject("metadata")
            val shouldNotify = jsonObject.optBoolean("shouldNotify", true)
            return BrowserSwitchRequest(
                requestCode,
                Uri.parse(url),
                metadata,
                returnUrlScheme,
                shouldNotify
            )
        }
    }
}