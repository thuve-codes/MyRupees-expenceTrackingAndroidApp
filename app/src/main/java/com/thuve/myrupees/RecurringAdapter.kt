package com.thuve.myrupees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class RecurringAdapter(
    private val transactions: MutableList<RecurringTransaction>,
    private val onPaidClick: ((RecurringTransaction) -> Unit)?
) : RecyclerView.Adapter<RecurringAdapter.ViewHolder>() {

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
        val transaction = transactions[position]
        holder.title.text = transaction.title
        holder.amount.text = "Rs. %.2f".format(transaction.amount)
        holder.date.text = transaction.scheduledDate

        if (transaction.isPaid || onPaidClick == null) {
            holder.paidBtn.visibility = View.GONE
        } else {
            holder.paidBtn.visibility = View.VISIBLE
            holder.paidBtn.setOnClickListener {
                onPaidClick.invoke(transaction)
            }
        }
    }

    override fun getItemCount() = transactions.size
}
