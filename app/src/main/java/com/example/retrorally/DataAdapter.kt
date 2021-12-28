package com.example.retrorally

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.data.models.Participant
import com.example.retrorally.databinding.DialogLayoutBinding


class DataAdapter(val c: Context, private val resultList: ArrayList<Participant>) :
    RecyclerView.Adapter<DataAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_data, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(resultList[position])

    }

    override fun getItemCount(): Int = resultList.size


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var number: TextView = itemView.findViewById(R.id.number_of_car)
        var score: TextView = itemView.findViewById(R.id.result)
        var inputMessage: ImageButton = itemView.findViewById(R.id.comment)

        fun onBind(result: Participant) {

            number.text = result.number
            score.text = result.score
            inputMessage.setOnClickListener {
                val messageView =
                    DialogLayoutBinding.inflate(LayoutInflater.from(itemView.context)).inputMessage
                messageView.setText(result.comment)
                AlertDialog.Builder(itemView.context)
                    .setTitle("Комментарий")
                    .setView(messageView)
                    .setPositiveButton("OK") { dialog, id ->

                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, id ->
                        dialog.cancel()
                    }
                    .create()
                    .show()
            }
        }
    }
}

