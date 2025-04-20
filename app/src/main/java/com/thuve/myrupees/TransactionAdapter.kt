package com.thuve.myrupees

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onDelete: () -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.date)
        val category: TextView = itemView.findViewById(R.id.category)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        holder.amount.text = "Rs. %.2f".format(transaction.amount)
        holder.date.text = transaction.date
        holder.category.text = transaction.category

        val colorRes = if (transaction.type == "Income") {
            R.color.green
        } else {
            R.color.red
        }

        holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        holder.deleteBtn.setOnClickListener {
            val context = holder.itemView.context
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    transactions.removeAt(position)
                    SharedPrefManager.saveTransactions(context, transactions)
                    Log.d("TransactionAdapter", "Saved transactions: $transactions")
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, transactions.size)
                    onDelete() // Update balance in MainActivity
                    // Notify BudgetActivity of transaction change
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(android.content.Intent("TRANSACTION_UPDATED"))
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    override fun getItemCount(): Int = transactions.size
}