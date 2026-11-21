package com.novaprofiles.app.util

import android.webkit.WebSettings
import androidx.webkit.UserAgentMetadata
import androidx.webkit.UserAgentMetadata.BrandVersion
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * Sec-CH-UA brand hints (v2.8.16): the brands a page reads through
 * navigator.userAgentData must agree with the profile UA header.
 *
 * DDG's RealClientBrandHintProvider only RENAMES brands (DuckDuckGo /
 * Google Chrome / WebView) and keeps the WebView's own version numbers
 * — so while v2.8.13 made the UA header say Chrome 151, the brand
 * hints still said 157 (and could say "DuckDuckGo" when DDG's remote
 * branding flag is on). A page comparing the two sees a mismatch.
 *
 * This runs right AFTER every DDG setDefault/setOn call and rewrites
 * the list: Chrome/Chromium brands get the profile's Chrome version
 * (major + full, e.g. 151 / 151.0.0.0), any DuckDuckGo brand becomes
 * Google Chrome, grease brands (Not-A.Brand) are left alone, and
 * platform/model/mobile are kept from the WebView. Fail-closed: no
 * profile UA, no Chrome token in it, or no metadata support — nothing
 * is changed. It never triggers a reload (it only sets metadata).
 */
object ClientHints {
    fun alignToProfile(settings: WebSettings?) {
        if (settings == null) return
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) return
        val userAgent = NovaProfileSession.profileUserAgent() ?: return
        val match = Regex("""Chrome/(\d+)\.(\d+)\.(\d+)\.(\d+)""").find(userAgent) ?: return
        val major = match.groupValues[1]
        val full = "${match.groupValues[1]}.${match.groupValues[2]}.${match.groupValues[3]}.${match.groupValues[4]}"
        val metadata = WebSettingsCompat.getUserAgentMetadata(settings)
        val brands = metadata.brandVersionList.map { brand ->
            when {
                brand.brand.contains("DuckDuckGo") -> BrandVersion.Builder()
                    .setBrand("Google Chrome")
                    .setFullVersion(full)
                    .setMajorVersion(major)
                    .build()
                brand.brand.contains("Chrome") || brand.brand.contains("Chromium") -> BrandVersion.Builder()
                    .setBrand(brand.brand)
                    .setFullVersion(full)
                    .setMajorVersion(major)
                    .build()
                else -> brand
            }
        }
        val aligned = UserAgentMetadata.Builder()
            .setPlatform(metadata.platform)
            .setPlatformVersion(metadata.platformVersion)
            .setFullVersion(full)
            .setModel(metadata.model)
            .setBitness(metadata.bitness)
            .setArchitecture(metadata.architecture)
            .setWow64(metadata.isWow64)
            .setMobile(metadata.isMobile)
            .setBrandVersionList(brands)
            .build()
        WebSettingsCompat.setUserAgentMetadata(settings, aligned)
    }
}
