package com.receipt.scanner.ui

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.receipt.scanner.R
import com.receipt.scanner.data.ScanRecord
import com.receipt.scanner.databinding.ItemScanHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<ScanRecord, HistoryAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemScanHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: ScanRecord) {
            val context = binding.root.context

            // Status indicator color
            val statusColor = when (record.status) {
                ScanRecord.STATUS_COMPLETED -> R.color.success
                ScanRecord.STATUS_FAILED, ScanRecord.STATUS_SEND_FAILED -> R.color.error
                else -> R.color.pending
            }

            val drawable = binding.statusIndicator.background as? GradientDrawable
            drawable?.setColor(ContextCompat.getColor(context, statusColor))

            // Status text
            binding.statusText.text = when (record.status) {
                ScanRecord.STATUS_PENDING -> context.getString(R.string.status_pending)
                ScanRecord.STATUS_COMPLETED -> context.getString(R.string.status_completed)
                ScanRecord.STATUS_FAILED -> context.getString(R.string.status_failed)
                ScanRecord.STATUS_SENDING -> context.getString(R.string.sending)
                ScanRecord.STATUS_SEND_FAILED -> context.getString(R.string.send_failed)
                else -> record.status.replaceFirstChar { it.uppercase() }
            }

            // Timestamp
            binding.timestampText.text = dateFormat.format(Date(record.timestamp))

            // URL (truncated)
            binding.urlText.text = record.url

            // Receipt ID
            if (record.receiptId != null) {
                binding.receiptIdText.visibility = View.VISIBLE
                binding.receiptIdText.text = "Receipt ID: ${record.receiptId}"
            } else {
                binding.receiptIdText.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScanRecord>() {
        override fun areItemsTheSame(oldItem: ScanRecord, newItem: ScanRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScanRecord, newItem: ScanRecord): Boolean {
            return oldItem == newItem
        }
    }
}
