package rs.appsearch.simple.files.scan

/**
 * Phase 3.2.1: contracts only, without querying MediaStore or changing permissions.
 * Times are milliseconds since the Unix epoch; unknown metadata is nullable.
 */
enum class MediaCategory { IMAGE, VIDEO, AUDIO }
enum class MediaAccess { FULL, LIMITED, DENIED, UNAVAILABLE }
enum class MediaScanStatus { COMPLETED, PARTIAL, DENIED, UNAVAILABLE, CANCELLED, FAILED }
enum class MediaScanIssue { PERMISSION_REVOKED, QUERY_FAILED, INVALID_ROW, SOURCE_UNAVAILABLE }

/** One MediaStore collection on one volume; never scan aggregate and individual volumes together. */
data class MediaScanSource(
    val category: MediaCategory,
    val volumeName: String,
    val collectionUri: String,
    val access: MediaAccess
) {
    /** Includes category and volume to avoid collisions between independent collections. */
    val sourceId: String get() = "mediastore:${category.name.lowercase()}:$volumeName"
}

/**
 * All requested sources are rechecked against live Android grants by the eventual scanner.
 * batchSize is an upper bound on emitted entries, not a promised page size in the provider.
 */
data class MediaScanRequest(
    val sources: List<MediaScanSource>,
    val batchSize: Int = 200
) {
    init { require(batchSize in 1..1000) { "batchSize must be between 1 and 1000" } }
}

/**
 * Immutable metadata record. itemKey is stable within sourceId only.
 * It must not be derived from displayName or a file path.
 */
data class MediaScanItem(
    val sourceId: String,
    val itemKey: String,
    val mediaId: Long,
    val contentUri: String,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val modifiedAtMillis: Long?,
    val category: MediaCategory,
    val volumeName: String
)

data class MediaScanWarning(
    val sourceId: String,
    val issue: MediaScanIssue,
    val detail: String? = null
)

/** Counts reflect entries delivered to the batch callback, not necessarily committed to Room. */
data class MediaSourceResult(
    val sourceId: String,
    val status: MediaScanStatus,
    val processedCount: Long,
    val warnings: List<MediaScanWarning> = emptyList()
)

data class MediaScanResult(
    val sources: List<MediaSourceResult>
) {
    val processedCount: Long get() = sources.sumOf { it.processedCount }
    val fullyCompleted: Boolean get() = sources.all { it.status == MediaScanStatus.COMPLETED }
}
