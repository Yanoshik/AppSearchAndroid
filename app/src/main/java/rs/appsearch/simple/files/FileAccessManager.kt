package rs.appsearch.simple.files

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore

/**
 * Checks the actual runtime grants, never infers full storage access.
 * Probe queries read at most the first cursor row and do not index any files.
 */
class FileAccessManager(private val activity: Activity) {
    data class MediaState(
        val images: String,
        val video: String,
        val audio: String
    )

    private fun granted(permission: String) =
        activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun requestedPermissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        Build.VERSION.SDK_INT >= 33 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun probe(uri: android.net.Uri): Boolean {
        return try {
            activity.contentResolver.query(
                uri, arrayOf(MediaStore.MediaColumns._ID),
                null, null, null
            )?.use { true } ?: false
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun state(): MediaState {
        if (Build.VERSION.SDK_INT < 33) {
            val access = granted(Manifest.permission.READ_EXTERNAL_STORAGE)
            return if (access) {
                MediaState(
                    if (probe(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)) "Odobren" else "Nedostupno",
                    if (probe(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)) "Odobren" else "Nedostupno",
                    if (probe(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) "Odobren" else "Nedostupno"
                )
            } else MediaState("Nije odobren", "Nije odobren", "Nije odobren")
        }

        val partial = Build.VERSION.SDK_INT >= 34 &&
            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        fun visual(permission: String, uri: android.net.Uri): String {
            val full = granted(permission)
            if (!full && !partial) return "Nije odobren"
            if (!probe(uri)) return "Nedostupno"
            return if (full) "Odobren" else "Ograničen izbor"
        }
        return MediaState(
            visual(Manifest.permission.READ_MEDIA_IMAGES, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            visual(Manifest.permission.READ_MEDIA_VIDEO, MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
            if (!granted(Manifest.permission.READ_MEDIA_AUDIO)) "Nije odobren"
            else if (probe(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) "Odobren"
            else "Nedostupno"
        )
    }
}
