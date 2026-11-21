// File-level opt-in (Run #5 fix): TopAppBar / ExposedDropdownMenuBox /
// FilterChip are @ExperimentalMaterial3Api in the BOM 2026.08.00
// material3 version — the per-function @OptIn missed EditorScreen's
// TopAppBar (line 421), failing :app:compileReleaseKotlin.
// NOTE (Run #7): a @file: annotation MUST sit before the package line —
// after it, the parser rejects the imports ("imports are only allowed in
// the beginning of file"). That was my placement mistake.
@file:OptIn(ExperimentalMaterial3Api::class)
package com.novaprofiles.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.novaprofiles.app.data.Profile
import com.novaprofiles.app.util.ProxyHelper
import com.novaprofiles.app.util.NovaProfileSession
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.mode.AppLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NovaProfile v2.8.2: status bar 100% white on every screen —
        // solid white background, dark icons. (DDG activities get the
        // same treatment in DuckDuckGoActivity.)
        window.statusBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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
        setContent {
            NovaProfileTheme {
                MainScreen(vm = vm, autoRunProfileId = autoRunProfileId, autoRunQuery = autoRunQuery)
            }
        }
    }
}

/** Profile templates (v2.0 Pro): one tap fills home URL + identity. */
// v2.7.2: templates removed with the Add-profile simplification —
// new profiles are auto-numbered and the proxy pick sets the identity.

/**
 * v2.7.2: the card flag comes from the profile's stored proxy country
 * first — that is the real proxy. Language is only a fallback (old
 * profiles created before proxyCountry existed).
 * v2.8.5: any 2-letter proxy country wins — the old pickerCountries gate
 * showed the wrong flag for proxy countries outside that list (a Poland
 * proxy showed the US flag).
 */
fun countryOf(profile: Profile): Pair<String, String> {
    val pc = profile.proxyCountry.trim().uppercase()
    if (pc.length == 2) {
        return Profile.flagEmoji(pc) to pc
    }
    return when {
        profile.language.startsWith("en-GB") -> "🇬🇧" to "GB"
        profile.language.startsWith("en-CA") -> "🇨🇦" to "CA"
        profile.language.startsWith("fr") -> "🇫🇷" to "FR"
        profile.language.startsWith("de") -> "🇩🇪" to "DE"
        profile.language.startsWith("es") -> "🇪🇸" to "ES"
        profile.language.startsWith("pt") -> "🇧🇷" to "BR"
        profile.language.startsWith("it") -> "🇮🇹" to "IT"
        profile.language.startsWith("nl") -> "🇳🇱" to "NL"
        profile.language.startsWith("ja") -> "🇯🇵" to "JP"
        profile.language.startsWith("ko") -> "🇰🇷" to "KR"
        profile.language.startsWith("zh") -> "🇨🇳" to "CN"
        profile.language.startsWith("ru") -> "🇷🇺" to "RU"
        profile.language.startsWith("pl") -> "🇵🇱" to "PL"
        profile.language.startsWith("tr") -> "🇹🇷" to "TR"
        profile.language.startsWith("ar") -> "🇸🇦" to "SA"
        else -> "🇺🇸" to "US"
    }
}

private enum class HomeTab { PROFILES, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: ProfileViewModel, autoRunProfileId: Long? = null, autoRunQuery: String? = null) {
    val profiles by vm.profiles.collectAsState()
    // v2.7.2: trash is gone — no trash list, no showTrash state.
    val folders by vm.folders.collectAsState()
    val query by vm.query.collectAsState()
    val selectedFolder by vm.selectedFolder.collectAsState()
    val proxyCheck by vm.proxyCheckResult.collectAsState()
    val importResult by vm.importResult.collectAsState()
    val exportResult by vm.exportResult.collectAsState()

    val ctx = LocalContext.current
    var editing by remember { mutableStateOf<Profile?>(null) }
    var creating by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Profile?>(null) }
    var tab by remember { mutableStateOf(HomeTab.PROFILES) }
    var runningIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // v2.6: profiles whose proxy location is being re-synced before launch.
    var syncingIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // v2.8.1: the profile whose last launch failed — its result is shown
    // on its own card (never at the bottom of the screen).
    var launchFailedId by remember { mutableStateOf<Long?>(null) }
    var searchOpen by remember { mutableStateOf(false) }
    var labProfile by remember { mutableStateOf<Profile?>(null) }
    val proxyStore = remember { ProxyStore(ctx) }
    // v2.8.6: the card shows the saved proxy's measured IP + speed — the
    // entry is looked up by host:port, no Room change.
    val savedProxies by proxyStore.proxies.collectAsState()
    val scope = rememberCoroutineScope()

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

    /** DDG port: prepare the profile, then open DuckDuckGo's browser
     *  with it. prepare() applies the per-profile WebView data dir +
     *  proxy override and waits (v2.7.1 lesson: a WebView created
     *  before the override lands leaks the real IP). Fail-closed: a
     *  proxy that cannot be applied means no browser at all. */
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
        ctx.startActivity(
            BrowserActivity.intent(ctx, launchSource = AppLauncher, queryExtra = query).apply {
                putExtra(NovaProfileSession.EXTRA_RELOAD_TAB, true)
            }
        )
        return true
    }

    /**
     * The card Run tap, shared by the tap and the post-restart
     * auto-run (v2.8.17): re-sync the proxy IP's timezone +
     * coordinates first when the profile follows the IP, then launch
     * it — with the fresh values, never the stale stored ones
     * (v2.8.2). The optional query is only for the fingerprint
     * lab's whoer check after a restart.
     */
    fun runProfile(p: Profile, query: String? = null) {
        launchFailedId = null
        // v2.6 smart 90% fix: before opening the browser,
        // re-resolve the proxy IP's CURRENT timezone +
        // coordinates (one ip-api call through the proxy)
        // and save them, so the run never uses a stale
        // timezone. Free US proxies sit in Mountain View
        // (America/Los_Angeles) while the country default
        // is New York — a 3-hour mismatch whoer deducts
        // for. On lookup failure, launch with the stored
        // values instead of blocking.
        // v2.8.18: skip the re-sync when this exact proxy was already
        // resolved and the profile already carries its location. The
        // editor test does that seconds before Save, so re-resolving on
        // every Play tap added up to 12s of blank spinner (6s connect
        // + 6s read through a slow proxy) for zero benefit — the user
        // read it, reasonably, as "browser never opens". Only re-sync
        // when the saved entry is untested or its timezone no longer
        // matches the profile (the proxy rotated to a new city).
        val savedEntry = if (p.proxyMode != "NONE") {
            proxyStore.proxies.value.firstOrNull {
                it.host == p.proxyHost && it.port == p.proxyPort
            }
        } else null
        val alreadySynced = savedEntry != null && savedEntry.ok &&
            savedEntry.lastIp.isNotBlank() &&
            savedEntry.lastTimezone.isNotBlank() &&
            (p.timezoneMode != "IP" || savedEntry.lastTimezone == p.timezone)
        if (p.proxyMode != "NONE" &&
            (p.timezoneMode == "IP" || p.geoMode == "IP") && !alreadySynced
        ) {
            syncingIds = syncingIds + p.id
            scope.launch {
                try {
                    val entry = ProxyEntry(
                        name = p.name,
                        mode = p.proxyMode, host = p.proxyHost,
                        port = p.proxyPort, user = p.proxyUser,
                        pass = p.proxyPass
                    )
                    val loc = proxyStore.resolveLocation(entry)
                    // v2.8.2: launch with the FRESH profile — the old
                    // code saved the re-synced timezone to Room but
                    // launched the browser with the STALE profile, so
                    // whoer saw a timezone mismatch (78% instead of
                    // ~90%).
                    var toLaunch = p
                    if (loc != null) {
                        val updated = p.copy(
                            timezone = if (p.timezoneMode == "IP") loc.timezone else p.timezone,
                            latitude = if (p.geoMode == "IP" && loc.lat != 0.0) loc.lat else p.latitude,
                            longitude = if (p.geoMode == "IP" && loc.lon != 0.0) loc.lon else p.longitude
                        )
                        vm.updateNow(updated)
                        toLaunch = updated
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
                    if (launchProfile(toLaunch, query)) {
                        runningIds = runningIds + p.id
                        vm.markRun(p.id)
                    }
                } catch (e: Exception) {
                    // v2.8.18: never strand the card on a spinner — any
                    // failure in the sync launches with the stored
                    // values instead of dying mid-sync.
                    try {
                        if (launchProfile(p, query)) {
                            runningIds = runningIds + p.id
                            vm.markRun(p.id)
                        }
                    } catch (e2: Exception) {
                    }
                } finally {
                    syncingIds = syncingIds - p.id
                }
            }
        } else {
            scope.launch {
                if (launchProfile(p, query)) {
                    runningIds = runningIds + p.id
                    vm.markRun(p.id)
                }
            }
        }
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

    // Fingerprint Lab (v2.0 Pro) — full-screen check for one profile.
    labProfile?.let { lp ->
        FingerprintLabScreen(profile = lp, onBack = { labProfile = null })
        return
    }

    // v2.7.2: the next auto-numbered name for a new profile — the max
    // existing "Profile N" + 1, so a deleted number is never reused.
    val nextProfileName = run {
        val max = profiles.mapNotNull {
            Regex("""^Profile (\d+)$""").find(it.name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        "Profile ${max + 1}"
    }

    // Add/Edit profile page — v2.8.13: a full-screen page in THIS
    // activity, not a Dialog. The Dialog had its own window whose status
    // bar stayed grey through two fixes (v2.8.7 SideEffect read a null
    // parent before attach; v2.8.12 attach listener never fired on the
    // dialog's parent). In the activity window the bar is already white
    // (onCreate). Back press cancels, like the Dialog's dismiss.
    if (editing != null || creating) {
        EditorScreen(
            initial = editing,
            nextProfileName = nextProfileName,
            onSave = { p ->
                if (editing != null) vm.update(p) else vm.create(p)
                editing = null
                creating = false
            },
            onCancel = { editing = null; creating = false },
            onCheckProxy = { p -> vm.checkProxy(p, "https://api.ipify.org") },
            proxyCheckResult = proxyCheck,
            onClearNotice = { vm.clearNotices() },
            // v2.8.2: Delete + Check fingerprint moved here from the
            // card's 3-dot menu (menu removed, user order).
            onDelete = { pendingDelete = it },
            onCheckFingerprint = { labProfile = it },
            proxyStore = proxyStore
        )
        return
    }

    Scaffold(
        bottomBar = {
            // GoLogin-style bottom: pill nav (Profiles / Settings)
            // + green "Add profile" button.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFF1F3F4))
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = { Icon(Icons.Outlined.Person, null, Modifier.size(22.dp)) },
                        label = "Profiles",
                        selected = tab == HomeTab.PROFILES,
                        onClick = { tab = HomeTab.PROFILES }
                    )
                    BottomNavItem(
                        icon = { Icon(Icons.Filled.Settings, null, Modifier.size(22.dp)) },
                        label = "Settings",
                        selected = tab == HomeTab.SETTINGS,
                        onClick = { tab = HomeTab.SETTINGS }
                    )
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { creating = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Add, null, Modifier.size(20.dp))
                        Text("Add profile", fontSize = 11.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // v2.8.17: post-restart auto-run — the fresh process runs
            // the profile named by the restart intent, once profiles
            // have loaded from Room; the flag stops a re-run on
            // recomposition.
            var autoRunDone by remember { mutableStateOf(false) }
            LaunchedEffect(autoRunProfileId, profiles) {
                if (!autoRunDone && autoRunProfileId != null && profiles.isNotEmpty()) {
                    autoRunDone = true
                    profiles.find { it.id == autoRunProfileId }?.let { runProfile(it, autoRunQuery) }
                }
            }
            when (tab) {
                HomeTab.PROFILES -> ProfilesTab(
                    vm = vm,
                    profiles = profiles,
                    folders = folders,
                    query = query,
                    selectedFolder = selectedFolder,
                    searchOpen = searchOpen,
                    onSearchOpen = { searchOpen = it },
                    runningIds = runningIds,
                    syncingIds = syncingIds,
                    onRun = { p -> runProfile(p) },
                    onStop = { p -> stopProfile(p.id) },
                    onStopAll = { stopProfile(null) },
                    onEdit = { editing = it },
                    onShowAbout = { showAbout = true },
                    noticeProfileId = launchFailedId,
                    noticeText = proxyCheck,
                    onDismissNotice = { vm.clearNotices(); launchFailedId = null },
                    proxyEntryFor = { p ->
                        savedProxies.firstOrNull {
                            it.host == p.proxyHost && it.port == p.proxyPort
                        }
                    }
                )
                HomeTab.SETTINGS -> SettingsTab(
                    onExport = { exportLauncher.launch("novaprofile-profiles.zip") },
                    onImport = { importLauncher.launch(arrayOf("*/*")) },
                    onAbout = { showAbout = true },
                    notice = importResult ?: exportResult
                )
            }
        }
    }


    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About NovaProfile") },
            text = {
                Text(
                    "NovaProfile 2.8.18 Pro — original multi-profile browser.\n\n" +
                        "• Unlimited profiles, everything free, no billing, no trial.\n" +
                        "• Per-profile cookies, proxy, user-agent, language, " +
                        "screen, WebGL and Canvas settings.\n" +
                        "• Multi-tab browser with history, bookmarks, downloads, " +
                        "desktop mode, find in page and translate.\n" +
                        "• Per-profile manual proxy, Fingerprint Lab check, sessions.\n" +
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
                    // v2.8.2: Delete is only reachable from the editor
                    // now — close the editor too, or it would keep
                    // showing a deleted profile.
                    editing = null
                    creating = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
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
            fontSize = 10.sp,
            maxLines = 1,
            color = if (selected) GoGreen else GoGreyText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ProfilesTab(
    vm: ProfileViewModel,
    profiles: List<Profile>,
    folders: List<String>,
    query: String,
    selectedFolder: String,
    searchOpen: Boolean,
    onSearchOpen: (Boolean) -> Unit,
    runningIds: Set<Long>,
    syncingIds: Set<Long> = emptySet(),
    onRun: (Profile) -> Unit,
    onStop: (Profile) -> Unit,
    onStopAll: () -> Unit,
    onEdit: (Profile) -> Unit,
    onShowAbout: () -> Unit,
    noticeProfileId: Long? = null,
    noticeText: String? = null,
    onDismissNotice: () -> Unit = {},
    proxyEntryFor: (Profile) -> ProxyEntry? = { null }
) {
    Column(Modifier.fillMaxSize()) {
        // Account row: green avatar + name + search.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoGreen),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Spacer(Modifier.width(10.dp))
            // v2.7.2: home header is just the name (+ "Connected" while
            // a profile runs) — search icon, info icon and the search
            // field are removed (user order); About stays in Settings.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "NovaProfile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1
                )
                // v2.8.18: profile count under the app name, as asked.
                Text(
                    "Profile: ${profiles.size}",
                    color = GoGreyText,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
            // v2.3: GoLogin shows "Connected" up here when a profile runs.
            if (runningIds.isNotEmpty()) {
                Text("Connected", color = GoGreyText, fontSize = 14.sp)
            }
        }
        // v2.7.2: folder row removed (user order) — folder icon + All +
        // folder pills are gone; every profile lives in General.
        Spacer(Modifier.height(4.dp))

        // Sessions banner (v2.0 Pro): running profiles + Stop all.
        if (runningIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoGreen)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${runningIds.size} profile${if (runningIds.size > 1) "s" else ""} running",
                    color = GoGreenDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onStopAll) { Text("Stop all", color = GoStopRed) }
            }
        }

        val shown = profiles
        if (shown.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No profiles yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tap Add profile to create your first profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoGreyText
                )
            }
        } else {
            LazyColumn {
                items(shown, key = { it.id }) { p ->
                    GoProfileRow(
                        profile = p,
                        running = runningIds.contains(p.id),
                        syncing = syncingIds.contains(p.id),
                        onRun = { onRun(p) },
                        onStop = { onStop(p) },
                        onEdit = { onEdit(p) },
                        notice = if (noticeProfileId == p.id) noticeText else null,
                        onDismissNotice = onDismissNotice,
                        proxyEntry = proxyEntryFor(p)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoProfileRow(
    profile: Profile,
    running: Boolean,
    syncing: Boolean = false,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    notice: String? = null,
    onDismissNotice: () -> Unit = {},
    proxyEntry: ProxyEntry? = null
) {
    val (flag, code) = countryOf(profile)
    val connection = when (profile.proxyMode) {
        "NONE" -> "Direct"
        "HTTP" -> "HTTP proxy"
        "SOCKS5" -> "SOCKS5 proxy"
        else -> profile.proxyMode
    }
    // v2.8.5: the card shows the CONNECTED proxy's country — flag +
    // name — plus the proxy itself (mode + host:port). A Direct profile
    // shows no country flag at all instead of a misleading one.
    val hasProxy = profile.proxyMode != "NONE" && profile.proxyHost.isNotBlank()
    val cardFlag = if (hasProxy) flag else "🌐"
    val cardLine = if (hasProxy) {
        "$cardFlag  ${Profile.countrySettings(code).label} • $connection"
    } else {
        "$cardFlag  $connection"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (running) GoMintBg else Color.White)
            // v2.8.2: tap the card to open / return to the browser —
            // the editor moved to the pencil. Disabled while the
            // location re-sync is running, so a tap cannot double-launch.
            .clickable(enabled = !syncing) { onRun() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GoGreen)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (running) GoGreenDark else Color.Black
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    cardLine,
                    fontSize = 14.sp,
                    color = if (running) GoGreenDark else GoGreyText
                )
                if (hasProxy) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${profile.proxyMode} ${profile.proxyHost}:${profile.proxyPort}",
                        fontSize = 13.sp,
                        color = if (running) GoGreenDark else GoGreyText
                    )
                }
                // v2.8.6: proxy IP + speed + location on the card — the
                // exit IP and latency come from the saved proxy's last
                // check (refreshed on every run), the place from the
                // profile's IP-matched timezone.
                if (hasProxy && proxyEntry?.ok == true && proxyEntry.lastIp.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "IP ${proxyEntry.lastIp} • ${proxyEntry.lastMs} ms • ${profile.timezone.substringAfter("/").replace("_", " ")}",
                        fontSize = 13.sp,
                        color = if (running) GoGreenDark else GoGreyText
                    )
                }
            }
            // v2.8.2: the grey Android icon and the 3-dot menu are gone
            // (user order). A running profile shows an Eye — tap it (or
            // the card) to jump back into the open browser on the same
            // page. A stopped profile shows the green Play. Edit is the
            // pencil; Delete and Check fingerprint live in the editor.
            if (running) {
                IconButton(onClick = onRun, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Filled.Visibility,
                        contentDescription = "View browser",
                        tint = GoGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GoStopRed)
                        .clickable { onStop() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Stop, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            } else if (syncing) {
                // v2.6: location re-sync in progress — spinner, no double tap.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, GoGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GoGreen,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, GoGreen, CircleShape)
                        .clickable { onRun() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = GoGreen, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = GoGreyText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        // v2.8.1: the folder ("General") and "new" pills are removed
        // (user order) — only a real note shows as a pill.
        if (profile.notes.isNotBlank()) {
            Row(
                modifier = Modifier.padding(start = 18.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F0FE))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) { Text(profile.notes, fontSize = 12.sp, color = Color(0xFF1967D2)) }
            }
        }
        // v2.8.1: a proxy check / launch result is attached to this
        // profile's own card — never at the bottom of the screen.
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GoDivider)
    )
}

@Composable
private fun SettingsTab(
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
    notice: String?
) {
    // v2.8.7: the content sits in the middle of the page, vertically and
    // horizontally (user order) — it used to hug the top, leaving a big
    // empty space below.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onExport,
            colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.FileUpload, null)
            Spacer(Modifier.width(8.dp))
            Text("Export all profiles (.zip)")
        }
        Button(
            onClick = onImport,
            colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.FileDownload, null)
            Spacer(Modifier.width(8.dp))
            Text("Import profiles (.zip)")
        }
        // v2.7.2: trash button removed (user order) — profiles delete
        // permanently from their own menu, there is no trash anymore.
        Button(
            onClick = onAbout,
            colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Info, null)
            Spacer(Modifier.width(8.dp))
            Text("About NovaProfile")
        }
        notice?.let {
            Text(
                it,
                color = GoGreyText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Fingerprint protection is JS-level inside the system WebView. " +
                "It is NOT a Chromium fork — advanced fingerprint tests can " +
                "still detect the real device.",
            color = GoGreyText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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

@Composable
fun EditorScreen(
    initial: Profile?,
    onSave: (Profile) -> Unit,
    onCancel: () -> Unit,
    onCheckProxy: (Profile) -> Unit,
    proxyCheckResult: String?,
    onClearNotice: () -> Unit,
    proxyStore: ProxyStore,
    // v2.8.2: Delete + Check fingerprint moved here from the card's
    // 3-dot menu (menu removed, user order).
    onDelete: (Profile) -> Unit = {},
    onCheckFingerprint: (Profile) -> Unit = {},
    // v2.7.2: new profiles are auto-numbered (Profile 1, 2, 3…) — the
    // name field is pre-filled with the next free number, and the user
    // can rename it right here on the main form (v2.8.3).
    nextProfileName: String = "Profile 1"
) {
    var draft by remember { mutableStateOf(initial ?: Profile.new(nextProfileName)) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    // v2.8.3: sheet views — main form, manual proxy form (type the
    // proxy yourself; the country picker is gone, user order), and the
    // extended-settings section.
    var manualProxyOpen by remember { mutableStateOf(false) }
    var extSection by remember { mutableStateOf<String?>(null) }
    var proxyStatus by remember { mutableStateOf<String?>(null) }

    // v2.8.6: full-screen page instead of a bottom sheet — a downward
    // swipe on the sheet dragged the whole page down and snapped it back
    // ("blinking up down"); on a page the swipe scrolls the form.
    // v2.8.13: NO Dialog — the editor renders in the activity window,
    // so the status bar is the activity's white bar (see the MainScreen
    // note). Two dialog-window fixes failed (v2.8.7 SideEffect read a
    // null parent before attach; v2.8.12 attach listener never fired on
    // the dialog's parent). Back press cancels, like the Dialog's
    // dismissOnBackPress.
    BackHandler { onCancel() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            when {
                manualProxyOpen -> ManualProxyView(
                    proxyStore = proxyStore,
                    scope = scope,
                    current = draft,
                    onApplied = { working ->
                        proxyStore.add(working)
                        // The live check already resolved the proxy IP's
                        // country + timezone + coordinates — the card
                        // flag reads proxyCountry, never the language.
                        val cc = working.country.substringBefore(" •").trim().uppercase()
                        draft = draft.copy(
                            proxyMode = working.mode,
                            proxyHost = working.host,
                            proxyPort = working.port,
                            proxyUser = working.user,
                            proxyPass = working.pass,
                            proxyCountry = cc,
                            // v2.5/v2.6: timezone + coordinates follow
                            // the proxy's IP — but only when the
                            // profile's modes say IP-based.
                            timezone = if (draft.timezoneMode == "IP") working.lastTimezone.ifBlank { draft.timezone } else draft.timezone,
                            latitude = if (draft.geoMode == "IP" && working.lastLat != 0.0) working.lastLat else draft.latitude,
                            longitude = if (draft.geoMode == "IP" && working.lastLon != 0.0) working.lastLon else draft.longitude
                        )
                        proxyStatus =
                            "Connected • ${working.lastIp} • ${working.lastMs} ms — " +
                                "timezone auto-matched. Save the profile to apply."
                        manualProxyOpen = false
                    },
                    onDirect = {
                        draft = draft.copy(
                            proxyMode = "NONE", proxyHost = "", proxyPort = 8080,
                            proxyUser = "", proxyPass = "", proxyCountry = ""
                        )
                        proxyStatus = "Direct — no proxy. Save the profile to apply."
                        manualProxyOpen = false
                    },
                    onBack = { manualProxyOpen = false }
                )
                extSection != null -> ExtendedSectionView(
                    section = extSection!!,
                    draft = draft,
                    onDraft = { draft = it },
                    onBack = { extSection = null },
                    onOpenManualProxy = { extSection = null; manualProxyOpen = true },
                    onCheckProxy = onCheckProxy,
                    onClearNotice = onClearNotice,
                    proxyCheckResult = proxyCheckResult
                )
                else -> {
                    Text(
                        if (initial == null) "Add profile" else "Edit profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    // v2.8.3: the name lives here on the main form —
                    // rename any profile (new or existing) without
                    // digging into Extended settings.
                    OutlinedTextField(
                        value = draft.name,
                        onValueChange = { draft = draft.copy(name = it) },
                        label = { Text("Profile name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    // Proxy row (v2.8.3): tap opens the manual proxy
                    // form — type the proxy yourself, it is tested
                    // before it is applied. No country picker anymore.
                    Text("Proxy", fontWeight = FontWeight.Bold)
                    SectionRow(
                        label = if (draft.proxyMode == "NONE") "Connect proxy"
                        else "${draft.proxyMode} ${draft.proxyHost}:${draft.proxyPort}",
                        sub = if (draft.proxyMode == "NONE") "Tap to type a proxy"
                        else "Timezone ${draft.timezone} • ${draft.language}",
                        onClick = { manualProxyOpen = true }
                    )
                    proxyStatus?.let { Text(it, color = GoGreyText) }
                    // Extended settings (v2.6): GoLogin's 5 sections.
                    Text("Extended settings", fontWeight = FontWeight.Bold)
                    for ((key, label) in listOf(
                        "overview" to "Profile Overview",
                        "proxy" to "Proxy & Location",
                        "browser" to "Browser",
                        "storage" to "Storage",
                        "hardware" to "Hardware"
                    )) {
                        SectionRow(label) { extSection = key }
                    }
                    // v2.8.2: Check fingerprint stays — it lives here on
                    // the profile form (user order: keep it, put it in
                    // the right place, do not remove it).
                    SectionRow(
                        label = "Check fingerprint",
                        sub = "whoer.net — live disguise + proxy check",
                        onClick = { onCheckFingerprint(draft) }
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                when {
                                    draft.name.isBlank() -> error = "Name is required."
                                    draft.proxyMode != "NONE" && draft.proxyHost.isBlank() ->
                                        error = "Proxy host is required for ${draft.proxyMode}."
                                    draft.proxyPort !in 1..65535 ->
                                        error = "Proxy port must be 1–65535."
                                    else -> onSave(draft)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (initial == null) "Add profile" else "Save profile")
                        }
                        TextButton(onClick = onCancel) { Text("Cancel") }
                    }
                    // v2.8.2: Delete lives here now (the card's 3-dot
                    // menu is gone) — it asks once, then deletes
                    // permanently; the editor closes on confirm.
                    if (initial != null) {
                        TextButton(
                            onClick = { onDelete(draft) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Delete profile", color = Color(0xFFD93025)) }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
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

/**
 * Manual proxy form (v2.8.3): the country picker is gone (user order)
 * — the user types the proxy themselves. Protocol is SOCKS5,
 * permanently (no choice to make), IP + Port as separate fields,
 * login, password.
 * The proxy is TESTED through
 * ipify before it is applied to the draft, so a dead proxy can never
 * be saved onto a profile; the live check also resolves the proxy IP's
 * country + timezone for the card flag and the timezone auto-match.
 * v2.8.16: a .txt file (one host:port:user:pass line per proxy) can
 * be imported from the device file manager, and every saved proxy is
 * listed below the form — tap one to fill it in. Typing stays as the
 * manual option.
 */
@Composable
private fun ManualProxyView(
    proxyStore: ProxyStore,
    scope: CoroutineScope,
    current: Profile,
    onApplied: (ProxyEntry) -> Unit,
    onDirect: () -> Unit,
    onBack: () -> Unit
) {
    val savedProxies by proxyStore.proxies.collectAsState()
    val ctx = LocalContext.current
    var importResult by remember { mutableStateOf<String?>(null) }
    // v2.8.16: import a proxy list file from the device file manager —
    // one host:port:user:pass line per proxy (Webshare export). The
    // file is read off the main thread; bad or already-saved lines are
    // counted, never silently dropped.
    val proxyFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val lines = withContext(Dispatchers.IO) {
                    try {
                        ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readLines()
                            ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                val (imported, skipped) = proxyStore.importLines(lines)
                importResult = if (imported == 0 && skipped == 0) {
                    "No proxies found in this file — expected host:port:user:pass lines."
                } else if (skipped > 0) {
                    "$imported proxies imported • $skipped lines skipped (bad or already saved)"
                } else {
                    "$imported proxies imported"
                }
            }
        }
    }
    var newIp by remember {
        mutableStateOf(
            if (current.proxyMode == "NONE" || current.proxyHost.isBlank()) ""
            else current.proxyHost
        )
    }
    // v2.8.7: Port is its own field (numeric keyboard) — the user could
    // not find where the port goes when it hid inside the IP field.
    var newPort by remember {
        mutableStateOf(
            if (current.proxyMode == "NONE" || current.proxyHost.isBlank()) ""
            else current.proxyPort.toString()
        )
    }
    var newUser by remember { mutableStateOf(current.proxyUser) }
    var newPass by remember { mutableStateOf(current.proxyPass) }
    var showPass by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    // v2.8.18: which saved row is being tested right now (its
    // host:port), so the row itself can show "Testing…".
    var testingKey by remember { mutableStateOf<String?>(null) }
    // v2.8.18: manual typing is behind a show/hide row, collapsed
    // by default — import + the saved list are the main way in.
    var manualExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    // Next auto name ("Myproxy #1", "#2"…) when the name is blank.
    val suggestedName = run {
        val max = savedProxies.mapNotNull {
            Regex("""^Myproxy #(\d+)$""").find(it.name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        "Myproxy #${max + 1}"
    }

    // v2.8.18: ONE test path for the Test button AND a saved-row tap —
    // test the entry (SOCKS5 only) and apply it on success. Used to
    // live inline in the Test button only; the row tap filled the form
    // and left the user one more tap away from a working proxy.
    fun testEntry(base: ProxyEntry) {
        testing = true
        testingKey = "${base.host}:${base.port}"
        formError = null
        scope.launch {
            val attempt = proxyStore.check(base.copy(mode = "SOCKS5"))
            testing = false
            testingKey = null
            if (attempt.ok) {
                onApplied(attempt)
            } else {
                formError = "Proxy did not answer — check the IP:port and login, then try again."
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Add proxy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Text(
            "Import a .txt file, or tap a saved proxy below — it is " +
                "tested and applied at once. Or add one manually. " +
                "Protocol is SOCKS5 — permanently.",
            color = GoGreyText
        )
        OutlinedButton(
            onClick = { proxyFileLauncher.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Import proxies from file (.txt)") }
        importResult?.let { Text(it, color = GoGreyText) }
        // v2.8.18: manual typing is behind a show/hide row (collapsed
        // by default) — import + the saved list are the main way in.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { manualExpanded = !manualExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Add proxy manually",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (manualExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (manualExpanded) "Hide manual form" else "Show manual form"
            )
        }
        if (manualExpanded) {
        OutlinedTextField(
            value = newIp, onValueChange = { typed ->
                // v2.8.7: a pasted line splits into the separate fields —
                // "host:port:user:pass" fills all four, "host:port" fills
                // IP + Port. Never overwrites a user/pass already typed.
                val four = Regex("""^([^:]+):(\d+):([^:]+):(.+)$""").matchEntire(typed.trim())
                val two = Regex("""^([^:]+):(\d+)$""").matchEntire(typed.trim())
                when {
                    four != null && newUser.isBlank() && newPass.isBlank() -> {
                        newIp = four.groupValues[1]
                        newPort = four.groupValues[2]
                        newUser = four.groupValues[3]
                        newPass = four.groupValues[4]
                    }
                    two != null -> {
                        newIp = two.groupValues[1]
                        newPort = two.groupValues[2]
                    }
                    else -> newIp = typed
                }
                formError = null
            },
            label = { Text("IP") },
            placeholder = { Text("198.23.243.226") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = newPort, onValueChange = { newPort = it.filter { c -> c.isDigit() }.take(5) },
            label = { Text("Port") },
            placeholder = { Text("6361") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        // v2.8.7: show exactly what will be tested, from the two fields.
        val portNum = newPort.toIntOrNull()
        if (newIp.isNotBlank() || newPort.isNotBlank()) {
            if (newIp.trim().isNotBlank() && portNum != null && portNum in 1..65535) {
                Text("Will test ${newIp.trim()}:$portNum", color = GoGreyText)
            } else {
                Text(
                    "Port is required — e.g. 6361",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        OutlinedTextField(
            value = newUser, onValueChange = { newUser = it },
            label = { Text("Username") },
            placeholder = { Text("Proxy login (leave empty if none)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = newPass, onValueChange = { newPass = it },
            label = { Text("Password") },
            placeholder = { Text("Proxy password") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
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
        formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                val host = newIp.substringBefore(":").trim()
                val port = newPort.toIntOrNull() ?: return@Button
                if (host.isBlank() || port !in 1..65535) return@Button
                // v2.8.8: no name field anymore — the saved entry is just
                // auto-numbered ("Myproxy #N"); the name is never shown
                // anywhere since the Proxies tab is gone.
                testEntry(
                    ProxyEntry(
                        name = suggestedName, mode = "HTTP", host = host,
                        port = port, user = newUser.trim(), pass = newPass
                    )
                )
            },
            enabled = !testing && newIp.trim().isNotBlank() &&
                (newPort.toIntOrNull() ?: 0) in 1..65535,
            colors = ButtonDefaults.buttonColors(containerColor = GoGreen),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (testing) "Testing…" else "Test & apply proxy") }
        }
        TextButton(
            onClick = onDirect,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Use no proxy (Direct)") }
        // v2.8.16: every saved proxy, with its live-check detail.
        // v2.8.18: tap one to test & apply it AT ONCE (it fills the
        // form too) — bin to delete it. Manual typing stays behind
        // its show/hide row above; this list is the shortcut.
        if (savedProxies.isNotEmpty()) {
            Text(
                "Saved proxies (${savedProxies.size}) — tap one to test & apply it at once",
                fontWeight = FontWeight.Bold, fontSize = 16.sp
            )
            savedProxies.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            newIp = entry.host
                            newPort = entry.port.toString()
                            newUser = entry.user
                            newPass = entry.pass
                            formError = null
                            // v2.8.18: tap tests AND applies at once —
                            // filling the form alone left the user one
                            // more tap away from a working proxy.
                            testEntry(entry)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${entry.mode} ${entry.host}:${entry.port}",
                            fontWeight = FontWeight.Bold
                        )
                        val countryCode = entry.country.substringBefore(" •").trim()
                        val countryLabel = if (countryCode.length == 2) {
                            Profile.countrySettings(countryCode).label
                        } else {
                            entry.country
                        }
                        Text(
                            if (countryLabel.isBlank()) "Not tested yet — tap to test & apply"
                            else if (countryCode.length == 2) "${Profile.flagEmoji(countryCode)} $countryLabel"
                            else countryLabel,
                            color = GoGreyText
                        )
                        if (testingKey == "${entry.host}:${entry.port}") {
                            Text("Testing…", color = GoGreyText)
                        }
                        if (entry.ok && entry.lastIp.isNotBlank()) {
                            Text("IP ${entry.lastIp} • ${entry.lastMs} ms", color = GoGreyText)
                        }
                    }
                    IconButton(onClick = { proxyStore.remove(entry.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete proxy")
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}


/** Extended settings (v2.6): GoLogin's 5 sub-screens. */
@Composable
private fun ExtendedSectionView(
    section: String,
    draft: Profile,
    onDraft: (Profile) -> Unit,
    onBack: () -> Unit,
    onOpenManualProxy: () -> Unit,
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
                draft, onDraft, onOpenManualProxy,
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
    // v2.8.3: the Name field moved to the editor's main form — it is
    // not repeated here anymore.
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
    onOpenManualProxy: () -> Unit,
    onCheckProxy: (Profile) -> Unit,
    onClearNotice: () -> Unit,
    proxyCheckResult: String?
) {
    Text("Proxy", fontWeight = FontWeight.Bold)
    // v2.8.1: the check result lives INSIDE this proxy card — never at
    // the bottom of the sheet.
    var checkedHere by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GoDivider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionRow(
            label = if (draft.proxyMode == "NONE") "Connect proxy"
            else "${draft.proxyMode} ${draft.proxyHost}:${draft.proxyPort}",
            sub = if (draft.proxyMode == "NONE") "Tap to type a proxy"
            else "Timezone ${draft.timezone}",
            onClick = onOpenManualProxy
        )
        Button(onClick = { checkedHere = true; onClearNotice(); onCheckProxy(draft) }) {
            Text("Check proxy")
        }
        if (checkedHere) {
            proxyCheckResult?.let { Text(it, color = GoGreyText) }
        }
    }
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
