package rs.appsearch.simple.files.index

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Phase 3.1 database only: no scans are started by constructing this database.
 * No destructive migration fallback. Future versions must add explicit migrations.
 */
@Database(
    entities = [
        IndexedFileEntity::class,
        IndexSourceEntity::class,
        ScanSessionEntity::class,
        StagedFileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FileIndexDatabase : RoomDatabase() {
    abstract fun fileIndexDao(): FileIndexDao

    companion object {
        @Volatile private var instance: FileIndexDatabase? = null

        fun get(context: Context): FileIndexDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FileIndexDatabase::class.java,
                    "appsearch_file_index.db"
                ).build().also { instance = it }
            }
    }
}
