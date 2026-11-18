package rs.appsearch.simple.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Existing App Search 1.1 app discovery/launch behavior, isolated from the file tab. */
class AppsScreen(context: Context) : LinearLayout(context) {
    private val search = EditText(context)
    private val clear = TextView(context)
    private val results = LinearLayout(context)
    private val scroll = ScrollView(context)
    private val apps: List<ResolveInfo> = context.packageManager
        .queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        .sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }

    init {
        orientation = VERTICAL

        val searchRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        search.apply {
            hint = "Unesi naziv aplikacije..."
            isSingleLine = true
            textSize = 16f
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(themeColor(android.R.attr.colorBackgroundFloating, 0xFFFFFFFF.toInt()))
        }
        searchRow.addView(search, LayoutParams(0, dp(54), 1f))

        clear.apply {
            text = "×"
            textSize = 26f
            gravity = Gravity.CENTER
            contentDescription = "Obriši pretragu"
            visibility = GONE
            isClickable = true
            isFocusable = true
            setOnClickListener { search.text.clear() }
        }
        searchRow.addView(clear, LayoutParams(dp(48), dp(54)))
        addView(searchRow)

        results.apply {
            orientation = VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }
        scroll.addView(results)
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clear.visibility = if (s.isNullOrEmpty()) GONE else VISIBLE
                showResults(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        showResults("")
    }

    fun dismissKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(search.windowToken, 0)
        search.clearFocus()
    }

    private fun showResults(query: String) {
        val previousScroll = scroll.scrollY
        results.removeAllViews()
        val matches = apps.filter {
            it.loadLabel(context.packageManager).toString().contains(query.trim(), ignoreCase = true)
        }
        if (matches.isEmpty()) {
            results.addView(TextView(context).apply {
                text = if (query.isBlank()) "Nema dostupnih aplikacija." else "Nije pronađena nijedna aplikacija."
                textSize = 15f
                setTextColor(themeColor(android.R.attr.textColorSecondary, 0xFF777777.toInt()))
                gravity = Gravity.CENTER
                setPadding(0, dp(30), 0, dp(30))
            })
            return
        }
        matches.forEach { info ->
            val row = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setBackgroundColor(themeColor(android.R.attr.colorBackgroundFloating, 0xFFFFFFFF.toInt()))
                isClickable = true
                isFocusable = true
                setOnClickListener { launchApp(info) }
            }
            row.addView(TextView(context).apply {
                text = "●"
                textSize = 22f
                setTextColor(0xFF4267B2.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 0, dp(12), 0)
            }, LayoutParams(dp(34), dp(42)))
            row.addView(TextView(context).apply {
                text = info.loadLabel(context.packageManager)
                textSize = 16f
                setTextColor(themeColor(android.R.attr.textColorPrimary, 0xFF202124.toInt()))
            }, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            results.addView(row, LayoutParams(LayoutParams.MATCH_PARENT, dp(58)).apply {
                bottomMargin = dp(6)
            })
        }
        scroll.post { scroll.scrollTo(0, previousScroll.coerceAtMost(scroll.getChildAt(0).height)) }
    }

    private fun launchApp(info: ResolveInfo) {
        val intent = context.packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Aplikacija ne može da se pokrene.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun themeColor(attribute: Int, fallback: Int): Int {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(attribute, value, true)) {
            if (value.resourceId != 0) {
                @Suppress("DEPRECATION")
                context.resources.getColor(value.resourceId)
            } else value.data
        } else fallback
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
