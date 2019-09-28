package com.pocnative.bonfim.pocnativeandroid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.models.PhysicalActivity

class HistoricAdapter(private val historic: ArrayList<PhysicalActivity>) :
        RecyclerView.Adapter<HistoricAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val historicItem = LayoutInflater.from(parent.context)
                .inflate(R.layout.historic_item, parent, false)

        return ViewHolder(historicItem)
    }

    override fun getItemCount(): Int {
        return historic.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvDuration.text = "Duration: ${historic[position].duration}"
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)
    }
}