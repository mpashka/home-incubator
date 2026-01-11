package com.receipt.scanner.data

data class ScanRecord(
    val id: Int,
    val url: String,
    val status: String,
    val receiptId: Int?,
    val timestamp: Long
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_PROCESSING = "processing"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_FAILED = "failed"
        const val STATUS_SENDING = "sending"
        const val STATUS_SEND_FAILED = "send_failed"
    }
}
