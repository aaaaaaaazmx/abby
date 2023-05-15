package com.cl.modules_contact.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cl.modules_contact.R

internal class InEditModeAdapter(
    private val count: Int
) : NineGridView.Adapter() {

    override fun getItemCount(): Int {
        return count
    }

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.ngv_item_in_edit_mode, parent, false)
    }

    override fun onBindItemView(itemView: View, viewType: Int, position: Int) {
//        val tvItem = itemView.findViewById<TextView>(R.id.tvItem)
//        tvItem.text = String.format("item %s", position)
    }
}