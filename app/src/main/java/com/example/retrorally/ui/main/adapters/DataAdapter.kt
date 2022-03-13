package com.example.retrorally.ui.main.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.R
import com.example.retrorally.data.models.Participant
import com.example.retrorally.databinding.DialogLayoutBinding

class DataAdapter(
    val c: Context,
    private var resultList: MutableList<Participant>,
    private val onParticipantChanged: (Participant, Int) -> Unit
) :
    RecyclerView.Adapter<DataAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_data, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(resultList[position], position, onParticipantChanged)
    }

    override fun getItemCount(): Int = resultList.size

    fun setData(participants: MutableList<Participant>) {
        resultList = participants
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var number: EditText = itemView.findViewById(R.id.number_of_car)
        var score: EditText = itemView.findViewById(R.id.result)
        var inputMessage: ImageButton = itemView.findViewById(R.id.comment)
        var onChangeButton: ImageButton = itemView.findViewById(R.id.on_change_button)
        var newComment = ""

        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChangeButton.isVisible = true
                Log.d("MyChange", "меня изменили!")
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        fun onBind(
            participant: Participant,
            position: Int,
            onParticipantChanged: (Participant, Int) -> Unit
        ) {
            score.removeTextChangedListener(textWatcher)
            val idOfParticipant = participant.idOfString
            number.setText(participant.participant)
            score.setText(participant.result)
            score.addTextChangedListener(textWatcher)
            inputMessage.setOnClickListener {
                val messageView =
                    DialogLayoutBinding.inflate(LayoutInflater.from(itemView.context)).inputMessage
                messageView.setText(participant.comment)
                newComment = messageView.text.toString()
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
            onChangeButton.setOnClickListener {
                onParticipantChanged(
                    Participant(
                        idOfParticipant,
                        number.text.toString(),
                        score.text.toString(),
                        newComment
                    ),
                    position
                )
                onChangeButton.isVisible = false
            }
        }
    }
}

