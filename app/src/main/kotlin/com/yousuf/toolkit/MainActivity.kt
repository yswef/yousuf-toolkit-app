// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package com.yousuf.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import core.design.AppTheme
import core.design.SoundFx
import core.design.UiState
import core.navigation.AppNav
import core.update.ContentUpdate
import data.local.LocalStore
import data.local.Store
import feature.journal.JournalSync

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SoundFx.init(applicationContext)
        feature.watch.WatchScheduler.ensure(applicationContext)
        setContent {
            val store = remember { Store.of(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            val ui = remember { UiState(applicationContext, systemDark) }
            SideEffect { ui.systemDark = systemDark }
            CompositionLocalProvider(LocalStore provides store) {
                AppTheme(mode = ui.mode, dark = ui.dark, accent = store.accent, fontScale = ui.fontScale, motion = ui.effectiveMotion) {
                    LaunchedEffect(Unit) {
                        ContentUpdate.sync(applicationContext, store.serverUrl)
                        if (store.journalAutoSync && store.premium) runCatching { JournalSync.push(store) }
                    }
                    AppNav(ui = ui)
                }
            }
        }
    }
}

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
