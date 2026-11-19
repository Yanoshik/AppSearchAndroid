package rs.appsearch.simple

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import rs.appsearch.simple.files.FileAccessManager
import rs.appsearch.simple.files.FolderGrantStore
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
    private val mediaRequestCode = 2201
    private val folderRequestCode = 2202
    private val accessManager by lazy { FileAccessManager(this) }
    private val folderStore by lazy { FolderGrantStore(this) }

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
        filesScreen = FilesScreen(this).apply {
            visibility = View.GONE
            onRequestMediaAccess = { explainAndRequestMediaAccess() }
            onAddFolder = { openFolderPicker() }
        }
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
        filesScreen.refresh(accessManager.state())
    }

    private fun showTab(tab: String) {
        if (tab == activeTab) return
        appsScreen.dismissKeyboard()
        activeTab = tab
        appsScreen.visibility = if (tab == "apps") View.VISIBLE else View.GONE
        filesScreen.visibility = if (tab == "files") View.VISIBLE else View.GONE
        if (tab == "files") filesScreen.refresh(accessManager.state())
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

    private fun explainAndRequestMediaAccess() {
        AlertDialog.Builder(this)
            .setTitle("Pristup medijima")
            .setMessage("Aplikacija traži dozvole za čitanje dostupnih fotografija, videa i audija. Možeš odobriti, ograničiti ili odbiti pristup. U ovoj fazi se ne skeniraju fajlovi.")
            .setNegativeButton("Otkaži", null)
            .setPositiveButton("Nastavi") { _, _ ->
                requestPermissions(accessManager.requestedPermissions(), mediaRequestCode)
            }.show()
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        try {
            startActivityForResult(intent, folderRequestCode)
        } catch (_: Exception) {
            filesScreen.showMessage("Sistemski izbor foldera nije dostupan.")
        }
    }

    @Deprecated("Legacy Activity result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != folderRequestCode || resultCode != RESULT_OK) return
        val uri = data?.data
        if (uri == null) {
            filesScreen.showMessage("Folder nije izabran.")
            return
        }
        if ((data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            filesScreen.showMessage("Nije odobren pristup za čitanje foldera.")
            return
        }
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val name = folderStore.displayName(uri)
            val added = folderStore.add(uri, name)
            filesScreen.showMessage(
                if (added) "Folder je dodat. Pristup je sačuvan."
                else if (folderStore.list().any { it.uri == uri }) "Folder je već na listi."
                else "Nije moguće sačuvati folder."
            )
        } catch (_: SecurityException) {
            filesScreen.showMessage("Android nije dozvolio trajni pristup ovom folderu.")
        } catch (_: Exception) {
            filesScreen.showMessage("Nije moguće dodati izabrani folder.")
        }
        filesScreen.refresh(accessManager.state())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == mediaRequestCode) {
            filesScreen.refresh(accessManager.state())
            filesScreen.showMessage("Status dozvola je osvežen. Indeksiranje dolazi u Fazi 3.")
        }
    }

    override fun onResume() {
        super.onResume()
        if (::filesScreen.isInitialized) filesScreen.refresh(accessManager.state())
    }

    @Deprecated("Use Android's back dispatch; retained for minSdk 23 navigation.")
    override fun onBackPressed() {
        if (activeTab == "files") showTab("apps") else super.onBackPressed()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
