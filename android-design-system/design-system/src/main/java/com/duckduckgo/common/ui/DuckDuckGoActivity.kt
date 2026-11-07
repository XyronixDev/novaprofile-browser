/*
 * Copyright (c) 2023 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.common.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.duckduckgo.common.ui.store.AppBrandDesignUpdateToggles
import com.duckduckgo.common.ui.store.ThemingDataStore
import com.duckduckgo.common.ui.view.isFullScreen
import com.duckduckgo.di.android.AndroidInjection
import com.duckduckgo.di.android.InjectableActivity
import com.duckduckgo.mobile.android.R
import dev.zacsweers.metro.HasMemberInjections
import javax.inject.Inject

@HasMemberInjections
abstract class DuckDuckGoActivity : InjectableActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject lateinit var themingDataStore: ThemingDataStore

    @Inject lateinit var appBrandDesignUpdateToggles: AppBrandDesignUpdateToggles

    /**
     * Override in subclasses whose look should follow the fire-mode theme.
     * Default `false` keeps activities mode-agnostic on the regular accent regardless
     * of the user's browser mode.
     *
     * Overriding activities must inject [BrowserMode] and return the appropriate Boolean value.
     */
    protected open val applyFireTheme: Boolean = false

    private var themeChangeReceiver: BroadcastReceiver? = null

    private var displayCutoutModeManaged = false

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        onCreate(savedInstanceState, true)
    }

    /**
     * We need to conditionally defer the injection in certain places. So if this method
     * is called from an Activity with injectDependencies=false, you'll probably need to call
     * injectDependencies() directly.
     */
    fun onCreate(
        savedInstanceState: Bundle?,
        injectDependencies: Boolean = true,
    ) {
        if (injectDependencies) injectDependencies()
        // NovaProfile v2.8.1: light mode only — dark mode is removed
        // entirely, so the stored theme is ignored and LIGHT is forced
        // (including fire mode, which has its own light variant).
        themeChangeReceiver = applyTheme(
            DuckDuckGoTheme.LIGHT,
            applyFireTheme,
            appBrandDesignUpdateToggles.theme().isEnabled(),
            listOfNotNull(
                R.style.ThemeOverlay_Rebrand_Radius.takeIf { appBrandDesignUpdateToggles.radius().isEnabled() },
                R.style.ThemeOverlay_Rebrand_Badges.takeIf { appBrandDesignUpdateToggles.badges().isEnabled() },
            ),
        )
        super.onCreate(savedInstanceState)
    }

    protected fun injectDependencies() {
        AndroidInjection.inject(this, bindingKey = InjectableActivity::class.java)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        themeChangeReceiver?.let {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(it)
        }
        super.onDestroy()
    }

    fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_24)
    }

    fun isDarkThemeEnabled(): Boolean {
        // NovaProfile v2.8.1: dark mode is removed — always light, so
        // edge-to-edge bars always use the light (dark-on-white) style.
        return false
    }

    /**
     * Enables edge-to-edge with fully transparent system bars. Call from [onCreate] when
     * edge-to-edge is enabled for the screen.
     */
    protected fun enableTransparentEdgeToEdge() {
        val barStyle = if (isDarkThemeEnabled()) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        applyDisplayCutoutMode(resources.configuration.orientation)
    }

    /**
     * Sets the window's display-cutout mode from [orientation]: in landscape the window avoids the
     * cutout (the notch/camera area is reserved as a black letterbox), in portrait it keeps the
     * default behaviour and draws normally around the (typically top-centre) cutout.
     *
     * Calling this marks the cutout mode as managed, so it is re-applied on rotation via
     * [onConfigurationChanged] for activities that declare `android:configChanges` and are not
     * recreated. Non-edge-to-edge screens never call this, so their cutout stays at the platform default.
     */
    protected fun applyDisplayCutoutMode(orientation: Int) {
        displayCutoutModeManaged = true
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Activities declaring android:configChanges (e.g. orientation) are not recreated on rotation,
        // so re-apply the cutout mode here. Only screens that applied it once (edge-to-edge) are managed;
        // others keep the platform default. Subclasses overriding this must call super.
        if (displayCutoutModeManaged) {
            applyDisplayCutoutMode(newConfig.orientation)
        }
    }

    protected inline fun <reified V : ViewModel> bindViewModel() = lazy {
        ViewModelProvider(this, viewModelFactory).get(V::class.java)
    }

    open fun toggleFullScreen() {
        if (isFullScreen()) {
            // If we are exiting full screen, reset the orientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        val newUiOptions = window.decorView.systemUiVisibility
            .xor(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            .xor(View.SYSTEM_UI_FLAG_FULLSCREEN)
            .xor(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        window.decorView.systemUiVisibility = newUiOptions
    }
}
