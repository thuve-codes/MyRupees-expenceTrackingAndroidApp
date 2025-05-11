package com.thuve.myrupees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecurringAdapter(
    private val onPaidClick: ((RecurringTransaction) -> Unit)?
) : ListAdapter<RecurringTransaction, RecurringAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleText)
        val amount: TextView = itemView.findViewById(R.id.amountText)
        val date: TextView = itemView.findViewById(R.id.dateText)
        val paidBtn: Button = itemView.findViewById(R.id.paidBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recurring_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.title.text = transaction.title
        holder.amount.text = "Rs. %.2f".format(transaction.amount)
        holder.date.text = transaction.scheduledDate

        if (transaction.paid || onPaidClick == null) {
            holder.paidBtn.visibility = View.GONE
        } else {
            holder.paidBtn.visibility = View.VISIBLE
            holder.paidBtn.setOnClickListener {
                onPaidClick?.invoke(transaction.copy(paid = true))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RecurringTransaction>() {
        override fun areItemsTheSame(oldItem: RecurringTransaction, newItem: RecurringTransaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecurringTransaction, newItem: RecurringTransaction): Boolean {
            return oldItem == newItem
        }
    }
}