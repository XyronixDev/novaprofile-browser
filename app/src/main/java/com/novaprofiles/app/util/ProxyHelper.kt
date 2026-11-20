package com.novaprofiles.app.util

import android.webkit.WebView
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.novaprofiles.app.data.Profile
import java.net.URLEncoder
import java.util.concurrent.Executor

/**
 * Per-profile proxy via androidx.webkit ProxyController.
 *
 * Proxy override is app-wide in the system WebView (not per-WebView), so
 * NovaProfile applies the running profile's proxy on launch and clears it
 * on stop — only one profile runs at a time, which matches the product
 * rule.
 *
 * Proxy auth travels INSIDE the proxy URL ("socks://user:pass@host:port")
 * — Chromium parses the userinfo and uses it for the SOCKS5 handshake.
 * The old idea (answer auth in onReceivedHttpAuthRequest) can never work
 * for SOCKS: that callback only fires for HTTP 407 auth, while SOCKS5
 * auth happens at the handshake, before any HTTP — without credentials
 * the server rejects the connection and the page dies with
 * ERR_SOCKS_CONNECTION_FAILED, even though the form's Java-level test
 * passed (java.net.Authenticator does SOCKS auth). The 407 handler in
 * BrowserActivity stays as the HTTP fallback.
 *
 * Supported modes: NONE, HTTP, SOCKS5. A proxy check is a plain HTTPS
 * fetch of the user's check URL done with the same proxy settings.
 */
object ProxyHelper {

    fun isSupported(): Boolean =
        WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)

    fun apply(profile: Profile, executor: Executor, onDone: (Boolean) -> Unit) {
        if (!isSupported()) {
            onDone(false)
            return
        }
        if (profile.proxyMode == "NONE" || profile.proxyHost.isBlank()) {
            clear(executor, onDone)
            return
        }
        val scheme = when (profile.proxyMode) {
            "SOCKS5" -> "socks"
            else -> "http"
        }
        // v2.8.9: credentials go in the URL — without them an authenticated
        // SOCKS5 proxy rejects the WebView's handshake outright.
        val userInfo = if (profile.proxyUser.isNotBlank()) {
            "${encodeUserInfo(profile.proxyUser)}:${encodeUserInfo(profile.proxyPass)}@"
        } else ""
        val proxyUrl = "$scheme://$userInfo${profile.proxyHost}:${profile.proxyPort}"
        val config = ProxyConfig.Builder()
            .addProxyRule(proxyUrl)
            .addBypassRule("localhost")
            .addBypassRule("127.0.0.1")
            .build()
        try {
            ProxyController.getInstance().setProxyOverride(
                config, executor
            ) { onDone(true) }
        } catch (e: Exception) {
            onDone(false)
        }
    }

    /** URL-encode one half of proxy userinfo (URLEncoder's "+" is not valid here). */
    private fun encodeUserInfo(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    fun clear(executor: Executor, onDone: (Boolean) -> Unit = {}) {
        if (!isSupported()) {
            onDone(false)
            return
        }
        try {
            ProxyController.getInstance().clearProxyOverride(executor) { onDone(true) }
        } catch (e: Exception) {
            onDone(false)
        }
    }

    /** Human-readable summary for the profile card. */
    fun summary(profile: Profile): String = when (profile.proxyMode) {
        "NONE" -> "No proxy"
        "HTTP" -> "HTTP ${profile.proxyHost}:${profile.proxyPort}"
        "SOCKS5" -> "SOCKS5 ${profile.proxyHost}:${profile.proxyPort}"
        else -> profile.proxyMode
    }

    /** Placeholder that keeps a WebView reference alive for lint; unused. */
    @Suppress("unused")
    fun warmUp(webView: WebView) {
        // No-op: WebView is created by the caller after the data-dir suffix
        // is set (see BrowserActivity).
    }
}
