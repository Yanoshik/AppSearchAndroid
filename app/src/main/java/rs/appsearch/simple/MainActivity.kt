package rs.appsearch.simple

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import rs.appsearch.simple.apps.AppsScreen
import rs.appsearch.simple.files.FilesScreen

/** Phase 1 UI shell; deliberately does not request storage permissions or read files. */
class MainActivity : Activity() {
    private lateinit var appsScreen: AppsScreen
    private lateinit var filesScreen: FilesScreen
    private lateinit var appsTab: TextView
    private lateinit var filesTab: TextView
    private var activeTab = "apps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val primaryText = if (dark) 0xFFF4F4F4.toInt() else 0xFF202124.toInt()
        val background = if (dark) 0xFF18191D.toInt() else 0xFFF7F7F7.toInt()
        val unselected = if (dark) 0xFFB7B7B7.toInt() else 0xFF666666.toInt()
        val gold = if (dark) 0xFFFFD477.toInt() else 0xFF9B701D.toInt()

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(12))
            setBackgroundColor(background)
            isFocusableInTouchMode = true
            requestFocus()
        }

        page.addView(TextView(this).apply {
            text = "App Search"
            textSize = 28f
            setTextColor(primaryText)
            typeface = Typeface.DEFAULT_BOLD
        })

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(14), 0, dp(12))
        }
        appsTab = TextView(this).apply {
            text = "Aplikacije"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { showTab("apps") }
        }
        filesTab = TextView(this).apply {
            text = "Fajlovi"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { showTab("files") }
        }
        tabs.addView(appsTab, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        tabs.addView(filesTab, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        page.addView(tabs)

        appsScreen = AppsScreen(this)
        filesScreen = FilesScreen(this).apply { visibility = View.GONE }
        page.addView(appsScreen, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(filesScreen, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(page)

        // Tab views remain attached: their search/scroll state survives tab switches.
        fun style(tab: TextView, selected: Boolean) {
            tab.setTextColor(if (selected) gold else unselected)
            tab.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            tab.setBackgroundColor(if (selected) {
                if (dark) 0xFF303137.toInt() else 0xFFFFF0C9.toInt()
            } else background)
        }
        style(appsTab, true)
        style(filesTab, false)
    }

    private fun showTab(tab: String) {
        if (tab == activeTab) return
        appsScreen.dismissKeyboard()
        activeTab = tab
        appsScreen.visibility = if (tab == "apps") View.VISIBLE else View.GONE
        filesScreen.visibility = if (tab == "files") View.VISIBLE else View.GONE
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val selected = if (dark) 0xFFFFD477.toInt() else 0xFF9B701D.toInt()
        val unselected = if (dark) 0xFFB7B7B7.toInt() else 0xFF666666.toInt()
        val background = if (dark) 0xFF18191D.toInt() else 0xFFF7F7F7.toInt()
        val highlight = if (dark) 0xFF303137.toInt() else 0xFFFFF0C9.toInt()
        listOf(appsTab to (tab == "apps"), filesTab to (tab == "files")).forEach { (view, isSelected) ->
            view.setTextColor(if (isSelected) selected else unselected)
            view.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            view.setBackgroundColor(if (isSelected) highlight else background)
            view.isSelected = isSelected
        }
    }

    @Deprecated("Use Android's back dispatch; retained for minSdk 23 navigation.")
    override fun onBackPressed() {
        if (activeTab == "files") showTab("apps") else super.onBackPressed()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
