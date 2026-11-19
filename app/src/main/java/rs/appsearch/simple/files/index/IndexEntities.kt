package rs.appsearch.simple.files.index

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Metadata only. The file content is never copied into this database.
 * itemKey is a source-specific stable identity (not the display name).
 */
@Entity(
    tableName = "indexed_files",
    indices = [
        Index(value = ["sourceId", "itemKey"], unique = true),
        Index(value = ["sourceId"]),
        Index(value = ["lastSeenScanId"]),
        Index(value = ["displayName"])
    ]
)
data class IndexedFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceId: String,
    val itemKey: String,
    val providerAuthority: String?,
    val documentUri: String,
    val documentId: String?,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val modifiedAt: Long?,
    val relativeLocation: String?,
    val fileCategory: String,
    val availability: String,
    val lastSeenScanId: String?,
    val indexedAt: Long
)

@Entity(tableName = "index_sources", indices = [Index(value = ["sourceType"])])
data class IndexSourceEntity(
    @PrimaryKey val sourceId: String,
    val sourceType: String,
    val rootUri: String?,
    val displayName: String,
    val accessState: String,
    val lastSuccessfulScan: Long?,
    val lastError: String?,
    val mediaGeneration: Long?
)

@Entity(tableName = "scan_sessions", indices = [Index(value = ["status"])])
data class ScanSessionEntity(
    @PrimaryKey val scanId: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val status: String,
    val processedFiles: Long,
    val indexedFiles: Long,
    val failedItems: Long,
    val scanScope: String
)

@Entity(
    tableName = "staged_files",
    primaryKeys = ["scanId", "sourceId", "itemKey"],
    indices = [Index(value = ["scanId"]), Index(value = ["sourceId"])]
)
data class StagedFileEntity(
    val scanId: String,
    val sourceId: String,
    val itemKey: String,
    val providerAuthority: String?,
    val documentUri: String,
    val documentId: String?,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val modifiedAt: Long?,
    val relativeLocation: String?,
    val fileCategory: String,
    val indexedAt: Long
)
