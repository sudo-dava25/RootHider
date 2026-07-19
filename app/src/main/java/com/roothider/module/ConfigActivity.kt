package com.roothider.module

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Launcher-visible activity. Its main job is to tell the user this APK
 * only takes effect once enabled + scoped inside LSPosed Manager — the
 * real hook logic in HookEntry never runs unless LSPosed loads it into
 * a target app's process.
 */
class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply {
            text = "RootHider is a LSPosed module.\n\n" +
                "Open LSPosed Manager, enable this module, " +
                "and select which apps it should apply to."
            textSize = 16f
            setPadding(48, 96, 48, 48)
        }
        setContentView(text)
    }
}
