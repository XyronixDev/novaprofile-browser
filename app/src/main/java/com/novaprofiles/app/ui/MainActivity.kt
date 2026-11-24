// File-level opt-in (Run #5 fix): TopAppBar / ExposedDropdownMenuBox /
// FilterChip are @ExperimentalMaterial3Api in the BOM 2026.08.00
// material3 version — the per-function @OptIn missed a TopAppBar
// NOTE (Run #7): a @file: annotation MUST sit before the package line —
// after it, the parser rejects the imports ("imports are only allowed in
// the beginning of file"). That was my placement mistake.
@file:OptIn(ExperimentalMaterial3Api::class)
package com.novaprofiles.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.duckduckgo.app.browser.BuildConfig
import com.novaprofiles.app.util.ProxyEntry
import com.novaprofiles.app.util.ProxyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.novaprofiles.app.data.Profile
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.mode.AppLauncher
import com.novaprofiles.app.util.DirectConnection
import com.novaprofiles.app.util.DirectInfo
import com.novaprofiles.app.util.NovaProfileSession
import com.novaprofiles.app.util.ProxyHelper
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        // DDG port: no viewmodel-compose artifact in this project — the
        // ViewModel is created with the standard AndroidViewModelFactory.
        val vm = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ProfileViewModel::class.java]
        // v2.8.17: after a profile-switch restart, the fresh process
        // auto-runs the profile named here (plus an optional query,
        // e.g. whoer.net from the fingerprint lab). The extras are
        // removed at once so a later recreation never re-runs them.
        val autoRunProfileId = intent.getLongExtra(
            NovaProfileSession.EXTRA_AUTO_RUN_PROFILE_ID, -1L
        ).takeIf { it > 0 }
        val autoRunQuery = intent.getStringExtra(NovaProfileSession.EXTRA_AUTO_RUN_QUERY)
        intent.removeExtra(NovaProfileSession.EXTRA_AUTO_RUN_PROFILE_ID)
        intent.removeExtra(NovaProfileSession.EXTRA_AUTO_RUN_QUERY)
        // v2.8.20: lock the WebView data dir at process start, before
        // setContent/composition can create any WebView. The restart
        // writes its target to prefs (commit); the auto-run intent
        // carries it too; the last-run profile covers a plain cold
        // start, so tapping the same profile again never needs a
        // restart. Without this, the auto-run's prepare() could hit a
        // suffix poisoned during startup and restart forever — the
        // white flash + "Profile: 0" loop the user filmed.
        val suffixProfileId = autoRunProfileId
            ?: NovaProfileSession.pendingSuffixProfileId(this)
            ?: NovaProfileSession.lastTabProfileId(this)
        if (suffixProfileId != null) {
            NovaProfileSession.setSuffixEarly(this, suffixProfileId)
            NovaProfileSession.setPendingSuffixProfileId(this, null)
        }
        setContent {
            NovaProfileTheme {
                // M3 bodyLarge carries a 24sp lineHeight; every label that
                // only sets a fontSize inherits it, so small rows render far
                // taller and airier than the reference. Keep the theme type
                // and only tighten the leading to a natural multiple.
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(lineHeight = 1.2.em)
                ) {
                    MainScreen(vm = vm, autoRunProfileId = autoRunProfileId, autoRunQuery = autoRunQuery)
                }
            }
        }
    }
}

/**
 * The card flag. The stored proxy country is the real proxy; a Direct
 * profile shows the live-detected connection country. There is no
 * language fallback anywhere — the language defaults to en-US, so
 * guessing from it painted every direct profile with the US flag.
 * Unknown stays the globe, never an invented country.
 */
fun countryOf(profile: Profile, direct: DirectInfo? = null): Pair<String, String> {
    val pc = profile.proxyCountry.trim().uppercase()
    if (pc.length == 2) {
        return Profile.flagEmoji(pc) to pc
    }
    if (profile.proxyMode == "NONE") {
        val code = direct?.countryCode?.uppercase().orEmpty()
        return if (code.length == 2) Profile.flagEmoji(code) to code else "🌐" to ""
    }
    return "🌐" to ""
}

/**
 * v2.7.2: resolve a saved proxy's country to a 2-letter code. Sources
 * disagree on the format — "US • United States" (ProxyScrape/GitHub),
 * a bare full name "United States" (Geonode), or a bare code "US".
 * The old code only handled the first form, so Geonode proxies never
 * updated the language and the card kept showing the old country.
 */
fun proxyCountryCode(entryCountry: String): String? {
    val token = entryCountry.substringBefore(" •").trim()
    if (token.length == 2 && token.uppercase() in Profile.pickerCountries) {
        return token.uppercase()
    }
    return Profile.pickerCountries.firstOrNull {
        Profile.countrySettings(it).label.equals(token, ignoreCase = true)
    }
}

/**
 * v2.7.3: parse the manual Add-proxy IP field ("198.165.0.0:8080" or
 * a bare host — port defaults to 8080). Returns null when the field
 * cannot be a proxy address; the Add button stays disabled then.
 */
fun parseManualIp(raw: String): Pair<String, Int>? {
    val t = raw.trim()
    if (t.isEmpty()) return null
    val lastColon = t.lastIndexOf(':')
    val host: String
    val port: Int
    if (lastColon > 0) {
        host = t.substring(0, lastColon).trim()
        port = t.substring(lastColon + 1).trim().toIntOrNull() ?: return null
    } else {
        host = t
        port = 8080
    }
    if (host.isEmpty() || port !in 1..65535) return null
    return host to port
}

/**
 * v2.8.24: city for a profile card ("London, United Kingdom") from the
 * profile's timezone. Known zones map to their city; anything else uses
 * the zone's city part (after the "/") with underscores as spaces.
 */
fun cityFromTimezone(tz: String): String = when (tz) {
    "America/New_York" -> "New York"
    "America/Chicago" -> "Chicago"
    "America/Denver" -> "Denver"
    "America/Los_Angeles" -> "Los Angeles"
    "America/Toronto" -> "Toronto"
    "America/Sao_Paulo" -> "Sao Paulo"
    "Europe/London" -> "London"
    "Europe/Paris" -> "Paris"
    "Europe/Berlin" -> "Berlin"
    "Europe/Amsterdam" -> "Amsterdam"
    "Europe/Rome" -> "Rome"
    "Europe/Madrid" -> "Madrid"
    "Europe/Stockholm" -> "Stockholm"
    "Europe/Zurich" -> "Zurich"
    "Europe/Dublin" -> "Dublin"
    "Asia/Tokyo" -> "Tokyo"
    "Asia/Kolkata" -> "Kolkata"
    "Australia/Sydney" -> "Sydney"
    "Pacific/Auckland" -> "Auckland"
    "UTC" -> "UTC"
    "Auto" -> ""
    else -> tz.substringAfter("/", tz).replace("_", " ")
}

/** (city, country label) for a profile card. A Direct profile shows the detected connection city and country; undetected it reads Unknown. */
fun profileCityCountry(profile: Profile, direct: DirectInfo? = null): Pair<String, String> {
    if (profile.proxyMode == "NONE") {
        return direct?.city.orEmpty() to (direct?.countryName ?: "Unknown")
    }
    val (_, code) = countryOf(profile, direct)
    if (code.isEmpty()) return cityFromTimezone(profile.timezone) to "Unknown"
    return cityFromTimezone(profile.timezone) to Profile.countrySettings(code).label
}

/** v2.8.24: the saved entry for a profile's proxy (matched by host:port). */
fun proxyEntryFor(profile: Profile, proxies: List<ProxyEntry>): ProxyEntry? =
    proxies.firstOrNull { it.host == profile.proxyHost && it.port == profile.proxyPort }

/**
 * v2.8.24: Active vs Offline on the profile card. Direct profiles are
 * Active (nothing to fail). A proxied profile is Active only when its
 * saved entry was tested working (ok + a real IP); an untested or
 * failed proxy reads Offline.
 */
fun isProfileActive(profile: Profile, proxies: List<ProxyEntry>): Boolean {
    if (profile.proxyMode == "NONE") return true
    val entry = proxyEntryFor(profile, proxies) ?: return false
    return entry.ok && entry.lastIp.isNotBlank()
}

/**
 * v2.8.24: parse one line of a proxy .txt file (the Webshare format
 * "IP:port:user:pass", a bare "host:port", or a "socks5://..." URL with
 * optional "user:pass@" userinfo). SOCKS5 only — the mode is fixed.
 * Returns null for blank, comment (#) or unparsable lines.
 */
fun parseProxyFileLine(raw: String): ProxyEntry? {
    var line = raw.trim()
    if (line.isEmpty() || line.startsWith("#")) return null
    var user = ""
    var pass = ""
    val schemeIdx = line.indexOf("://")
    if (schemeIdx >= 0) line = line.substring(schemeIdx + 3)
    val atIdx = line.lastIndexOf('@')
    if (atIdx >= 0) {
        val creds = line.substring(0, atIdx).split(":")
        if (creds.size >= 2) {
            user = creds[0]
            pass = creds.drop(1).joinToString(":")
        }
        line = line.substring(atIdx + 1)
    }
    val parts = line.split(":")
    if (parts.size < 2) return null
    val host = parts[0].trim()
    val port = parts[1].trim().toIntOrNull() ?: return null
    if (host.isEmpty() || port !in 1..65535) return null
    if (parts.size >= 4) {
        user = parts[2].trim()
        pass = parts.drop(3).joinToString(":").trim()
    }
    return ProxyEntry(name = "", mode = "SOCKS5", host = host, port = port, user = user, pass = pass)
}

/** v2.8.24: next auto name ("Myproxy #1", "#2"…) when the name is blank. */
fun nextAutoProxyName(proxies: List<ProxyEntry>): String {
    val max = proxies.mapNotNull {
        Regex("""^Myproxy #(\d+)$""").find(it.name)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }.maxOrNull() ?: 0
    return "Myproxy #${max + 1}"
}

/**
 * v2.8.24: apply a saved proxy entry to a profile (pure — the caller
 * decides whether the result is a draft or a vm.update). Timezone +
 * language + coordinates follow the proxy's IP, but only when the
 * profile's modes say IP-based; Custom keeps the user's own values.
 * The country code is stored on the profile so the card flag reads the
 * real proxy country (same logic as the old ProxiesTab onAssignProxy).
 */
fun profileWithProxy(p: Profile, entry: ProxyEntry): Profile {
    val code = proxyCountryCode(entry.country)
    val cs = code?.let { Profile.countrySettings(it) }
    return p.copy(
        proxyMode = entry.mode,
        proxyHost = entry.host,
        proxyPort = entry.port,
        proxyUser = entry.user,
        proxyPass = entry.pass,
        proxyCountry = code ?: p.proxyCountry,
        timezone = if (p.timezoneMode == "IP")
            entry.lastTimezone.ifBlank { p.timezone } else p.timezone,
        language = if (p.languagesMode == "IP" && cs != null)
            cs.language else p.language,
        latitude = if (p.geoMode == "IP" && entry.lastLat != 0.0)
            entry.lastLat else p.latitude,
        longitude = if (p.geoMode == "IP" && entry.lastLon != 0.0)
            entry.lastLon else p.longitude
    )
}

/** v2.8.24: premium home navigation — one route per screenshot screen. */
private enum class HomeRoute { PROFILES, PROXY, SETTINGS, FORM }

/** v2.8.24: the Profiles-list filter behind the funnel button. */
private enum class ProfileFilter { ALL, ACTIVE, OFFLINE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: ProfileViewModel, autoRunProfileId: Long? = null, autoRunQuery: String? = null) {
    val profiles by vm.profiles.collectAsState()
    val query by vm.query.collectAsState()
    val proxyCheck by vm.proxyCheckResult.collectAsState()
    val importResult by vm.importResult.collectAsState()
    val exportResult by vm.exportResult.collectAsState()

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val proxyStore = remember { ProxyStore(ctx) }
    val savedProxies by proxyStore.proxies.collectAsState()

    var route by remember { mutableStateOf(HomeRoute.PROFILES) }
    // v2.8.24: the Add/Edit form draft lives here (not in the form) so
    // it survives the trip to the Add Proxy screen and back.
    var draft by remember { mutableStateOf<Profile?>(null) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var proxyReturnToForm by remember { mutableStateOf(false) }
    var runningIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var syncingIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // v2.8.26 (DDG port): the profile whose last launch failed — its
    // result is shown on its own card (never at the bottom).
    var launchFailedId by remember { mutableStateOf<Long?>(null) }
    var labProfile by remember { mutableStateOf<Profile?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Profile?>(null) }
    var filter by remember { mutableStateOf(ProfileFilter.ALL) }
    // The device's own connection, detected live once at startup and
    // cached — Direct cards read their flag, country and IP from here.
    var directInfo by remember { mutableStateOf(DirectConnection.cached(ctx)) }
    var directDetecting by remember { mutableStateOf(false) }

    fun refreshDirect() {
        if (directDetecting) return
        directDetecting = true
        scope.launch {
            DirectConnection.detect(ctx)?.let { directInfo = it }
            directDetecting = false
        }
    }

    LaunchedEffect(Unit) { refreshDirect() }

    /** Sessions (v2.0 Pro): tell a running browser to stop itself. */
    fun stopProfile(id: Long?) {
        val intent = Intent(NovaProfileSession.STOP_PROFILE_ACTION).apply {
            setPackage(ctx.packageName)
            if (id != null) putExtra(NovaProfileSession.EXTRA_PROFILE_ID, id)
        }
        ctx.sendBroadcast(intent)
        NovaProfileSession.clearRunning(ctx)
        ProxyHelper.clear(ContextCompat.getMainExecutor(ctx)) {}
        runningIds = if (id == null) emptySet() else runningIds - id
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            ctx.contentResolver.openOutputStream(uri)?.let { out ->
                vm.exportAll(out, profiles)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            ctx.contentResolver.openInputStream(uri)?.let { input ->
                vm.importFrom(input)
            }
        }
    }

    fun openAddForm() {
        val max = profiles.mapNotNull {
            Regex("""^Profile (\d+)$""").find(it.name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        // v2.8.25: Add starts with an EMPTY name — the reference shows
        // the "Enter profile name..." placeholder with 0/50, not a
        // pre-filled "Profile N". (Profile.new() would re-fill it.)
        draft = Profile.new("Profile ${max + 1}").copy(name = "")
        editingId = null
        route = HomeRoute.FORM
    }

    fun openEditForm(p: Profile) {
        draft = p.copy()
        editingId = p.id
        route = HomeRoute.FORM
    }

    fun cancelForm() {
        draft = null
        editingId = null
        route = HomeRoute.PROFILES
    }

    fun saveDraft() {
        val d = draft ?: return
        if (editingId != null) vm.update(d) else vm.create(d)
        draft = null
        editingId = null
        route = HomeRoute.PROFILES
    }

    /** DDG port: prepare the profile, then open DuckDuckGo's browser
     *  with it. prepare() applies the per-profile WebView data dir +
     *  proxy override and waits (v2.7.1 lesson: a WebView created
     *  before the override lands leaks the real IP). Fail-closed: a
     *  proxy that cannot be applied means no browser at all.
     *  v2.8.17: when this process already uses another profile's data
     *  dir, prepare() reports SUFFIX_FAILED and the whole process is
     *  restarted for this profile — the fresh process auto-runs it
     *  with its own dir (own cookies/storage). Tabs belong to the
     *  last profile that used the browser, so a different profile
     *  flags them for clearing; the browser consumes the flag on its
     *  first tab-list emission and seeds one fresh tab. */
    suspend fun launchProfile(p: Profile, query: String? = null): Boolean {
        when (NovaProfileSession.prepare(ctx, p)) {
            NovaProfileSession.PrepareResult.PROXY_FAILED -> {
                launchFailedId = p.id
                vm.proxyCheckResult.value =
                    "Proxy apply nahi hua (${ProxyHelper.summary(p)}). Dobara try karo — browser direct nahi khola."
                return false
            }
            NovaProfileSession.PrepareResult.SUFFIX_FAILED -> {
                // v2.8.20: anti-loop — if we ALREADY restarted for this
                // profile and the suffix is STILL locked, restarting
                // again would loop forever (white flash + "Profile: 0").
                // Show it on the card instead of looping.
                if (NovaProfileSession.restartedProfileId(ctx) == p.id) {
                    launchFailedId = p.id
                    // v2.8.21: show the exact recorded reason — the
                    // user films the card, and the reason names the
                    // cause instead of another round of guessing.
                    val reason = NovaProfileSession.lastSuffixError(ctx)
                    vm.proxyCheckResult.value =
                        "Browser nahi khula — data folder lock hai. App band karke dobara kholo." +
                            (if (reason != null) " ($reason)" else "")
                    return false
                }
                NovaProfileSession.restartAppForProfile(ctx, p.id, query)
                return false
            }
            NovaProfileSession.PrepareResult.OK -> Unit
        }
        // v2.8.13: ask the browser to re-load the current tab under this
        // profile (the tab may hold a page loaded under another profile's
        // proxy — stale IP/country on whoer).
        val clearTabs = NovaProfileSession.lastTabProfileId(ctx) != p.id
        NovaProfileSession.setLastTabProfileId(ctx, p.id)
        if (clearTabs) NovaProfileSession.setTabsClearPending(ctx, true)
        // Open the profile's home page (Google by default), not a blank
        // tab — a blank tab focuses the omnibar and pops the keyboard.
        ctx.startActivity(
            BrowserActivity.intent(
                ctx,
                launchSource = AppLauncher,
                queryExtra = query ?: p.homeUrl
            ).apply {
                putExtra(NovaProfileSession.EXTRA_RELOAD_TAB, true)
            }
        )
        return true
    }

    /**
     * The card Run tap, shared by the tap and the post-restart
     * auto-run (v2.8.17): launch at once with the stored values, and
     * refresh the proxy IP's timezone + coordinates in the background
     * for the next launch when the profile follows the IP — the
     * refresh is a network call through the proxy and must never
     * delay the browser opening. The optional query is only for the
     * fingerprint lab's whoer check after a restart.
     */
    fun runProfile(p: Profile, query: String? = null) {
        launchFailedId = null
        // The browser must open immediately on tap. The proxy location
        // re-sync is a network call through the proxy (seconds on a slow
        // one) and must never block the launch — refresh it in the
        // background for the next launch instead; this launch uses the
        // stored (already tested) values.
        syncingIds = syncingIds + p.id
        if (p.proxyMode != "NONE" &&
            (p.timezoneMode == "IP" || p.geoMode == "IP")
        ) {
            val savedEntry = proxyStore.proxies.value.firstOrNull {
                it.host == p.proxyHost && it.port == p.proxyPort
            }
            val alreadySynced = savedEntry != null && savedEntry.ok &&
                savedEntry.lastIp.isNotBlank() &&
                savedEntry.lastTimezone.isNotBlank() &&
                (p.timezoneMode != "IP" || savedEntry.lastTimezone == p.timezone)
            if (!alreadySynced) {
                scope.launch {
                    try {
                        val entry = ProxyEntry(
                            name = p.name,
                            mode = p.proxyMode, host = p.proxyHost,
                            port = p.proxyPort, user = p.proxyUser,
                            pass = p.proxyPass
                        )
                        val loc = proxyStore.resolveLocation(entry)
                        if (loc != null) {
                            val updated = p.copy(
                                timezone = if (p.timezoneMode == "IP") loc.timezone else p.timezone,
                                latitude = if (p.geoMode == "IP" && loc.lat != 0.0) loc.lat else p.latitude,
                                longitude = if (p.geoMode == "IP" && loc.lon != 0.0) loc.lon else p.longitude
                            )
                            vm.updateNow(updated)
                            val saved = proxyStore.proxies.value.firstOrNull {
                                it.host == p.proxyHost && it.port == p.proxyPort
                            }
                            if (saved != null) {
                                proxyStore.update(
                                    saved.copy(
                                        lastIp = loc.ip, lastTimezone = loc.timezone,
                                        lastLat = loc.lat, lastLon = loc.lon, ok = true
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Background refresh only — a failure here must
                        // not touch the launch already in flight.
                    }
                }
            }
        }
        scope.launch {
            try {
                // Direct profiles carry the detected connection
                // timezone when it is known, so the run never uses
                // the "Auto" placeholder. Detection already ran at
                // startup, so this never waits on the network.
                var toLaunch = p
                if (p.proxyMode == "NONE" && p.timezoneMode == "IP") {
                    val tz = directInfo?.timezone.orEmpty()
                    if (tz.isNotBlank() && tz != p.timezone) {
                        toLaunch = p.copy(timezone = tz)
                        vm.updateNow(toLaunch)
                    }
                }
                if (launchProfile(toLaunch, query)) {
                    runningIds = runningIds + p.id
                    vm.markRun(p.id)
                }
            } finally {
                syncingIds = syncingIds - p.id
            }
        }
    }

    // System back: form/proxy/settings return to the list; the list exits.
    BackHandler(enabled = route != HomeRoute.PROFILES) {
        when (route) {
            HomeRoute.FORM -> cancelForm()
            HomeRoute.PROXY -> {
                if (proxyReturnToForm) {
                    proxyReturnToForm = false
                    route = HomeRoute.FORM
                } else {
                    route = HomeRoute.PROFILES
                }
            }
            HomeRoute.SETTINGS -> route = HomeRoute.PROFILES
            HomeRoute.PROFILES -> { /* unreachable — handler disabled */ }
        }
    }

    // v2.8.17 (DDG port): post-restart auto-run — the fresh process
    // runs the profile named by the restart intent, once; the flag
    // stops a re-run on recomposition. v2.8.19 loads by id directly
    // — the profiles Flow starts empty on a fresh process.
    var autoRunDone by remember { mutableStateOf(false) }
    LaunchedEffect(autoRunProfileId) {
        if (!autoRunDone && autoRunProfileId != null) {
            autoRunDone = true
            vm.getProfile(autoRunProfileId)?.let { runProfile(it, autoRunQuery) }
        }
    }

    // Fingerprint Lab (v2.0 Pro) — full-screen check for one profile.
    labProfile?.let { lp ->
        FingerprintLabScreen(profile = lp, onBack = { labProfile = null })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoContentBg)
    ) {
        BottomWave(modifier = Modifier.align(Alignment.BottomCenter))
        when (route) {
            HomeRoute.PROFILES -> ProfilesScreen(
                profiles = profiles,
                query = query,
                onQuery = { vm.query.value = it },
                filter = filter,
                onFilter = { filter = it },
                savedProxies = savedProxies,
                runningIds = runningIds,
                syncingIds = syncingIds,
                onLaunch = { runProfile(it) },
                onStop = { stopProfile(it.id) },
                onEdit = { openEditForm(it) },
                onDelete = { pendingDelete = it },
                onDuplicate = { vm.duplicate(it) },
                onCheckFingerprint = { labProfile = it },
                onOpenSettings = { route = HomeRoute.SETTINGS },
                proxyStore = proxyStore,
                onAddProfile = { openAddForm() },
                noticeProfileId = launchFailedId,
                noticeText = proxyCheck,
                onDismissNotice = { vm.clearNotices(); launchFailedId = null },
                directInfo = directInfo,
                onRetryDirect = { refreshDirect() },
                onShowAbout = { showAbout = true }
            )
            HomeRoute.PROXY -> AddProxyScreen(
                proxyStore = proxyStore,
                savedProxies = savedProxies,
                scope = scope,
                draft = draft,
                onApplyToDraft = { entry ->
                    draft = draft?.let { profileWithProxy(it, entry) }
                    proxyReturnToForm = false
                    route = HomeRoute.FORM
                },
                onDirectForDraft = {
                    draft = draft?.copy(
                        proxyMode = "NONE", proxyHost = "", proxyPort = 8080,
                        proxyUser = "", proxyPass = "", proxyCountry = ""
                    )
                    proxyReturnToForm = false
                    route = HomeRoute.FORM
                },
                onOpenProfiles = { route = HomeRoute.PROFILES },
                onOpenSettings = { route = HomeRoute.SETTINGS },
                onAddProfile = { openAddForm() }
            )
            HomeRoute.SETTINGS -> SettingsScreen(
                onExport = { exportLauncher.launch("novaprofile-profiles.zip") },
                onImport = { importLauncher.launch(arrayOf("*/*")) },
                onAbout = { showAbout = true },
                notice = importResult ?: exportResult,
                onOpenProfiles = { route = HomeRoute.PROFILES },
                onOpenSettings = { },
                onAddProfile = { openAddForm() }
            )
            HomeRoute.FORM -> {
                val d = draft
                if (d != null) {
                    AddEditFormScreen(
                        draft = d,
                        editing = editingId != null,
                        onDraft = { draft = it },
                        onSave = { saveDraft() },
                        onCancel = { cancelForm() },
                        onOpenProxyPicker = {
                            proxyReturnToForm = true
                            route = HomeRoute.PROXY
                        },
                        onCheckProxy = { p -> vm.checkProxy(p, "https://api.ipify.org") },
                        proxyCheckResult = proxyCheck,
                        onClearNotice = { vm.clearNotices() },
                        proxyStore = proxyStore,
                        onCheckFingerprint = { labProfile = it }
                    )
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About NovaProfile") },
            text = {
                Text(
                    "NovaProfile ${BuildConfig.VERSION_NAME} " +
                        "(Build ${BuildConfig.VERSION_CODE}) — DuckDuckGo-powered " +
                        "multi-profile browser.\n\n" +
                        "• Unlimited profiles, everything free, no billing, no trial.\n" +
                        "• Per-profile cookies, proxy, user-agent, language, " +
                        "screen, WebGL and Canvas settings.\n" +
                        "• Multi-tab browser with history, bookmarks, downloads, " +
                        "desktop mode, find in page and translate.\n" +
                        "• Pro: Fingerprint Lab, Proxy Manager, templates, sessions.\n" +
                        "• Fingerprint protection is JS-level inside the system " +
                        "WebView — it is NOT a Chromium fork and advanced " +
                        "fingerprint tests can still detect the real device.\n" +
                        "• One profile runs at a time. Data never leaves your phone."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("OK") }
            }
        )
    }

    // v2.7.2: delete is permanent — one confirm, no trash step.
    pendingDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete profile?") },
            text = { Text("\"${p.name}\" will be deleted permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteForever(p)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

/**
 * v2.8.24: the premium wave header — dark green, with the layered wave
 * edge from the reference screenshots, drawn on a Canvas (no assets).
 * [content] is the header row (back/logo/title/premium or bell/gear).
 */
@Composable
internal fun WaveHeader(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoHeaderDark)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(26.dp)) {
            val w = size.width
            val h = size.height
            // Mint wave band underneath.
            val mintPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.55f)
                cubicTo(w * 0.75f, h * 1.1f, w * 0.45f, h * 0.1f, w * 0.2f, h * 0.7f)
                cubicTo(w * 0.08f, h * 0.95f, w * 0.03f, h * 0.8f, 0f, h * 0.6f)
                close()
            }
            drawPath(mintPath, Color(0xFF9BE8C8))
            // Dark wave on top, slightly higher.
            val darkPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.35f)
                cubicTo(w * 0.7f, h * 0.9f, w * 0.4f, -h * 0.1f, w * 0.15f, h * 0.5f)
                cubicTo(w * 0.06f, h * 0.7f, w * 0.02f, h * 0.6f, 0f, h * 0.4f)
                close()
            }
            drawPath(darkPath, GoHeaderDark)
        }
    }
}

/**
 * v2.8.24: the green wave rising from the bottom of the screen (behind
 * the bottom nav / buttons), drawn on a Canvas (no assets).
 */
@Composable
private fun BottomWave(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(56.dp)) {
        val w = size.width
        val h = size.height
        val lightPath = Path().apply {
            moveTo(0f, h)
            lineTo(w, h)
            lineTo(w, h * 0.45f)
            cubicTo(w * 0.7f, h * 0.1f, w * 0.4f, h * 0.9f, w * 0.15f, h * 0.4f)
            cubicTo(w * 0.06f, h * 0.2f, w * 0.02f, h * 0.5f, 0f, h * 0.35f)
            close()
        }
        drawPath(lightPath, Color(0xFF4ADE9E))
        val darkPath = Path().apply {
            moveTo(0f, h)
            lineTo(w, h)
            lineTo(w, h * 0.7f)
            cubicTo(w * 0.75f, h * 0.3f, w * 0.45f, h * 1.0f, w * 0.2f, h * 0.65f)
            cubicTo(w * 0.08f, h * 0.45f, w * 0.03f, h * 0.7f, 0f, h * 0.6f)
            close()
        }
        drawPath(darkPath, GoHeaderDark)
    }
}

/**
 * v2.8.24: the NovaProfile infinity mark. The app ships no infinity
 * asset (ic_launcher_foreground is a white "N"), so the mark from the
 * reference screenshots is drawn with the standard infinity glyph —
 * no new logo is invented.
 */
@Composable
private fun NovaMark(color: Color, fontSize: Int) {
    Text(
        "N",
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false
    )
}

/** v2.8.25: the "NovaProfile / Anti-Detect Browser" title block. One line each, never wrapped. */
@Composable
private fun BrandTitle(modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Nova", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            Text("Profile", color = GoGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        }
        Text("Anti-Detect Browser", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
    }
}

/** The gold Premium pill (decorative text only — no paywall). One line, never wrapped. */
@Composable
private fun PremiumPill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, GoGold, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("\u265b", color = GoGold, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        Spacer(Modifier.width(4.dp))
        Text("Premium", color = GoGold, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
    }
}

/** Premium header row. The logo is the white N in a green circle on every screen, and the Premium badge is always shown. */
@Composable
internal fun PremiumHeader(
    logoInCircle: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (logoInCircle) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GoGreen),
                contentAlignment = Alignment.Center
            ) {
                NovaMark(color = Color.White, fontSize = 34)
            }
        } else {
            NovaMark(color = GoGreen, fontSize = 34)
        }
        Spacer(Modifier.width(8.dp))
        BrandTitle(Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        PremiumPill()
    }
}

/**
 * v2.8.24: premium bottom nav — white pill (Profiles / Settings) plus
 * the green round "+ Add Profile" button. [selected] is null on the
 * Add Proxy screen (no item highlighted there).
 */
@Composable
private fun PremiumBottomNav(
    selected: HomeRoute?,
    onProfiles: () -> Unit,
    onSettings: () -> Unit,
    onAddProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = { Icon(Icons.Outlined.Person, null, Modifier.size(18.dp)) },
                label = "Profiles",
                selected = selected == HomeRoute.PROFILES,
                onClick = onProfiles
            )
            BottomNavItem(
                icon = { Icon(Icons.Filled.Settings, null, Modifier.size(18.dp)) },
                label = "Settings",
                selected = selected == HomeRoute.SETTINGS,
                onClick = onSettings
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GoGreen)
                .clickable(onClick = onAddProfile),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text("Add Profile", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) Color(0xFFDFF3EC) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                    if (selected) GoGreen else GoGreyText
            ) { icon() }
        }
        Text(
            label,
            fontSize = 11.sp,
            maxLines = 1,
            color = if (selected) GoGreen else GoGreyText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * The dark pill tabs above the Profiles/Proxy pager. The selected tab
 * is the green pill and the other is plain white text; every label is
 * one line, never wrapped. Tapping a tab and swiping the pager switch
 * the same page, so the proxy list opens in place instead of a new screen.
 */
@Composable
private fun PillTabs(
    profileCount: Int,
    selectedPage: Int,
    onSelectPage: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(GoHeaderDark)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(if (selectedPage == 0) GoGreen else Color.Transparent)
                .clickable { onSelectPage(0) }
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Layers, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "Profiles ($profileCount)",
                color = Color.White,
                fontWeight = if (selectedPage == 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(if (selectedPage == 1) GoGreen else Color.Transparent)
                .clickable { onSelectPage(1) }
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "Proxy",
                color = Color.White,
                fontWeight = if (selectedPage == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/** v2.8.24: a green-icon section header ("Profile Name", "Proxy", …). */
@Composable
private fun SectionHeader(icon: @Composable () -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoTitle)
    }
}

/** v2.8.24: a white rounded row with a green icon, title, subtitle, chevron. */
@Composable
private fun MenuRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = GoGreyText, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GoGreyText)
    }
}

/** v2.8.24: the Profiles list — the home screen of the reference design. */
@Composable
private fun ProfilesScreen(
    profiles: List<Profile>,
    query: String,
    onQuery: (String) -> Unit,
    filter: ProfileFilter,
    onFilter: (ProfileFilter) -> Unit,
    savedProxies: List<ProxyEntry>,
    runningIds: Set<Long>,
    syncingIds: Set<Long>,
    onLaunch: (Profile) -> Unit,
    onStop: (Profile) -> Unit,
    onEdit: (Profile) -> Unit,
    onDelete: (Profile) -> Unit,
    onDuplicate: (Profile) -> Unit,
    onCheckFingerprint: (Profile) -> Unit,
    onOpenSettings: () -> Unit,
    proxyStore: ProxyStore,
    onAddProfile: () -> Unit,
    onShowAbout: () -> Unit,
    noticeProfileId: Long? = null,
    noticeText: String? = null,
    onDismissNotice: () -> Unit = {},
    directInfo: DirectInfo? = null,
    onRetryDirect: () -> Unit = {}
) {
    var filterOpen by remember { mutableStateOf(false) }
    val shown = profiles.filter {
        when (filter) {
            ProfileFilter.ALL -> true
            ProfileFilter.ACTIVE -> isProfileActive(it, savedProxies)
            ProfileFilter.OFFLINE -> !isProfileActive(it, savedProxies)
        }
    }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    BackHandler(enabled = pagerState.currentPage == 1) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveHeader {
            PremiumHeader(
                logoInCircle = true
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            PillTabs(
                profileCount = profiles.size,
                selectedPage = pagerState.currentPage,
                onSelectPage = { page -> scope.launch { pagerState.animateScrollToPage(page) } }
            )
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.weight(1f)
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val pageAlpha = (1f - 0.35f * abs(pageOffset)).coerceIn(0f, 1f)
                when (page) {
                    0 -> Column(
                        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = pageAlpha },
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search field + filter button.
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = onQuery,
                                placeholder = { Text("Search profiles...", color = GoGreyText, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, null, tint = GoGreen)
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(28.dp),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = GoGreen,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { filterOpen = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.FilterList, null, tint = GoGreen, modifier = Modifier.size(20.dp))
                                }
                                DropdownMenu(expanded = filterOpen, onDismissRequest = { filterOpen = false }) {
                                    DropdownMenuItem(
                                        text = { Text("All profiles") },
                                        onClick = { filterOpen = false; onFilter(ProfileFilter.ALL) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Active only") },
                                        onClick = { filterOpen = false; onFilter(ProfileFilter.ACTIVE) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Offline only") },
                                        onClick = { filterOpen = false; onFilter(ProfileFilter.OFFLINE) }
                                    )
                                }
                            }
                        }
                        if (filter != ProfileFilter.ALL) {
                            Text(
                                when (filter) {
                                    ProfileFilter.ACTIVE -> "Showing active profiles"
                                    ProfileFilter.OFFLINE -> "Showing offline profiles"
                                    else -> ""
                                },
                                color = GoGreyText,
                                fontSize = 13.sp
                            )
                        }
                        if (shown.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No profiles yet", style = MaterialTheme.typography.titleMedium, color = GoTitle)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Tap Add Profile to create your first profile.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoGreyText
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(shown, key = { it.id }) { p ->
                                    ProfileCard(
                                        profile = p,
                                        entry = proxyEntryFor(p, savedProxies),
                                        active = isProfileActive(p, savedProxies),
                                        running = runningIds.contains(p.id),
                                        syncing = syncingIds.contains(p.id),
                                        onLaunch = { onLaunch(p) },
                                        onStop = { onStop(p) },
                                        onEdit = { onEdit(p) },
                                        onDelete = { onDelete(p) },
                                        onDuplicate = { onDuplicate(p) },
                                        onCheckFingerprint = { onCheckFingerprint(p) },
                                        notice = if (noticeProfileId == p.id) noticeText else null,
                                        onDismissNotice = onDismissNotice,
                                        direct = directInfo,
                                        onRetryDirect = onRetryDirect
                                    )
                                }
                                item { Spacer(Modifier.height(8.dp)) }
                            }
                        }
                    }
                    else -> AddProxyBody(
                        proxyStore = proxyStore,
                        savedProxies = savedProxies,
                        scope = scope,
                        draft = null,
                        onApplyToDraft = {},
                        onDirectForDraft = {},
                        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = pageAlpha }
                    )
                }
            }
        }
        PremiumBottomNav(
            selected = HomeRoute.PROFILES,
            onProfiles = { },
            onSettings = onOpenSettings,
            onAddProfile = onAddProfile
        )
    }
}

/** v2.8.24: one profile card, exactly as in the reference screenshot. */
@Composable
private fun ProfileCard(
    profile: Profile,
    entry: ProxyEntry?,
    active: Boolean,
    running: Boolean,
    syncing: Boolean,
    onLaunch: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCheckFingerprint: () -> Unit,
    notice: String? = null,
    onDismissNotice: () -> Unit = {},
    direct: DirectInfo? = null,
    onRetryDirect: () -> Unit = {}
) {
    var menuOpen by remember { mutableStateOf(false) }
    val (flag, _) = countryOf(profile, direct)
    val (city, country) = profileCityCountry(profile, direct)
    val location = listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")
    val ipLine = when {
        profile.proxyMode == "NONE" -> if (direct != null) "IP: ${direct.ip}" else "IP: Unavailable"
        entry == null -> "IP: ${profile.proxyHost}"
        entry.lastIp.isBlank() -> "IP: ${profile.proxyHost} • not tested"
        else -> "IP: ${entry.lastIp} • ${entry.lastMs} ms"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        // Green left accent.
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(GoGreen)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 14.dp, bottom = 4.dp, end = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(flag, fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            profile.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GoTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) Color(0xFFD9F2E5) else Color(0xFFF1F3F4))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (active) GoGreenDark else GoGreyText)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    if (active) "Active" else "Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) GoGreenDark else GoGreyText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, null, tint = GoGreyText, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (profile.proxyMode == "NONE") "Direct connection" else "Proxy • ${profile.proxyMode}",
                            fontSize = 11.sp,
                            color = GoGreyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = GoGreyText, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(location, fontSize = 11.sp, color = GoGreyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Language, null, tint = GoGreyText, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(ipLine, fontSize = 11.sp, color = GoGreyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (profile.proxyMode == "NONE" && direct == null) {
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = onRetryDirect,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    "Retry",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoGreenDark,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
                Box {
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Check fingerprint") },
                            onClick = { menuOpen = false; onCheckFingerprint() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { menuOpen = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { menuOpen = false; onDuplicate() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFD93025)) },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
            // Bottom action bar: Launch | Edit | More.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F3F4))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (running) {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = GoStopRed),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Icon(Icons.Filled.Stop, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Stop", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, softWrap = false)
                    }
                } else if (syncing) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(GoGreen)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    Button(
                        onClick = onLaunch,
                        colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Launch", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, softWrap = false)
                    }
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onEdit)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Edit, null, tint = GoTitle, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", fontWeight = FontWeight.Bold, color = GoTitle, fontSize = 13.sp, maxLines = 1, softWrap = false)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(GoDivider)
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { menuOpen = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("•••", fontWeight = FontWeight.Bold, color = GoTitle, fontSize = 13.sp, maxLines = 1, softWrap = false)
                    Spacer(Modifier.width(6.dp))
                    Text("More", fontWeight = FontWeight.Bold, color = GoTitle, fontSize = 13.sp, maxLines = 1, softWrap = false)
                }
            }
            // v2.8.26 (DDG port): a proxy check / launch result is
            // attached to this profile's own card — never at the
            // bottom of the screen (same as the DDG line's row).
            if (notice != null) {
                Row(
                    modifier = Modifier.padding(start = 18.dp, top = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notice,
                        fontSize = 13.sp,
                        color = Color(0xFFD93025),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismissNotice) { Text("OK", fontSize = 13.sp) }
                }
            }
        }
    }
}

/** v2.8.24: the Add / Edit Profile form — the reference screenshot form. */
@Composable
private fun AddEditFormScreen(
    draft: Profile,
    editing: Boolean,
    onDraft: (Profile) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onOpenProxyPicker: () -> Unit,
    onCheckProxy: (Profile) -> Unit,
    proxyCheckResult: String?,
    onClearNotice: () -> Unit,
    proxyStore: ProxyStore,
    onCheckFingerprint: (Profile) -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    var proxyPickerOpen by remember { mutableStateOf(false) }
    var extSection by remember { mutableStateOf<String?>(null) }
    var connectingCountry by remember { mutableStateOf<String?>(null) }
    var countryStatus by remember { mutableStateOf<String?>(null) }

    // System back inside a sub-screen returns to the main form.
    BackHandler(enabled = proxyPickerOpen || extSection != null) {
        proxyPickerOpen = false
        extSection = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveHeader {
            PremiumHeader(logoInCircle = true)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(2.dp))
            when {
                proxyPickerOpen -> ProxyPickerView(
                    proxyStore = proxyStore,
                    scope = scope,
                    connectingCountry = connectingCountry,
                    onConnectingCountry = { connectingCountry = it },
                    countryStatus = countryStatus,
                    onCountryStatus = { countryStatus = it },
                    onPicked = { working, cs ->
                        proxyStore.add(working)
                        onDraft(
                            draft.copy(
                                proxyMode = working.mode,
                                proxyHost = working.host,
                                proxyPort = working.port,
                                proxyUser = "",
                                proxyPass = "",
                                proxyCountry = cs.code,
                                timezone = if (draft.timezoneMode == "IP") working.lastTimezone.ifBlank { cs.timezone } else draft.timezone,
                                language = if (draft.languagesMode == "IP") cs.language else draft.language,
                                latitude = if (draft.geoMode == "IP" && working.lastLat != 0.0) working.lastLat else draft.latitude,
                                longitude = if (draft.geoMode == "IP" && working.lastLon != 0.0) working.lastLon else draft.longitude
                            )
                        )
                        countryStatus =
                            "${cs.label} connected • ${working.lastIp} • " +
                                "${working.lastMs} ms — " +
                                "timezone + language auto-matched. " +
                                "Save the profile to apply."
                        proxyPickerOpen = false
                    },
                    onBack = { connectingCountry = null; proxyPickerOpen = false }
                )
                extSection != null -> ExtendedSectionView(
                    section = extSection!!,
                    draft = draft,
                    onDraft = onDraft,
                    onBack = { extSection = null },
                    onOpenProxyPicker = { extSection = null; proxyPickerOpen = true },
                    onCheckProxy = onCheckProxy,
                    onClearNotice = onClearNotice,
                    proxyCheckResult = proxyCheckResult
                )
                else -> {
                    // Avatar + title.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(80.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(GoMintBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Person,
                                    null,
                                    tint = GoGreenDark,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(GoGreen)
                                    .border(3.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (editing) Icons.Filled.Edit else Icons.Filled.Add,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (editing) "Edit Profile" else "Add Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = GoTitle
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (editing) "Update your profile settings and customize your browser environment."
                                else "Create a new profile with unique fingerprint and proxy settings.",
                                color = GoGreyText,
                                fontSize = 12.sp
                            )
                        }
                    }
                    // Profile Name card.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Person, null, tint = GoGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Profile Name", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoTitle)
                        }
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { onDraft(draft.copy(name = it.take(50))) },
                            placeholder = { Text("Enter profile name...", color = GoGreyText, fontSize = 12.sp) },
                            trailingIcon = {
                                Text("${draft.name.length}/50", color = GoGreyText, fontSize = 10.sp)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = GoGreen,
                                unfocusedBorderColor = GoDivider
                            )
                        )
                    }
                    // Proxy.
                    SectionHeader(
                        icon = { Icon(Icons.Filled.Layers, null, tint = GoGreen, modifier = Modifier.size(18.dp)) },
                        label = "Proxy"
                    )
                    MenuRow(
                        icon = {
                            Icon(
                                if (draft.proxyMode == "NONE") Icons.Filled.Link else Icons.Filled.Storage,
                                null,
                                tint = GoGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = if (draft.proxyMode == "NONE") "Connect Proxy"
                        else "${draft.proxyMode} ${draft.proxyHost}:${draft.proxyPort}",
                        subtitle = if (draft.proxyMode == "NONE") "Tap to select a proxy"
                        else "Timezone: ${draft.timezone} • ${draft.language}",
                        onClick = onOpenProxyPicker
                    )
                    countryStatus?.let { Text(it, color = GoGreyText, fontSize = 13.sp) }
                    // Extended settings.
                    SectionHeader(
                        icon = { Icon(Icons.Filled.Settings, null, tint = GoGreen, modifier = Modifier.size(18.dp)) },
                        label = "Extended Settings"
                    )
                    MenuRow(
                        icon = { Icon(Icons.Filled.DesktopWindows, null, tint = GoGreen, modifier = Modifier.size(24.dp)) },
                        title = "Profile Overview",
                        // v2.8.25: the reference text differs per screen —
                        // Add says "Set profile details…", Edit says
                        // "View and manage profile details".
                        subtitle = if (editing) "View and manage profile details"
                        else "Set profile details and advanced options",
                        onClick = { extSection = "overview" }
                    )
                    MenuRow(
                        icon = { Icon(Icons.Filled.LocationOn, null, tint = GoGreen, modifier = Modifier.size(24.dp)) },
                        title = "Proxy & Location",
                        subtitle = "Choose country and proxy settings",
                        onClick = { extSection = "proxy" }
                    )
                    MenuRow(
                        icon = { Icon(Icons.Filled.Language, null, tint = GoGreen, modifier = Modifier.size(24.dp)) },
                        title = "Browser",
                        subtitle = "Configure browser settings",
                        onClick = { extSection = "browser" }
                    )
                    MenuRow(
                        icon = { Icon(Icons.Filled.Storage, null, tint = GoGreen, modifier = Modifier.size(24.dp)) },
                        title = "Storage",
                        subtitle = "Manage profile storage",
                        onClick = { extSection = "storage" }
                    )
                    MenuRow(
                        icon = { Icon(Icons.Filled.Memory, null, tint = GoGreen, modifier = Modifier.size(24.dp)) },
                        title = "Hardware",
                        subtitle = "Customize hardware fingerprint",
                        onClick = { extSection = "hardware" }
                    )
                    MenuRow(
                        icon = { Icon(Icons.Filled.Fingerprint, null, tint = GoGreen, modifier = Modifier.size(24.dp)) },
                        title = "Check fingerprint",
                        subtitle = "Verify your profile fingerprint",
                        onClick = { onCheckFingerprint(draft) }
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        // Bottom buttons (main form view only).
        if (!proxyPickerOpen && extSection == null) {
            // v2.8.25: the reference bottom rows differ per screen —
            // Add: Cancel (mint, left) + Add Profile (green, right);
            // Edit: Save Profile (mint, left) + Cancel (white, right).
            val trySave: () -> Unit = {
                when {
                    draft.name.isBlank() -> error = "Name is required."
                    draft.proxyMode != "NONE" && draft.proxyHost.isBlank() ->
                        error = "Proxy host is required for ${draft.proxyMode}."
                    draft.proxyPort !in 1..65535 ->
                        error = "Proxy port must be 1–65535."
                    else -> onSave()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editing) {
                    Button(
                        onClick = trySave,
                        enabled = connectingCountry == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDFF3EC),
                            contentColor = GoGreenDark
                        ),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (connectingCountry != null) "Connecting ${connectingCountry}…"
                            else "Save Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = GoTitle
                        ),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, softWrap = false)
                    }
                } else {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDFF3EC),
                            contentColor = GoGreenDark
                        ),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Close, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = trySave,
                        enabled = connectingCountry == null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(3f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (connectingCountry != null) "Connecting ${connectingCountry}…"
                            else "Add Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/** v2.8.24: the Add Proxy screen — import, manual add, saved list. */
@Composable
private fun AddProxyScreen(
    proxyStore: ProxyStore,
    savedProxies: List<ProxyEntry>,
    scope: CoroutineScope,
    draft: Profile?,
    onApplyToDraft: (ProxyEntry) -> Unit,
    onDirectForDraft: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddProfile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WaveHeader {
            PremiumHeader(logoInCircle = true)
        }
        AddProxyBody(
            proxyStore = proxyStore,
            savedProxies = savedProxies,
            scope = scope,
            draft = draft,
            onApplyToDraft = onApplyToDraft,
            onDirectForDraft = onDirectForDraft,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        )
        PremiumBottomNav(
            selected = null,
            onProfiles = onOpenProfiles,
            onSettings = onOpenSettings,
            onAddProfile = onAddProfile
        )
    }
}

@Composable
private fun AddProxyBody(
    proxyStore: ProxyStore,
    savedProxies: List<ProxyEntry>,
    scope: CoroutineScope,
    draft: Profile?,
    onApplyToDraft: (ProxyEntry) -> Unit,
    onDirectForDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scroll = rememberScrollState()
    var showManual by remember { mutableStateOf(false) }
    var newIp by remember { mutableStateOf("") }
    var newUser by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var testingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var testAllRunning by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val txtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            var added = 0
            var skipped = 0
            try {
                val text = ctx.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.readText() ?: ""
                var counter = proxyStore.proxies.value.mapNotNull {
                    Regex("""^Myproxy #(\d+)$""").find(it.name)
                        ?.groupValues?.getOrNull(1)?.toIntOrNull()
                }.maxOrNull() ?: 0
                val fresh = mutableListOf<ProxyEntry>()
                for (raw in text.lines()) {
                    if (raw.isBlank()) continue
                    val parsed = parseProxyFileLine(raw)
                    if (parsed == null) {
                        skipped++
                        continue
                    }
                    val existing = proxyStore.proxies.value.firstOrNull {
                        it.host == parsed.host && it.port == parsed.port
                    }
                    val name = existing?.name ?: run {
                        counter++
                        "Myproxy #${counter}"
                    }
                    val freshEntry = parsed.copy(name = name)
                    proxyStore.add(freshEntry)
                    if (existing == null) fresh += freshEntry
                    added++
                }
                if (fresh.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        status = "Imported $added — testing…"
                        testAllRunning = true
                    }
                    val results = proxyStore.checkAll(fresh)
                    results.forEach { proxyStore.update(it) }
                    withContext(Dispatchers.Main) {
                        status = "Imported $added — ${results.count { it.ok }} working"
                        testAllRunning = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        status = "Imported $added prox${if (added == 1) "y" else "ies"}" +
                            if (skipped > 0) " ($skipped lines skipped)" else ""
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    status = "Import failed: ${e.message}"
                    testAllRunning = false
                }
            }
        }
    }

    fun testEntry(entry: ProxyEntry, applyAfter: Boolean) {
        testingIds = testingIds + entry.id
        scope.launch {
            val checked = proxyStore.check(entry)
            proxyStore.update(checked)
            testingIds = testingIds - entry.id
            if (applyAfter) {
                if (checked.ok) onApplyToDraft(checked)
                else status = "Proxy failed — not applied"
            } else {
                status = if (checked.ok) "${entry.host} works • ${checked.lastMs} ms"
                else "${entry.host} failed"
            }
        }
    }

    Column(
        modifier = modifier.verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            Spacer(Modifier.height(2.dp))
            // Import from file.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoMintBg)
                    .clickable { txtLauncher.launch(arrayOf("text/plain", "*/*")) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(".txt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Import Proxies from File",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Select a .txt file with your proxies", color = GoGreyText, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GoGreyText)
            }
            status?.let { Text(it, color = GoGreyText, fontSize = 13.sp) }
            // Manual add (expandable, SOCKS5 only).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManual = !showManual },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Tune, null, tint = GoGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add Proxy Manually",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoTitle,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    if (showManual) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = GoTitle
                )
            }
            if (showManual) {
                val suggestedName = nextAutoProxyName(savedProxies)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GoDivider, RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add proxy", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = GoTitle)
                    Text(
                        "SOCKS5 only — the proxy is tested before it is saved.",
                        color = GoGreyText,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = newIp,
                        onValueChange = { newIp = it; formError = null },
                        label = { Text("IP") },
                        placeholder = { Text("198.165.0.0:8080") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newUser,
                        onValueChange = { newUser = it },
                        label = { Text("Username") },
                        placeholder = { Text("Proxy login") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Password") },
                        placeholder = { Text("Proxy password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Proxy name") },
                        placeholder = { Text(suggestedName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        onClick = {
                            val parsed = parseManualIp(newIp) ?: return@Button
                            val (host, port) = parsed
                            val name = newName.trim().ifBlank { suggestedName }
                            adding = true
                            formError = null
                            scope.launch {
                                val checked = proxyStore.check(
                                    ProxyEntry(
                                        name = name, mode = "SOCKS5", host = host,
                                        port = port, user = newUser.trim(), pass = newPass
                                    )
                                )
                                adding = false
                                if (checked.ok) {
                                    proxyStore.add(checked)
                                    newIp = ""
                                    newUser = ""
                                    newPass = ""
                                    newName = ""
                                    showManual = false
                                    if (draft != null) onApplyToDraft(checked)
                                    else status = "Proxy saved ✓"
                                } else {
                                    formError = "Proxy did not answer — check the IP:port and login, then try again."
                                }
                            }
                        },
                        enabled = !adding && parseManualIp(newIp) != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (adding) "Testing…" else "Add proxy") }
                }
            }
            // Direct. v2.8.25: Direct is the default (no proxy) — it
            // shows selected both from a draft and from the tab, exactly
            // as in the reference (green text + green dot).
            val directSelected = draft?.proxyMode != "SOCKS5"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable {
                        if (draft != null) onDirectForDraft()
                        else status = "Open Add or Edit Profile to use Direct"
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Shield, null, tint = GoGreen, modifier = Modifier.size(26.dp))
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Use no proxy (Direct)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (directSelected) GoGreen else GoTitle,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (directSelected) GoGreen else GoDivider, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (directSelected) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(GoGreen)
                        )
                    }
                }
            }
            // Saved proxies header.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Storage, null, tint = GoGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Saved Proxies (${savedProxies.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Tap one to test & apply it at once", color = GoGreyText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = {
                        testAllRunning = true
                        status = "Testing ${savedProxies.size} proxies…"
                        scope.launch {
                            val results = proxyStore.checkAll(savedProxies)
                            results.forEach { proxyStore.update(it) }
                            testAllRunning = false
                            status = "Tested ${results.size} — ${results.count { it.ok }} working"
                        }
                    },
                    enabled = !testAllRunning && savedProxies.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoMintBg,
                        contentColor = GoGreenDark
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Test All", fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, softWrap = false)
                }
            }
            if (testAllRunning && savedProxies.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoGreen, strokeWidth = 4.dp, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Testing proxies…", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoTitle)
                        Spacer(Modifier.height(4.dp))
                        Text("Checking flags, IP and speed", fontSize = 11.sp, color = GoGreyText)
                    }
                }
            }
            AnimatedVisibility(
                visible = !testAllRunning,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (savedProxies.isEmpty()) {
                        Text("No saved proxies yet.", color = GoGreyText)
                    }
                    for (entry in savedProxies) {
                        ProxyRow(
                            entry = entry,
                            selected = draft?.proxyHost == entry.host && draft?.proxyPort == entry.port && draft?.proxyMode != "NONE",
                            testing = testingIds.contains(entry.id),
                            onTap = { testEntry(entry, applyAfter = draft != null) },
                            onTest = { testEntry(entry, applyAfter = false) },
                            onDelete = { proxyStore.remove(entry.id) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
    }
}

/** v2.8.24: one saved proxy row on the Add Proxy screen. */
@Composable
private fun ProxyRow(
    entry: ProxyEntry,
    selected: Boolean,
    testing: Boolean,
    onTap: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val code = proxyCountryCode(entry.country)
    val flag = if (code != null) Profile.flagEmoji(code) else "🌐"
    val countryLabel = if (entry.country.isBlank()) "Unknown"
    else entry.country.substringAfter(" • ", entry.country)
    val tested = entry.lastMs > 0L || entry.lastIp.isNotBlank()
    val ipLine = if (entry.lastIp.isNotBlank()) "IP ${entry.lastIp} • ${entry.lastMs} ms"
    else if (tested) "Test failed — tap to retry"
    else "Not tested yet — tap play"
    val ready = entry.ok && entry.lastIp.isNotBlank()
    val failed = tested && !ready

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                if (selected) 2.dp else 0.dp,
                if (selected) GoGreen else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onTap)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 26.sp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "SOCKS5 ${entry.host}:${entry.port}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GoTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = GoGreyText, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
                Text(countryLabel, fontSize = 10.sp, color = GoGreyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, null, tint = GoGreyText, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
                Text(ipLine, fontSize = 9.sp, color = GoGreyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // The status pill sits under the IP line so the host:port
            // gets the row's full width instead of being cut off.
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (ready) Color(0xFFD9F2E5)
                        else if (failed) Color(0xFFFDE8E7) else Color(0xFFF1F3F4)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (ready) Icons.Filled.Check
                    else if (failed) Icons.Filled.Close else Icons.Filled.Schedule,
                    null,
                    tint = if (ready) GoGreenDark
                    else if (failed) Color(0xFFD93025) else GoGreyText,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (ready) "Ready" else if (failed) "Failed" else "Not tested",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ready) GoGreenDark
                    else if (failed) Color(0xFFD93025) else GoGreyText,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, GoGreen, CircleShape)
                        .clickable(onClick = onTest),
                    contentAlignment = Alignment.Center
                ) {
                    if (testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = GoGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.PlayArrow, null, tint = GoGreen, modifier = Modifier.size(12.dp))
                    }
                }
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = GoTitle, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFD93025)) },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
    }
}

/** v2.8.24: the Settings screen — export / import / about + the JS-level notice. */
@Composable
private fun SettingsScreen(
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
    notice: String?,
    onOpenProfiles: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddProfile: () -> Unit
) {
    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        WaveHeader {
            PremiumHeader(logoInCircle = true)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(GoMintBg)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Settings, null, tint = GoGreenDark, modifier = Modifier.size(48.dp))
            }
            Text(
                "Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 27.sp,
                color = GoTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Customize your NovaProfile experience",
                color = GoGreyText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            SettingsRow(
                icon = { Icon(Icons.Filled.FileUpload, null, tint = GoGreenDark, modifier = Modifier.size(24.dp)) },
                title = "Export All Profiles",
                subtitle = "Save your profiles to a .zip file",
                onClick = onExport
            )
            SettingsRow(
                icon = { Icon(Icons.Filled.FileDownload, null, tint = GoGreenDark, modifier = Modifier.size(24.dp)) },
                title = "Import Profiles",
                subtitle = "Load profiles from a .zip file",
                onClick = onImport
            )
            SettingsRow(
                icon = { Icon(Icons.Filled.Info, null, tint = GoGreenDark, modifier = Modifier.size(24.dp)) },
                title = "About NovaProfile",
                // v2.8.25: the reference label, exactly as screenshotted.
                subtitle = "Version 1.0.0 • Build 1000",
                onClick = onAbout
            )
            notice?.let { Text(it, color = GoGreyText, fontSize = 13.sp) }
            // Fingerprint notice card.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoMintBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Shield, null, tint = GoGreen, modifier = Modifier.size(56.dp))
                    Icon(Icons.Filled.Fingerprint, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(64.dp)
                        .background(GoGreyText.copy(alpha = 0.4f))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Fingerprint protection is JS-level inside the system WebView.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoTitle
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "It is NOT a Chromium fork — advanced fingerprint tests can still detect the real device.",
                        fontSize = 10.sp,
                        color = GoGreyText
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        PremiumBottomNav(
            selected = HomeRoute.SETTINGS,
            onProfiles = onOpenProfiles,
            onSettings = onOpenSettings,
            onAddProfile = onAddProfile
        )
    }
}

/** v2.8.24: a Settings row — mint icon tile, title, subtitle, chevron. */
@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GoMintBg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = GoGreyText, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GoGreyText)
    }
}

/** A bordered row with a label, optional sub-text and a chevron. */
@Composable
private fun SectionRow(label: String, sub: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GoDivider, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold)
            sub?.let { Text(it, color = GoGreyText, fontSize = 13.sp) }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GoGreyText)
    }
}

/** A row of single-choice chips. */
@Composable
private fun ChoiceChips(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for ((value, label) in options) {
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}

/** A label + switch row. */
@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (o in options) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(o) },
                    onClick = { onSelect(o); expanded = false }
                )
            }
        }
    }
}

/**
 * Country proxy picker (v2.6): the reference Add proxy screen, now
 * inside Add profile. Tap a country → fetch + test free proxies for it
 * (fastest working wins) → "Proxy is ok!" → Add proxy applies it to
 * the draft.
 */
@Composable
private fun ProxyPickerView(
    proxyStore: ProxyStore,
    scope: CoroutineScope,
    connectingCountry: String?,
    onConnectingCountry: (String?) -> Unit,
    countryStatus: String?,
    onCountryStatus: (String?) -> Unit,
    onPicked: (ProxyEntry, Profile.CountrySettings) -> Unit,
    onBack: () -> Unit
) {
    var pickedProxy by remember { mutableStateOf<ProxyEntry?>(null) }
    var pickedCode by remember { mutableStateOf<String?>(null) }
    var pickingError by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Add proxy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Text(
            "Pick a country — the app fetches and tests free proxies for it.",
            color = GoGreyText
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GoDivider, RoundedCornerShape(12.dp))
        ) {
            for (code in Profile.pickerCountries) {
                val cs = Profile.countrySettings(code)
                val expanded = connectingCountry == code || pickedCode == code
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = connectingCountry == null) {
                            pickedProxy = null
                            pickedCode = code
                            pickingError = null
                            onConnectingCountry(code)
                            onCountryStatus("Fetching $code proxies…")
                            scope.launch {
                                val candidates = proxyStore.fetchFreeProxies(code, limit = 10)
                                if (candidates.isEmpty()) {
                                    pickingError =
                                        "Could not fetch any free ${cs.label} proxies — " +
                                            "check connection and try again."
                                } else {
                                    onCountryStatus("Testing ${candidates.size} ${cs.label} proxies…")
                                    val working = proxyStore.checkFirstWorking(candidates)
                                    if (working != null) {
                                        pickedProxy = working
                                        onCountryStatus(
                                            "${cs.label} connected • ${working.lastIp} • " +
                                                "${working.lastMs} ms"
                                        )
                                    } else {
                                        pickingError =
                                            "No working free ${cs.label} proxy right now — " +
                                                "free proxies die fast. Try again."
                                    }
                                }
                                onConnectingCountry(null)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Profile.flagEmoji(code), fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(cs.label, modifier = Modifier.weight(1f))
                    Icon(
                        if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = GoGreyText
                    )
                }
                // v2.7.1: search + result expand UNDER the tapped country
                // (user order) — no more result card pinned at the bottom.
                if (expanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(GoDivider)
                    )
                    if (connectingCountry == code) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(countryStatus ?: "Checking…", color = GoGreyText)
                        }
                    } else if (pickedCode == code && pickedProxy != null) {
                        val wp = pickedProxy!!
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Proxy is ok! • ${wp.lastIp} • ${wp.lastMs} ms" +
                                    (if (wp.lastTimezone.isNotBlank()) " • ${wp.lastTimezone}" else ""),
                                color = GoGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text("HTTP • Free", color = GoGreyText)
                            Button(
                                onClick = { onPicked(wp, cs) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Add proxy") }
                        }
                    } else if (pickedCode == code) {
                        pickingError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Extended settings (v2.6): the 5 sub-screens. */
@Composable
private fun ExtendedSectionView(
    section: String,
    draft: Profile,
    onDraft: (Profile) -> Unit,
    onBack: () -> Unit,
    onOpenProxyPicker: () -> Unit,
    onCheckProxy: (Profile) -> Unit,
    onClearNotice: () -> Unit,
    proxyCheckResult: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                when (section) {
                    "overview" -> "Profile Overview"
                    "proxy" -> "Proxy & Location"
                    "browser" -> "Browser"
                    "storage" -> "Storage"
                    else -> "Hardware"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        when (section) {
            "overview" -> OverviewSection(draft, onDraft)
            "proxy" -> ProxyLocationSection(
                draft, onDraft, onOpenProxyPicker,
                onCheckProxy, onClearNotice, proxyCheckResult
            )
            "browser" -> BrowserSection(draft, onDraft)
            "storage" -> StorageSection(draft, onDraft)
            else -> HardwareSection(draft, onDraft)
        }
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OverviewSection(draft: Profile, onDraft: (Profile) -> Unit) {
    OutlinedTextField(
        value = draft.name, onValueChange = { onDraft(draft.copy(name = it)) },
        label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    OutlinedTextField(
        value = draft.notes, onValueChange = { onDraft(draft.copy(notes = it)) },
        label = { Text("Note") }, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = draft.folder, onValueChange = { onDraft(draft.copy(folder = it)) },
        label = { Text("Folder") }, modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    DropDownField("Operating System", Profile.OS_OPTIONS, draft.os) { os ->
        val (vendor, renderer) = Profile.osWebgl(os)
        onDraft(
            draft.copy(
                os = os,
                userAgent = if (draft.userAgentPreset == "Custom") draft.userAgent
                else Profile.osUserAgent(os),
                webglVendor = vendor,
                webglRenderer = renderer
            )
        )
    }
    Button(
        onClick = { onDraft(Profile.randomFingerprint(draft)) },
        colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
        modifier = Modifier.fillMaxWidth()
    ) { Text("NEW FINGERPRINT") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GoDivider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Fingerprint data", fontWeight = FontWeight.Bold)
        Text("Resolution  ${draft.screenWidth}x${draft.screenHeight}", color = GoGreyText)
        Text("OS  ${draft.os}", color = GoGreyText)
        Text("Languages  ${draft.language}", color = GoGreyText)
        Text("Fonts  ${draft.fontsList.split(",").size} fonts", color = GoGreyText)
        Text(
            "User-agent  ${if (draft.userAgent.isNotBlank()) draft.userAgent else Profile.defaultUserAgent(draft.userAgentPreset)}",
            color = GoGreyText
        )
        val canvasSpoofed = draft.canvasMode == "noise" || draft.canvasMode == "block"
        val spoofed = canvasSpoofed || draft.webglMode == "MASKED" || draft.audioMode == "MASKED"
        Text(
            if (spoofed) "Fingerprint: Unique ✓" else "Fingerprint: Real (not spoofed)",
            color = if (spoofed) GoGreenDark else GoGreyText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProxyLocationSection(
    draft: Profile,
    onDraft: (Profile) -> Unit,
    onOpenProxyPicker: () -> Unit,
    onCheckProxy: (Profile) -> Unit,
    onClearNotice: () -> Unit,
    proxyCheckResult: String?
) {
    Text("Proxy", fontWeight = FontWeight.Bold)
    SectionRow(
        label = if (draft.proxyMode == "NONE") "Connect proxy"
        else "${draft.proxyMode} ${draft.proxyHost}:${draft.proxyPort}",
        sub = if (draft.proxyMode == "NONE") "Tap to pick a country"
        else "Timezone ${draft.timezone}",
        onClick = onOpenProxyPicker
    )
    Button(onClick = { onClearNotice(); onCheckProxy(draft) }) {
        Text("Check proxy")
    }
    proxyCheckResult?.let { Text(it) }
    Text("Timezone", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("IP" to "Fill based on IP", "CUSTOM" to "Custom"),
        selected = draft.timezoneMode,
        onSelect = { onDraft(draft.copy(timezoneMode = it)) }
    )
    if (draft.timezoneMode == "CUSTOM") {
        DropDownField("Timezone", Profile.TIMEZONE_OPTIONS, draft.timezone) {
            onDraft(draft.copy(timezone = it))
        }
    } else {
        Text("Current: ${draft.timezone} (from the proxy IP)", color = GoGreyText)
    }
    Text("Geolocation", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("IP" to "Based on IP", "OFF" to "Off"),
        selected = draft.geoMode,
        onSelect = { onDraft(draft.copy(geoMode = it)) }
    )
    if (draft.geoMode == "IP") {
        Text(
            if (draft.latitude != null && draft.longitude != null)
                "Current: ${draft.latitude}, ${draft.longitude} (from the proxy IP)"
            else "Set when a proxy connects or the profile runs.",
            color = GoGreyText
        )
    }
    Text("WebRTC", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("disable" to "Disabled", "real" to "Real"),
        selected = draft.webrtcMode,
        onSelect = { onDraft(draft.copy(webrtcMode = it)) }
    )
    Text("Disabled is safest — Real can leak your real IP through the proxy.", color = GoGreyText)
    Text("Custom DNS", fontWeight = FontWeight.Bold)
    Text("Based on ip — DNS resolves through the proxy.", color = GoGreyText)
}

@Composable
private fun BrowserSection(draft: Profile, onDraft: (Profile) -> Unit) {
    DropDownField(
        "User-agent preset", Profile.USER_AGENT_PRESETS,
        draft.userAgentPreset
    ) { onDraft(draft.copy(userAgentPreset = it)) }
    if (draft.userAgentPreset == "Custom") {
        OutlinedTextField(
            value = draft.userAgent,
            onValueChange = { onDraft(draft.copy(userAgent = it)) },
            label = { Text("Custom user-agent") },
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Text(
            "User-agent: ${if (draft.userAgent.isNotBlank()) draft.userAgent else Profile.defaultUserAgent(draft.userAgentPreset)}",
            color = GoGreyText
        )
    }
    OutlinedTextField(
        value = draft.homeUrl, onValueChange = { onDraft(draft.copy(homeUrl = it)) },
        label = { Text("Start URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    Text("Languages", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("IP" to "IP based", "CUSTOM" to "Custom"),
        selected = draft.languagesMode,
        onSelect = { onDraft(draft.copy(languagesMode = it)) }
    )
    if (draft.languagesMode == "CUSTOM") {
        OutlinedTextField(
            value = draft.language, onValueChange = { onDraft(draft.copy(language = it)) },
            label = { Text("Language (e.g. en-US)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
    } else {
        Text("Current: ${draft.language} (from the proxy country)", color = GoGreyText)
    }
    Text("Fonts", fontWeight = FontWeight.Bold)
    SwitchRow(
        if (draft.fontsMasking) "Masked" else "Real",
        draft.fontsMasking
    ) { onDraft(draft.copy(fontsMasking = it)) }
    Text("Masking hides unusual fonts that could fingerprint the profile.", color = GoGreyText)
    if (draft.fontsMasking) {
        val selected = draft.fontsList.split(",").map { it.trim() }
            .filter { it.isNotEmpty() }.toSet()
        for (font in Profile.FONT_OPTIONS) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newList = if (selected.contains(font)) selected - font
                        else selected + font
                        onDraft(draft.copy(fontsList = newList.joinToString(",")))
                    }
            ) {
                Checkbox(checked = selected.contains(font), onCheckedChange = null)
                Text(font)
            }
        }
    } else {
        Text("Real fonts installed on this device are reported — no selection needed.", color = GoGreyText)
    }
}

@Composable
private fun StorageSection(draft: Profile, onDraft: (Profile) -> Unit) {
    SwitchRow("Save history", draft.saveHistory) {
        onDraft(draft.copy(saveHistory = it))
    }
    SwitchRow("Save bookmarks", draft.saveBookmarks) {
        onDraft(draft.copy(saveBookmarks = it))
    }
    SwitchRow("Local storage and indexedDB", draft.domStorage) {
        onDraft(draft.copy(domStorage = it))
    }
    Text(
        "Lock session, Google services, passwords, plugins and extensions " +
            "are not available in the system WebView — they are not shown here.",
        color = GoGreyText
    )
}

@Composable
private fun HardwareSection(draft: Profile, onDraft: (Profile) -> Unit) {
    val resOptions = Profile.RESOLUTIONS.map { "${it.first}x${it.second}" }
    val resCurrent = "${draft.screenWidth}x${draft.screenHeight}"
    DropDownField(
        "Resolution",
        if (resOptions.contains(resCurrent)) resOptions else resOptions + resCurrent,
        resCurrent
    ) { v ->
        val parts = v.split("x")
        onDraft(
            draft.copy(
                screenWidth = parts[0].toIntOrNull() ?: draft.screenWidth,
                screenHeight = parts.getOrNull(1)?.toIntOrNull() ?: draft.screenHeight
            )
        )
    }
    DropDownField(
        "Device memory",
        listOf("1 GB", "2 GB", "4 GB", "8 GB"),
        "${draft.deviceMemory} GB"
    ) { v ->
        onDraft(draft.copy(deviceMemory = v.removeSuffix(" GB").toIntOrNull() ?: draft.deviceMemory))
    }
    DropDownField(
        "Number of threads",
        Profile.THREAD_COUNTS.map { it.toString() },
        draft.hardwareConcurrency.toString()
    ) { v ->
        onDraft(draft.copy(hardwareConcurrency = v.toIntOrNull() ?: draft.hardwareConcurrency))
    }
    Text("Media devices", fontWeight = FontWeight.Bold)
    SwitchRow(
        if (draft.mediaMasked) "Masked • 0/0/0" else "Real",
        draft.mediaMasked
    ) { onDraft(draft.copy(mediaMasked = it)) }
    Text("Canvas", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("real" to "Real", "noise" to "Noise", "block" to "Block"),
        selected = draft.canvasMode,
        onSelect = { onDraft(draft.copy(canvasMode = it)) }
    )
    Text("WebGL image", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("REAL" to "Real", "MASKED" to "Masked"),
        selected = draft.webglMode,
        onSelect = { onDraft(draft.copy(webglMode = it)) }
    )
    if (draft.webglMode == "MASKED") {
        Text("WebGL metadata", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = draft.webglVendor, onValueChange = { onDraft(draft.copy(webglVendor = it)) },
            label = { Text("WebGL vendor") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = draft.webglRenderer, onValueChange = { onDraft(draft.copy(webglRenderer = it)) },
            label = { Text("WebGL renderer") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
    }
    Text("Audio context", fontWeight = FontWeight.Bold)
    ChoiceChips(
        options = listOf("REAL" to "Real", "MASKED" to "Masked"),
        selected = draft.audioMode,
        onSelect = { onDraft(draft.copy(audioMode = it)) }
    )
}
