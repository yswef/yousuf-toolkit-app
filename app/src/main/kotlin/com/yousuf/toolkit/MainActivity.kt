package com.yousuf.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import core.design.AppTheme
import core.design.UiState
import core.navigation.AppNav
import data.local.LocalStore
import data.local.Store

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val store = remember { Store.of(applicationContext) }
            val ui = remember { UiState(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            SideEffect { ui.systemDark = systemDark }
            CompositionLocalProvider(LocalStore provides store) {
                AppTheme(mode = ui.mode, dark = ui.dark, accent = store.accent) {
                    AppNav(ui = ui)
                }
            }
        }
    }
}
