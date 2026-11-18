package rs.appsearch.simple.files

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Phase 1 placeholder only. No storage permissions, scan, or file access. */
class FilesScreen(context: Context) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(16), dp(30), dp(16), dp(16))

        addView(TextView(context).apply {
            text = "Pronađi fajlove"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })

        addView(TextView(context).apply {
            text = "App Search će omogućiti pretragu dostupnih fotografija, dokumenata, muzike, video-snimaka i drugih fajlova."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(18))
        })

        addView(TextView(context).apply {
            text = "Fajlovi ostaju na telefonu. Pretraga će raditi offline. Pristup folderima odobravaš sam."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(20))
        })

        addView(Button(context).apply {
            text = "Pokreni skeniranje — u pripremi"
            isEnabled = false
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        addView(Button(context).apply {
            text = "Dodaj folder — u pripremi"
            isEnabled = false
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        addView(TextView(context).apply {
            text = "Faza 1: skeniranje i pristup fajlovima još nisu dostupni."
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
