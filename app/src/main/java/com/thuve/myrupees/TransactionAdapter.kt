package com.thuve.myrupees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        if(transaction.type == "Income"){
            holder.amount.setTextColor(holder.itemView.context.resources.getColor(R.color.green))
            holder.amount.text = "Rs. ${transaction.amount}"
        }else{
            holder.amount.setTextColor(holder.itemView.context.resources.getColor(R.color.red))
            holder.amount.text = "Rs. ${transaction.amount}"
        }
        holder.date.text = transaction.date
    }

    override fun getItemCount(): Int = transactions.size
}
