package rs.appsearch.simple.files

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Phase 2: access setup only. Does not scan directories, enumerate files or create an index.
 */
class FilesScreen(context: Context) : ScrollView(context) {
    var onRequestMediaAccess: (() -> Unit)? = null
    var onAddFolder: (() -> Unit)? = null

    private val grantStore = FolderGrantStore(context)
    private val mediaStatus = TextView(context)
    private val foldersContainer = LinearLayout(context)
    private val notice = TextView(context)

    init {
        isFillViewport = true
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(22), dp(16), dp(18))
        }
        addView(content)

        content.addView(TextView(context).apply {
            text = "Pronađi fajlove"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })

        content.addView(TextView(context).apply {
            text = "App Search će omogućiti pretragu dostupnih fotografija, dokumenata, muzike, video-snimaka i drugih fajlova."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(18))
        })

        content.addView(TextView(context).apply {
            text = "Fajlovi ostaju na telefonu. Pretraga će raditi offline. Pristup folderima odobravaš sam."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(14))
        })

        content.addView(Button(context).apply {
            text = "Pokreni skeniranje — provera pristupa"
            contentDescription = "Pripremi dozvole za buduće skeniranje"
            setOnClickListener { onRequestMediaAccess?.invoke() }
        }, LinearLayout.LayoutParams(-1, -2))

        content.addView(Button(context).apply {
            text = "Dodaj folder"
            setOnClickListener { onAddFolder?.invoke() }
        }, LinearLayout.LayoutParams(-1, -2))

        notice.apply {
            textSize = 13f
            gravity = Gravity.CENTER
            text = "Faza 2: proveravamo samo pristup. Prava pretraga i indeksiranje dolaze kasnije."
            setPadding(0, dp(12), 0, dp(18))
        }
        content.addView(notice)

        content.addView(TextView(context).apply {
            text = "Status pristupa medijima"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
        })
        mediaStatus.apply {
            textSize = 14f
            setPadding(0, dp(8), 0, dp(20))
        }
        content.addView(mediaStatus)

        content.addView(TextView(context).apply {
            text = "Odobreni folderi"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
        })
        foldersContainer.orientation = LinearLayout.VERTICAL
        content.addView(foldersContainer)
    }

    fun showMessage(message: String) {
        notice.text = message
    }

    fun refresh(media: FileAccessManager.MediaState) {
        mediaStatus.text = "Fotografije: ${media.images}\nVideo: ${media.video}\nAudio: ${media.audio}"
        foldersContainer.removeAllViews()
        val folders = grantStore.list()
        if (folders.isEmpty()) {
            foldersContainer.addView(TextView(context).apply {
                text = "Nema dodatih foldera. Možeš izabrati dostupnu lokaciju pomoću dugmeta Dodaj folder."
                textSize = 14f
                setPadding(0, dp(10), 0, dp(8))
            })
            return
        }
        folders.forEach { folder ->
            val item = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(10), 0, dp(12))
            }
            item.addView(TextView(context).apply {
                text = folder.name
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            })
            item.addView(TextView(context).apply {
                text = if (folder.accessible) "Pristup dostupan" else "Lokacija trenutno nije dostupna ili je pristup opozvan"
                textSize = 13f
            })
            item.addView(Button(context).apply {
                text = "Ukloni folder"
                contentDescription = "Ukloni ${folder.name} iz App Search liste"
                setOnClickListener { confirmRemoval(folder.uri, folder.name) }
            })
            foldersContainer.addView(item)
        }
    }

    private fun confirmRemoval(uri: Uri, folderName: String) {
        AlertDialog.Builder(context)
            .setTitle("Ukloniti folder iz App Search liste?")
            .setMessage("Folder: $folderName\n\nOvim uklanjaš pristupnu lokaciju iz aplikacije. Fajlovi na telefonu neće biti obrisani.")
            .setNegativeButton("Otkaži", null)
            .setPositiveButton("Ukloni folder") { _, _ ->
                val removed = grantStore.remove(uri)
                showMessage(if (removed) "Folder je uklonjen iz liste. Fajlovi nisu obrisani."
                    else "Folder nije uklonjen. Pokušaj ponovo.")
                // The hosting activity will also refresh on resume.
                foldersContainer.removeAllViews()
                val current = grantStore.list()
                if (current.isEmpty()) {
                    foldersContainer.addView(TextView(context).apply {
                        text = "Nema dodatih foldera."
                        textSize = 14f
                    })
                } else {
                    // Rebuild using the current media labels retained on screen.
                    val lines = mediaStatus.text.toString().lines()
                    refresh(FileAccessManager.MediaState(
                        lines.getOrNull(0)?.substringAfter(": ") ?: "Nepoznato",
                        lines.getOrNull(1)?.substringAfter(": ") ?: "Nepoznato",
                        lines.getOrNull(2)?.substringAfter(": ") ?: "Nepoznato"
                    ))
                }
            }
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
