package com.novaprofiles.app.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL

/**
 * The device's own public connection, detected live. A Direct profile has
 * no proxy, so its country can only come from the connection itself —
 * never from the profile language, which defaults to en-US and would
 * paint every direct profile with the US flag.
 */
data class DirectInfo(
    val ip: String,
    val countryCode: String,
    val countryName: String,
    val city: String,
    val timezone: String
)

object DirectConnection {
    private const val PREFS = "novaprofile_direct"
    private const val IPIFY_URL = "https://api.ipify.org"
    private const val GEO_URL = "http://ip-api.com/json/%s?fields=status,country,countryCode,city,timezone"

    fun cached(context: Context): DirectInfo? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ip = prefs.getString("ip", "").orEmpty()
        val code = prefs.getString("countryCode", "").orEmpty()
        val name = prefs.getString("countryName", "").orEmpty()
        if (ip.isBlank() || code.length != 2 || name.isBlank()) return null
        return DirectInfo(
            ip = ip,
            countryCode = code,
            countryName = name,
            city = prefs.getString("city", "").orEmpty(),
            timezone = prefs.getString("timezone", "").orEmpty()
        )
    }

    /**
     * Detects the public IP and its country. Both calls open with
     * Proxy.NO_PROXY, so they always measure the device's own connection —
     * a running profile's WebView proxy override never touches them.
     * Returns null when the network or the lookup fails; the caller shows
     * Unknown and offers a retry instead of inventing a country.
     */
    suspend fun detect(context: Context): DirectInfo? = withContext(Dispatchers.IO) {
        try {
            val ip = readText(IPIFY_URL).trim()
            if (ip.isBlank() || !ip.contains('.')) return@withContext null
            val json = JSONObject(readText(GEO_URL.format(ip)))
            if (json.optString("status") != "success") return@withContext null
            val code = json.optString("countryCode").uppercase()
            val name = json.optString("country")
            if (code.length != 2 || name.isBlank()) return@withContext null
            val info = DirectInfo(
                ip = ip,
                countryCode = code,
                countryName = name,
                city = json.optString("city"),
                timezone = json.optString("timezone")
            )
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("ip", info.ip)
                .putString("countryCode", info.countryCode)
                .putString("countryName", info.countryName)
                .putString("city", info.city)
                .putString("timezone", info.timezone)
                .apply()
            info
        } catch (e: Exception) {
            null
        }
    }

    private fun readText(url: String): String {
        val connection = URL(url).openConnection(Proxy.NO_PROXY) as HttpURLConnection
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        return try {
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }
}
