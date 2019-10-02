package com.pocnative.bonfim.pocnativeandroid.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pocnative.bonfim.pocnativeandroid.HistoricDetailActivity
import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.historic.HistoricFragment
import com.pocnative.bonfim.pocnativeandroid.models.PhysicalActivity

class HistoricAdapter(
        private val historic: ArrayList<PhysicalActivity>,
        private val context: Context
) : RecyclerView.Adapter<HistoricAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val historicItem = LayoutInflater.from(parent.context)
                .inflate(R.layout.historic_item, parent, false)

        return ViewHolder(historicItem)
    }

    override fun getItemCount(): Int {
        return historic.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPhysicalActivity(historic[position])
        holder.tvDuration.text = "Duration: ${historic[position].duration}"
        holder.tvDate.text = "Completed on: ${historic[position].date}"
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        lateinit var physicalActivity: PhysicalActivity
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val intent = Intent(context, HistoricDetailActivity::class.java)
            intent.putExtra("phisycalACtivity", physicalActivity)
            context.startActivity(intent)
        }

        fun bindPhysicalActivity(physicalActivity: PhysicalActivity){
            this.physicalActivity = physicalActivity
        }
    }
}