package com.novaprofiles.app.util

import android.content.Context
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.novaprofiles.app.data.Profile
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * The running profile, shared between the NovaProfile home screen and
 * DuckDuckGo's browser (which is a separate activity in this same app).
 *
 * The home activity sets the profile before launching the browser; the
 * browser's WebViewClient reads it for the per-page fingerprint script
 * and for proxy auth. The id is also persisted so a later launch can
 * find it again. The proxy override itself is process-wide and does not
 * survive a process restart — after a restart the browser runs without
 * a profile (no proxy, no fingerprint) until the user runs a profile
 * from the home screen again.
 */
object NovaProfileSession {
    const val STOP_PROFILE_ACTION = "com.novaprofiles.app.STOP_PROFILE"
    const val EXTRA_PROFILE_ID = "profile_id"

    private const val PREFS = "novaprofile_session"
    private const val KEY_RUNNING_PROFILE_ID = "running_profile_id"

    @Volatile
    private var cachedProfile: Profile? = null

    fun setRunningProfile(context: Context, profile: Profile) {
        cachedProfile = profile
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_RUNNING_PROFILE_ID, profile.id).apply()
    }

    fun currentProfile(): Profile? = cachedProfile

    fun runningProfileId(context: Context): Long? {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_RUNNING_PROFILE_ID, -1L)
        return if (id > 0) id else null
    }

    fun clearRunning(context: Context) {
        cachedProfile = null
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_RUNNING_PROFILE_ID).apply()
    }

    /**
     * Prepare a profile to run in THIS process before the browser opens:
     * per-profile WebView data directory + proxy override, waited.
     * (v2.7.1 lesson: a WebView created before the override lands leaks
     * the real IP.) Cookies live in the per-profile WebView dir (suffix),
     * so no Room cookie restore — restoring the stale snapshot would
     * clobber the newer cookies already in the dir. Returns false when a
     * configured proxy could not be applied — the caller must NOT open
     * the browser direct (fail-closed).
     */
    suspend fun prepare(context: Context, profile: Profile): Boolean {
        try {
            WebView.setDataDirectorySuffix("p${profile.id}")
        } catch (e: Exception) {
            // Suffix already set by an earlier profile in this process:
            // storage is shared. Same known limit as NovaProfile v2.x.
        }
        val applied = suspendCancellableCoroutine { cont ->
            ProxyHelper.apply(
                profile,
                ContextCompat.getMainExecutor(context)
            ) { ok ->
                if (cont.isActive) cont.resume(ok)
            }
        }
        val needsProxy = profile.proxyMode != "NONE" && profile.proxyHost.isNotBlank()
        if (needsProxy && !applied) return false
        setRunningProfile(context, profile)
        return true
    }
}
