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
    private const val KEY_PENDING_SUFFIX_PROFILE_ID = "pending_suffix_profile_id"
    private const val KEY_RESTARTED_PROFILE_ID = "restarted_profile_id"
    private const val KEY_LAST_SUFFIX_ERROR = "last_suffix_error"

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
     * v2.8.20: the profile id a profile-switch restart is for, written
     * with commit() (synchronous) BEFORE the process is killed —
     * apply() may never flush before the kill. The fresh process reads
     * it in MainActivity.onCreate and locks the data dir before
     * anything can create a WebView.
     */
    fun pendingSuffixProfileId(context: Context): Long? {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_PENDING_SUFFIX_PROFILE_ID, -1L)
        return if (id > 0) id else null
    }

    fun setPendingSuffixProfileId(context: Context, id: Long?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_PENDING_SUFFIX_PROFILE_ID, id ?: -1L).commit()
    }

    /**
     * v2.8.20: the profile id the LAST restart was for. If prepare()
     * still returns SUFFIX_FAILED for this id, restarting again would
     * loop forever (white flash + "Profile: 0") — the caller must show
     * the failure on the card instead. Cleared on any successful
     * prepare(), so a stale marker can never block a later restart.
     */
    fun restartedProfileId(context: Context): Long? {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_RESTARTED_PROFILE_ID, -1L)
        return if (id > 0) id else null
    }

    fun setRestartedProfileId(context: Context, id: Long?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_RESTARTED_PROFILE_ID, id ?: -1L).commit()
    }

    /** v2.8.21: the exact exception the last suffix call died with. */
    fun lastSuffixError(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_SUFFIX_ERROR, null)

    fun setLastSuffixError(context: Context, error: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_SUFFIX_ERROR, error).apply()
    }

    /**
     * v2.8.20: lock the WebView data dir for a profile at process
     * start — MainActivity.onCreate calls this BEFORE setContent.
     * Application.onCreate creates no WebView (verified in
     * DuckDuckGoApplication/MultiProcessApplication), so this is the
     * earliest point in the process and nothing can have poisoned
     * the suffix yet. Returns false only if a WebView already exists
     * (which here means something created one during startup).
     */
    fun setSuffixEarly(context: Context, profileId: Long): Boolean {
        if (suffixProfileId == profileId) return true
        return try {
            WebView.setDataDirectorySuffix("p$profileId")
            suffixProfileId = profileId
            true
        } catch (e: Exception) {
            // v2.8.21: keep the exact reason — the card notice shows it,
            // so a failure on the phone names its own cause instead of
            // guessing. AOSP throws only when the WebView provider is
            // already initialised in this process.
            setLastSuffixError(context, e.javaClass.simpleName + ": " + (e.message ?: ""))
            false
        }
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
        // v2.8.20: persist the restart target synchronously — the fresh
        // process locks the data dir for it in onCreate, and the
        // anti-loop guard reads the restarted marker.
        setPendingSuffixProfileId(context, profileId)
        setRestartedProfileId(context, profileId)
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
                // v2.8.21: a WebView (or anything that loads the WebView
                // provider) already ran in this process, so the suffix
                // can no longer be set — AOSP throws once the provider
                // is initialised. Keep the exact reason for the card.
                setLastSuffixError(context, e.javaClass.simpleName + ": " + (e.message ?: ""))
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
        // v2.8.20: a successful prepare proves the restart worked — a
        // stale restarted marker must never block a later restart.
        setRestartedProfileId(context, null)
        setPendingSuffixProfileId(context, null)
        return PrepareResult.OK
    }
}
