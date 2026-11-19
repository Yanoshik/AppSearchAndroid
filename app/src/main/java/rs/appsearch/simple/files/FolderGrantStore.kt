package rs.appsearch.simple.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import org.json.JSONArray
import org.json.JSONObject

/** Stores only the user's chosen tree URI and display name; never file contents. */
class FolderGrantStore(private val context: Context) {
    data class Folder(val uri: Uri, val name: String, val accessible: Boolean)

    private val prefs = context.getSharedPreferences("appsearch_access_folders", Context.MODE_PRIVATE)
    private val key = "selected_trees"

    private fun saved(): JSONArray = try {
        JSONArray(prefs.getString(key, "[]") ?: "[]")
    } catch (_: Exception) {
        JSONArray()
    }

    private fun hasReadGrant(uri: Uri): Boolean =
        context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }

    private fun readable(uri: Uri): Boolean {
        if (!hasReadGrant(uri)) return false
        return try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                uri, DocumentsContract.getTreeDocumentId(uri)
            )
            context.contentResolver.query(
                docUri, arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE),
                null, null, null
            )?.use { cursor -> cursor.moveToFirst() } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun list(): List<Folder> {
        val stored = saved()
        val result = mutableListOf<Folder>()
        for (i in 0 until stored.length()) {
            val item = stored.optJSONObject(i) ?: continue
            val value = item.optString("uri")
            if (value.isBlank()) continue
            val uri = Uri.parse(value)
            result.add(Folder(uri, item.optString("name", "Folder"), readable(uri)))
        }
        return result
    }

    fun add(uri: Uri, name: String): Boolean {
        val data = saved()
        for (i in 0 until data.length()) {
            if (data.optJSONObject(i)?.optString("uri") == uri.toString()) return false
        }
        data.put(JSONObject().put("uri", uri.toString()).put("name", name))
        return prefs.edit().putString(key, data.toString()).commit()
    }

    fun remove(uri: Uri): Boolean {
        val data = saved()
        val remaining = JSONArray()
        var found = false
        for (i in 0 until data.length()) {
            val item = data.optJSONObject(i) ?: continue
            if (item.optString("uri") == uri.toString()) {
                found = true
            } else {
                remaining.put(item)
            }
        }
        if (!found) return false
        if (!prefs.edit().putString(key, remaining.toString()).commit()) return false
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // An already revoked grant is still removed from local records.
        }
        return true
    }

    fun displayName(uri: Uri): String {
        return try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                uri, DocumentsContract.getTreeDocumentId(uri)
            )
            context.contentResolver.query(
                docUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null, null, null
            )?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }?.takeIf { it.isNotBlank() } ?: "Izabrani folder"
        } catch (_: Exception) {
            "Izabrani folder"
        }
    }
}
