package com.example.eveosample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.eveosample.R

class OptionsAdapter(private val context: Context, private val options:
List<String>):ArrayAdapter<String>(context,
    android.R.layout.simple_list_item_1, options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_option, parent, false)
        val textView: TextView = view.findViewById(R.id.textOption)
        textView.text = getItem(position)
        return view
    }

}