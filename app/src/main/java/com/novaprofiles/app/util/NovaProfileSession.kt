package com.novaprofiles.app.util

import android.content.Context
import android.content.Intent
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
    // v2.8.13: ask the browser to re-load the current tab under the
    // running profile (the tab may hold a page loaded under another
    // profile's proxy — stale IP/country on whoer).
    const val EXTRA_RELOAD_TAB = "novaprofile_reload_tab"

    private const val PREFS = "novaprofile_session"
    private const val KEY_RUNNING_PROFILE_ID = "running_profile_id"
    private const val KEY_LAST_TAB_PROFILE_ID = "last_tab_profile_id"
    private const val KEY_TABS_CLEAR_PENDING = "tabs_clear_pending"

    // v2.8.17: after a profile-switch restart, the fresh process
    // auto-runs the profile named here (plus an optional query, e.g.
    // whoer.net from the fingerprint lab).
    const val EXTRA_AUTO_RUN_PROFILE_ID = "novaprofile_auto_run_profile_id"
    const val EXTRA_AUTO_RUN_QUERY = "novaprofile_auto_run_query"

    // Which profile's data dir this process already uses (null until
    // the first prepare() in this process sets one). Android allows
    // exactly one WebView data dir per process.
    @Volatile
    private var suffixProfileId: Long? = null

    enum class PrepareResult { OK, PROXY_FAILED, SUFFIX_FAILED }

    /** The profile whose tabs are stored in the browser's tab list. */
    fun lastTabProfileId(context: Context): Long? {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_TAB_PROFILE_ID, -1L)
        return if (id > 0) id else null
    }

    fun setLastTabProfileId(context: Context, id: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_LAST_TAB_PROFILE_ID, id).apply()
    }

    /** True while a profile switch still owes the browser a tab clear. */
    fun isTabsClearPending(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_TABS_CLEAR_PENDING, false)

    fun setTabsClearPending(context: Context, pending: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_TABS_CLEAR_PENDING, pending).apply()
    }

    /**
     * Restart the whole app process so a profile gets its OWN WebView
     * data dir (own cookies/storage). Android allows exactly one data
     * dir per process, set before the first WebView — once another
     * profile ran in this process, no suffix call can switch it. The
     * launch intent carries the profile id (and an optional query)
     * so the fresh process runs it by itself; the old profile's dir
     * stays on disk and comes back when that profile runs again.
     */
    fun restartAppForProfile(context: Context, profileId: Long, query: String? = null) {
        val restart = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(EXTRA_AUTO_RUN_PROFILE_ID, profileId)
            if (query != null) putExtra(EXTRA_AUTO_RUN_QUERY, query)
        } ?: return
        context.startActivity(restart)
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }

    @Volatile
    private var cachedProfile: Profile? = null

    fun setRunningProfile(context: Context, profile: Profile) {
        cachedProfile = profile
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_RUNNING_PROFILE_ID, profile.id).apply()
    }

    fun currentProfile(): Profile? = cachedProfile

    /**
     * v2.8.13: the HTTP User-Agent header the WebView sends must match the
     * JS-spoofed navigator.userAgent, or a site sees two different
     * browsers (whoer showed "Browser: Chrome 157.0" in the header vs
     * Chrome 151 in JS, and deducted 15% for "Browser headers
     * different"). Returns the profile's UA when a profile is running,
     * else null (plain DDG UA stands).
     */
    fun profileUserAgent(): String? = cachedProfile?.let { p ->
        p.userAgent.ifBlank { Profile.defaultUserAgent(p.userAgentPreset) }
    }

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
     * the real IP.) Returns SUFFIX_FAILED when this process already
     * uses another profile's data dir (or a WebView already exists
     * without one — e.g. after the fingerprint lab's check WebView):
     * Android allows one data dir per process, so the caller must
     * restart the process for this profile instead of sharing the
     * other profile's cookies/storage. The fingerprint lab treats
     * SUFFIX_FAILED as "proceed" — its check WebView reads values
     * only and needs no stored cookies. Cookies live in the
     * per-profile WebView dir (suffix), so no Room cookie restore —
     * restoring the stale snapshot would clobber the newer cookies
     * already in the dir. Returns PROXY_FAILED when a configured
     * proxy could not be applied — the caller must NOT open the
     * browser direct (fail-closed).
     */
    suspend fun prepare(context: Context, profile: Profile): PrepareResult {
        var suffixOk = true
        if (suffixProfileId != profile.id) {
            try {
                WebView.setDataDirectorySuffix("p${profile.id}")
                suffixProfileId = profile.id
            } catch (e: Exception) {
                // Suffix already set by an earlier profile in this
                // process, or a WebView already exists without one:
                // storage is shared with that profile in this process.
                suffixOk = false
            }
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
        if (needsProxy && !applied) return PrepareResult.PROXY_FAILED
        if (!suffixOk) return PrepareResult.SUFFIX_FAILED
        setRunningProfile(context, profile)
        return PrepareResult.OK
    }
}
