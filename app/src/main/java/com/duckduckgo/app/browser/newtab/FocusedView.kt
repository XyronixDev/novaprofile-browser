/*
 * Copyright (c) 2024 DuckDuckGo
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

package com.duckduckgo.app.browser.newtab

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.duckduckgo.app.browser.databinding.ViewFocusedViewBinding
import com.duckduckgo.browsermode.api.BrowserMode
import com.duckduckgo.codegen.annotations.InjectWith
import com.duckduckgo.common.ui.view.show
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ViewScope

@InjectWith(ViewScope::class)
class FocusedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    private val binding: ViewFocusedViewBinding by viewBinding()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnClickListener { }
    }

    fun setOnScrollChangeListener(listener: () -> Unit) {
        binding.focusedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            listener()
        }
    }

    fun show(isLogoVisible: Boolean, browserMode: BrowserMode) {
        val isFireMode = browserMode == BrowserMode.FIRE
        binding.focusedFireEmptyState.root.isVisible = isFireMode
        // NovaProfile v2.8.23: the DuckDuckGo logo never shows on the
        // focused (omnibox) view. The user saw the duck here in
        // v2.8.22 when the URL bar was focused — the v2.8.14 fix only
        // hid the new-tab Lottie, not this one (BrowserTabFragment
        // passes isLogoVisible = !hasFavorites, so a fresh tab with no
        // favorites always showed it).
        binding.focusedLogo.isVisible = false
        binding.focusedFavourites.isVisible = !isLogoVisible && !isFireMode
        this.show()
    }
}
