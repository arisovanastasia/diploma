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
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.R
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.models.dto.ResultsDTO
import com.example.retrorally.databinding.DialogLayoutBinding
import com.example.retrorally.databinding.LineDataLayoutBinding
import com.example.retrorally.ui.main.view.JudgeFragment

class DataAdapter(
    val c: Context,
    private var resultList: MutableList<Participant>,
    private val inputs: List<String>,
    private val fastComments: List<String>,
    private val onParticipantChanged: (Participant, Int) -> Unit
) :
    RecyclerView.Adapter<DataAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_data, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(resultList[position], position, inputs, fastComments, onParticipantChanged)
    }

    override fun getItemCount(): Int = resultList.size

    fun setData(participants: MutableList<Participant>) {
        resultList = participants
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var clickable: View = itemView.findViewById(R.id.result_clickable)
        var number: TextView = itemView.findViewById(R.id.number_of_car)
        var score: TextView = itemView.findViewById(R.id.result)
        var comment: TextView = itemView.findViewById(R.id.comment)

        fun onBind(
            participant: Participant,
            position: Int,
            inputs: List<String>,
            fastComments: List<String>,
            onParticipantChanged: (Participant, Int) -> Unit
        ) {
            val idOfParticipant = participant.idOfString
            number.setText(participant.participant)
            score.setText(getResultAsText(participant.result))
            comment.setText(participant.comment)
            clickable.setOnLongClickListener {
                val dialogView = LineDataLayoutBinding.inflate(LayoutInflater.from(itemView.context))
                JudgeFragment.adjustVisibility(inputs, fastComments, dialogView, false)
                JudgeFragment.fillInputs(participant, dialogView)
                AlertDialog.Builder(itemView.context)
                    .setTitle("Редактирование протокола")
                    .setView(dialogView.root)
                    .setPositiveButton("OK") { dialog, id ->
                        dialog.dismiss()

                        onParticipantChanged(
                            Participant(
                                idOfParticipant,
                                JudgeFragment.getCarNumber(dialogView),
                                JudgeFragment.getResult(dialogView),
                                JudgeFragment.getComment(dialogView)),
                            position)
                    }
                    .setNegativeButton("Отмена") { dialog, id ->
                        dialog.cancel()
                    }
                    .create()
                    .show()
                true
            }
        }

        private fun getResultAsText(resultsDTO: ResultsDTO): String {
            var result = ""
            // Первая строка
            if (resultsDTO.time_hm != null) {
                result += "Прибытие: ${resultsDTO.time_hm} "
            }
            if (resultsDTO.time_hms != null) {
                result += "Время: ${resultsDTO.time_hms} "
            }
            if (resultsDTO.set_time != null) {
                result += "Назначено: ${resultsDTO.set_time} "
            }

            // Это отдельной строкой?
            if (resultsDTO.start_time != null) {
                result += "\nСтарт: ${resultsDTO.start_time} "
            }
            if (resultsDTO.finish_time != null) {
                result += "Финиш: ${resultsDTO.finish_time}"
            }

            // А это точно второй/третьей строкой пойдет
            if (resultsDTO.cones != null) {
                result += "\nКонусы: ${resultsDTO.cones} "
            }
            if (resultsDTO.buttons != null) {
                result += "Стаканы: ${resultsDTO.buttons} "
            }
            if (resultsDTO.stop_line != null) {
                result += "Стоп-линия: ${resultsDTO.stop_line} "
            }
            if (resultsDTO.base != null) {
                result += "База: ${resultsDTO.base} "
            }
            if (resultsDTO.scheme != null) {
                result += "Схема: ${resultsDTO.scheme} "
            }
            return result
        }
    }
}

