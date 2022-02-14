package com.example.retrorally.ui.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorally.R

class TestAdapter(val context : Context, private var listOfItems: MutableList<String>, private val setTimeToView : (String) -> Unit) :
    RecyclerView.Adapter<TestAdapter.TestItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TestItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.test_list_item, parent, false)
        return TestItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestItemViewHolder, position: Int) {
        holder.onBindTest(listOfItems[position], setTimeToView)
    }

    override fun getItemCount() = listOfItems.size

    fun setTestData(timeList : MutableList<String>) {
        listOfItems = timeList
        notifyDataSetChanged()
    }

    class TestItemViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var timeButton: Button = item.findViewById(R.id.test_button)
        fun onBindTest(timeData: String, setTimeToView: (String) -> Unit) {
            timeButton.text = timeData
            timeButton.setOnClickListener {
                setTimeToView(timeButton.text.toString())
            }
        }
    }
}