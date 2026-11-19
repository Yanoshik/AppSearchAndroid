package rs.appsearch.simple.files.index

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FileIndexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSource(source: IndexSourceEntity)

    @Query("SELECT * FROM index_sources ORDER BY displayName")
    suspend fun sources(): List<IndexSourceEntity>

    @Query("SELECT * FROM index_sources WHERE sourceId = :sourceId LIMIT 1")
    suspend fun source(sourceId: String): IndexSourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSession(session: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions WHERE scanId = :scanId LIMIT 1")
    suspend fun session(scanId: String): ScanSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putStaged(items: List<StagedFileEntity>)

    @Query("SELECT COUNT(*) FROM staged_files WHERE scanId = :scanId")
    suspend fun stagedCount(scanId: String): Long

    @Query("DELETE FROM staged_files WHERE scanId = :scanId")
    suspend fun discardStaged(scanId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putIndexed(items: List<IndexedFileEntity>)

    @Query("SELECT * FROM indexed_files WHERE sourceId = :sourceId AND itemKey = :itemKey LIMIT 1")
    suspend fun indexedByIdentity(sourceId: String, itemKey: String): IndexedFileEntity?

    @Query("SELECT COUNT(*) FROM indexed_files")
    suspend fun indexedCount(): Long

    @Query("SELECT COUNT(*) FROM indexed_files WHERE sourceId = :sourceId")
    suspend fun indexedCountForSource(sourceId: String): Long

    @Query("DELETE FROM indexed_files WHERE sourceId = :sourceId AND lastSeenScanId != :scanId")
    suspend fun deleteUnseen(sourceId: String, scanId: String): Int

    /** Future scan coordinator must call this only after a fully successful source scan. */
    @Transaction
    suspend fun replaceCompletedSource(
        sourceId: String,
        scanId: String,
        entries: List<IndexedFileEntity>
    ) {
        require(entries.all { it.sourceId == sourceId && it.lastSeenScanId == scanId })
        // Existing row ids must be preserved for a stable identity.
        for (entry in entries) {
            val old = indexedByIdentity(sourceId, entry.itemKey)
            putIndexed(listOf(entry.copy(id = old?.id ?: 0L)))
        }
        deleteUnseen(sourceId, scanId)
    }
}
