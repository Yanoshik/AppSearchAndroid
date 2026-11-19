package rs.appsearch.simple.files.scan

/**
 * Contract for the future MediaStore implementation (Phase 3.2.2+).
 *
 * Implementations must:
 * - check permissions immediately before each source query;
 * - only emit metadata that Android currently permits;
 * - stream bounded batches rather than loading an entire collection;
 * - close cursors and propagate coroutine cancellation safely;
 * - distinguish an empty successful query from a failed or denied query;
 * - never read, modify, copy or delete the underlying file content.
 *
 * This interface intentionally does not start any scan, write to Room, or touch the UI.
 */
interface MediaStoreScanner {
    suspend fun scan(
        request: MediaScanRequest,
        onBatch: suspend (source: MediaScanSource, items: List<MediaScanItem>) -> Unit
    ): MediaScanResult
}
