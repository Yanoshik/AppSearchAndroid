package rs.appsearch.simple.files.index

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileIndexDatabaseTest {
    private lateinit var database: FileIndexDatabase
    private lateinit var dao: FileIndexDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FileIndexDatabase::class.java
        ).build()
        dao = database.fileIndexDao()
    }

    @After
    fun tearDown() = database.close()

    private fun item(key: String, scanId: String) = IndexedFileEntity(
        sourceId = "saf:my-docs",
        itemKey = key,
        providerAuthority = "com.android.externalstorage.documents",
        documentUri = "content://example/$key",
        documentId = key,
        displayName = "$key.txt",
        mimeType = "text/plain",
        sizeBytes = 15,
        modifiedAt = null,
        relativeLocation = null,
        fileCategory = "other",
        availability = "available",
        lastSeenScanId = scanId,
        indexedAt = 1
    )

    @Test
    fun completedSnapshotReplacesOldRowsAndAvoidsDuplicates() = runBlocking {
        dao.replaceCompletedSource("saf:my-docs", "scan-1", listOf(item("one", "scan-1"), item("two", "scan-1")))
        assertEquals(2L, dao.indexedCount())

        dao.replaceCompletedSource("saf:my-docs", "scan-2", listOf(item("one", "scan-2")))
        assertEquals(1L, dao.indexedCount())
        assertNotNull(dao.indexedByIdentity("saf:my-docs", "one"))

        dao.replaceCompletedSource("saf:my-docs", "scan-3", listOf(item("one", "scan-3")))
        assertEquals(1L, dao.indexedCount())
    }

    @Test
    fun stagingCanBeDiscardedWithoutChangingPublishedIndex() = runBlocking {
        dao.replaceCompletedSource("saf:my-docs", "good", listOf(item("original", "good")))
        dao.putStaged(listOf(StagedFileEntity(
            scanId = "cancelled", sourceId = "saf:my-docs", itemKey = "new",
            providerAuthority = null, documentUri = "content://example/new",
            documentId = "new", displayName = "new.txt", mimeType = "text/plain",
            sizeBytes = 4, modifiedAt = null, relativeLocation = null,
            fileCategory = "other", indexedAt = 2
        )))
        assertEquals(1L, dao.stagedCount("cancelled"))
        dao.discardStaged("cancelled")
        assertEquals(0L, dao.stagedCount("cancelled"))
        assertEquals(1L, dao.indexedCount())
    }
}
