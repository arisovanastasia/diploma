package com.example.retrorally

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.data.models.Participant

class DataAdapter : RecyclerView.Adapter<DataAdapter.ItemViewHolder>() {

    private var list = mutableListOf<Participant>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_data, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list?.size


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var number: TextView = itemView.findViewById(R.id.number_of_car)
        var score: TextView = itemView.findViewById(R.id.result)
        var comment: TextView = itemView.findViewById(R.id.comments)

        fun onBind(participant: Participant) {
            number.text = participant.number.toString()
            score.text = participant.score
            comment.text = participant.comment
        }

    }
}

