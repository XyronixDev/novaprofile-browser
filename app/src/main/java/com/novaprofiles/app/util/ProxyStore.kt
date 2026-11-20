package com.novaprofiles.app.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.Authenticator
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.URL
import java.util.UUID

/**
 * Proxy Manager storage (v2.0 Pro): the user's own proxies, kept locally
 * in SharedPreferences as JSON. No Room change, no migration, no data loss.
 *
 * A proxy check opens https://api.ipify.org THROUGH the proxy (real IP the
 * sites would see), then looks up that IP's country via ip-api.com, and
 * times the round trip.
 */
data class ProxyEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mode: String, // HTTP or SOCKS5
    val host: String,
    val port: Int,
    val user: String = "",
    val pass: String = "",
    val country: String = "",
    val lastIp: String = "",
    val lastMs: Long = 0,
    val ok: Boolean = false,
    // v2.5: the IP's real timezone from ip-api.com (e.g. a Seattle proxy
    // reports America/Los_Angeles, not the country default New York).
    // Profiles take this over the country default so timezone == IP.
    val lastTimezone: String = "",
    // v2.6: the IP's coordinates (Mountain View for the Google Cloud
    // free proxies) — the profile's geolocation spoof target.
    val lastLat: Double = 0.0,
    val lastLon: Double = 0.0
)

class ProxyStore(context: Context) {
    private val prefs = context.getSharedPreferences("novaprofile_proxies", Context.MODE_PRIVATE)

    private val _proxies = MutableStateFlow(load())
    val proxies: StateFlow<List<ProxyEntry>> = _proxies

    private fun load(): List<ProxyEntry> {
        return try {
            val raw = prefs.getString("list", "[]") ?: "[]"
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                ProxyEntry(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    name = o.optString("name"),
                    mode = o.optString("mode", "HTTP"),
                    host = o.optString("host"),
                    port = o.optInt("port", 8080),
                    user = o.optString("user"),
                    pass = o.optString("pass"),
                    country = o.optString("country"),
                    lastIp = o.optString("lastIp"),
                    lastMs = o.optLong("lastMs"),
                    ok = o.optBoolean("ok"),
                    lastTimezone = o.optString("lastTimezone"),
                    lastLat = o.optDouble("lastLat", 0.0),
                    lastLon = o.optDouble("lastLon", 0.0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persist(list: List<ProxyEntry>) {
        // v2.7.2 safety net: drop any host:port duplicates already on
        // the phone (keep the last entry for each), so old duplicates
        // disappear on the next write even if they predate add()'s fix.
        val deduped = list.reversed().distinctBy { it.host to it.port }.reversed()
        val arr = JSONArray()
        for (p in deduped) {
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("mode", p.mode)
                    put("host", p.host)
                    put("port", p.port)
                    put("user", p.user)
                    put("pass", p.pass)
                    put("country", p.country)
                    put("lastIp", p.lastIp)
                    put("lastMs", p.lastMs)
                    put("ok", p.ok)
                    put("lastTimezone", p.lastTimezone)
                    put("lastLat", p.lastLat)
                    put("lastLon", p.lastLon)
                }
            )
        }
        prefs.edit().putString("list", arr.toString()).apply()
        _proxies.value = deduped
    }

    // v2.7.2: same proxy picked twice must not stack up as two saved
    // entries (the phone showed 3x "Free IN 10"). Adding a host:port
    // that is already saved replaces it in place with the fresh test
    // result instead of appending a duplicate.
    fun add(entry: ProxyEntry) {
        val existing = _proxies.value.firstOrNull { it.host == entry.host && it.port == entry.port }
        if (existing != null) {
            persist(_proxies.value.map { if (it.id == existing.id) entry.copy(id = existing.id) else it })
        } else {
            persist(_proxies.value + entry)
        }
    }

    fun remove(id: String) {
        persist(_proxies.value.filter { it.id != id })
    }

    fun update(entry: ProxyEntry) {
        persist(_proxies.value.map { if (it.id == entry.id) entry else it })
    }

    /** Live check through the proxy. Returns the entry with fresh results. */
    suspend fun check(entry: ProxyEntry): ProxyEntry = withContext(Dispatchers.IO) {
        // v2.8.4: SOCKS5 username/password auth needs java.net.Authenticator.
        // The Proxy-Authorization header only authenticates HTTP proxies, so
        // every authenticated SOCKS5 proxy (e.g. Webshare) failed this test
        // even with the right host:port. Installed only for this check and
        // cleared afterwards — nothing else in the app installs one, and the
        // manual form tests sequentially, so the window is one test at a time.
        val needsAuth = entry.user.isNotBlank()
        if (needsAuth) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication? {
                    return if (requestingHost == entry.host) {
                        PasswordAuthentication(entry.user, entry.pass.toCharArray())
                    } else {
                        null
                    }
                }
            })
        }
        try {
            val started = System.currentTimeMillis()
            try {
            val type = if (entry.mode == "SOCKS5") Proxy.Type.SOCKS else Proxy.Type.HTTP
            val proxy = Proxy(type, InetSocketAddress(entry.host, entry.port))
            val conn = URL("https://api.ipify.org").openConnection(proxy) as HttpURLConnection
            // v2.3: 6s — a proxy slower than this to answer ipify is
            // unusable for browsing anyway, and parallel batches must
            // resolve fast.
            conn.connectTimeout = 6_000
            conn.readTimeout = 6_000
            if (entry.user.isNotBlank()) {
                val creds = android.util.Base64.encodeToString(
                    "${entry.user}:${entry.pass}".toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP
                )
                conn.setRequestProperty("Proxy-Authorization", "Basic $creds")
            }
            val ip = conn.inputStream.bufferedReader().readText().trim()
            conn.disconnect()
            if (conn.responseCode != 200 || ip.isBlank() || !ip.contains(".")) {
                return@withContext entry.copy(ok = false, lastMs = System.currentTimeMillis() - started)
            }
            // Country + timezone + coordinates lookup for that IP (also
            // through the proxy). The timezone is the v2.5 anti-detect
            // fix: the IP's own zone (e.g. America/Los_Angeles for
            // Mountain View) must be the profile's zone, not the country
            // capital's. Coordinates feed the v2.6 geolocation spoof.
            var country = ""
            var tz = ""
            var lat = 0.0
            var lon = 0.0
            try {
                val c2 = URL("http://ip-api.com/json/$ip?fields=status,country,countryCode,timezone,lat,lon")
                    .openConnection(proxy) as HttpURLConnection
                c2.connectTimeout = 6_000
                c2.readTimeout = 6_000
                val body = c2.inputStream.bufferedReader().readText()
                c2.disconnect()
                val o = JSONObject(body)
                if (o.optString("status") == "success") {
                    country = "${o.optString("countryCode")} • ${o.optString("country")}"
                    tz = o.optString("timezone")
                    lat = o.optDouble("lat", 0.0)
                    lon = o.optDouble("lon", 0.0)
                }
            } catch (e: Exception) {
            }
            entry.copy(
                ok = true,
                country = country,
                lastIp = ip,
                lastMs = System.currentTimeMillis() - started,
                lastTimezone = tz,
                lastLat = lat,
                lastLon = lon
            )
        } catch (e: Exception) {
            entry.copy(ok = false, lastMs = System.currentTimeMillis() - started)
        }
        } finally {
            if (needsAuth) Authenticator.setDefault(null)
        }
    }

    /**
     * v2.6 smart 90% fix: ONE ip-api call through the proxy, returning the
     * proxy IP's current (ip, timezone, lat, lon). Run time calls this
     * before opening the browser, so an old profile — or a free proxy
     * whose IP rotated — never runs with a stale timezone: the free US
     * proxies sit in Mountain View (America/Los_Angeles), while the
     * country default is New York, a 3-hour mismatch whoer deducts for.
     * Returns null on any failure; the caller then launches with the
     * profile's stored values instead of blocking.
     */
    data class Location(val ip: String, val timezone: String, val lat: Double, val lon: Double)

    suspend fun resolveLocation(entry: ProxyEntry): Location? =
        withContext(Dispatchers.IO) {
            try {
                val proxy = Proxy(
                    if (entry.mode == "SOCKS5") Proxy.Type.SOCKS else Proxy.Type.HTTP,
                    InetSocketAddress(entry.host, entry.port)
                )
                val c = URL("http://ip-api.com/json/?fields=status,query,timezone,lat,lon")
                    .openConnection(proxy) as HttpURLConnection
                c.connectTimeout = 6_000
                c.readTimeout = 6_000
                val body = c.inputStream.bufferedReader().readText()
                c.disconnect()
                val o = JSONObject(body)
                if (o.optString("status") != "success") return@withContext null
                val tz = o.optString("timezone")
                if (tz.isBlank()) return@withContext null
                Location(
                    ip = o.optString("query"),
                    timezone = tz,
                    lat = o.optDouble("lat", 0.0),
                    lon = o.optDouble("lon", 0.0)
                )
            } catch (e: Exception) {
                null
            }
        }
}
