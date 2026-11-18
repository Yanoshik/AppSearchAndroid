package rs.appsearch.simple

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var search: EditText
    private lateinit var results: LinearLayout
    private var apps: List<ResolveInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(12))
            setBackgroundColor(0xFFF7F7F7.toInt())
        }

        val title = TextView(this).apply {
            text = "App Search"
            textSize = 28f
            setTextColor(0xFF202124.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        page.addView(title)

        val subtitle = TextView(this).apply {
            text = "Pronađi i pokreni instaliranu aplikaciju"
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            setPadding(0, dp(4), 0, dp(18))
        }
        page.addView(subtitle)

        search = EditText(this).apply {
            hint = "Unesi naziv aplikacije..."
            singleLine = true
            textSize = 16f
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        page.addView(search, LinearLayout.LayoutParams(-1, dp(54)))

        val scroll = ScrollView(this)
        results = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }
        scroll.addView(results)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(page)

        loadApps()
        showResults("")

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showResults(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
    }

    private fun showResults(query: String) {
        results.removeAllViews()
        val matches = apps.filter {
            it.loadLabel(packageManager).toString().contains(query.trim(), ignoreCase = true)
        }
        if (matches.isEmpty()) {
            val empty = TextView(this).apply {
                text = if (query.isBlank()) "Nema dostupnih aplikacija." else "Nije pronađena nijedna aplikacija."
                textSize = 15f
                setTextColor(0xFF777777.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dp(30), 0, dp(30))
            }
            results.addView(empty)
            return
        }

        matches.forEach { info ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setBackgroundColor(0xFFFFFFFF.toInt())
                isClickable = true
                isFocusable = true
                setOnClickListener { launchApp(info) }
            }
            val icon = TextView(this).apply {
                text = "●"
                textSize = 22f
                setTextColor(0xFF4267B2.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 0, dp(12), 0)
            }
            val name = TextView(this).apply {
                text = info.loadLabel(packageManager)
                textSize = 16f
                setTextColor(0xFF202124.toInt())
            }
            row.addView(icon, LinearLayout.LayoutParams(dp(34), dp(42)))
            row.addView(name, LinearLayout.LayoutParams(0, -2, 1f))
            results.addView(row, LinearLayout.LayoutParams(-1, dp(58)).apply {
                bottomMargin = dp(6)
            })
        }
    }

    private fun launchApp(info: ResolveInfo) {
        val intent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Aplikacija ne može da se pokrene.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
