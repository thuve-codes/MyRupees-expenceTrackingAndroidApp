package com.thuve.myrupees

import android.app.AlertDialog
import android.content.Intent
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
    private val onDelete: (Transaction) -> Unit,
    private val onEdit: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.date)
        val category: TextView = itemView.findViewById(R.id.category)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteBtn)
        val editBtn: ImageButton = itemView.findViewById(R.id.editBtn)
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

        val colorRes = if (transaction.type == "Income") R.color.green else R.color.red
        holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        holder.deleteBtn.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setMessage("Do you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    onDelete(transactions[position])
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }


        holder.editBtn.setOnClickListener {
            onEdit(transaction, position)
        }
    }

    override fun getItemCount(): Int = transactions.size

}