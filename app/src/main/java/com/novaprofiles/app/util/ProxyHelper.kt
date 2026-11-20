package com.novaprofiles.app.util

import android.webkit.WebView
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.novaprofiles.app.data.Profile
import java.util.concurrent.Executor

/**
 * Per-profile proxy via androidx.webkit ProxyController.
 *
 * Proxy override is app-wide in the system WebView (not per-WebView), so
 * NovaProfile applies the running profile's proxy on launch and clears it
 * on stop — only one profile runs at a time, which matches the product
 * rule.
 *
 * AUTHENTICATED PROXIES GO THROUGH HTTP IN THE WEBVIEW (v2.8.10).
 * WebView's ProxyConfig format is "[scheme://]host[:port]" — there is NO
 * slot for a username/password, and Chromium rejects a proxy URL that
 * carries credentials: v2.8.9 tried "socks://user:pass@host:port" and
 * setProxyOverride failed on the phone ("Proxy apply nahi hua", browser
 * never opened). SOCKS5 auth has no API in WebView at all. So when the
 * profile has credentials, the browser override uses "http://host:port"
 * and the credentials are supplied by BrowserActivity's WebViewClient
 * (onReceivedHttpAuthRequest answers the proxy's HTTP 407) — that is
 * WebView's official auth path. Webshare serves HTTP on the same port as
 * SOCKS5, so the traffic still goes through the same server. A SOCKS5
 * profile WITHOUT credentials keeps the "socks" scheme. The Java-level
 * proxy check (ProxyStore) still tests with the profile's real mode,
 * because java.net.Authenticator does SOCKS auth properly.
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
        // v2.8.10: credentials exist → HTTP scheme, always. The WebView
        // cannot do authenticated SOCKS5 (see the class KDoc); the 407
        // handler in BrowserActivity supplies the credentials.
        val scheme = if (profile.proxyUser.isNotBlank()) {
            "http"
        } else when (profile.proxyMode) {
            "SOCKS5" -> "socks"
            else -> "http"
        }
        val proxyUrl = "$scheme://${profile.proxyHost}:${profile.proxyPort}"
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
