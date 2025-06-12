package com.example.yakbanghamster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.MedicineTodayData
import com.example.yakbanghamster.data.MedicineTodayResponse
import java.text.SimpleDateFormat
import java.util.Locale

class MedicineTodayAdapter(
    private var items: List<MedicineTodayData>,
    private val onItemClick: (MedicineTodayData) -> Unit
) : RecyclerView.Adapter<MedicineTodayAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hamsterImage: ImageView = itemView.findViewById(R.id.cardHamster)
        val medicineTime: TextView = itemView.findViewById(R.id.medicineTime)
        val medicineName: TextView = itemView.findViewById(R.id.medicineName)
        val arrowImage: ImageView = itemView.findViewById(R.id.cardArrow)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
    }

    fun formatToAmPm(time: String): String {
        val sdf24 = SimpleDateFormat("HH:mm", Locale.KOREA)
        val sdf12 = SimpleDateFormat("a hh:mm", Locale.KOREA)
        val date = sdf24.parse(time)
        return sdf12.format(date)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_home_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val formattedTimes = item.timeList.map { formatToAmPm(it) }
        holder.medicineTime.text = formattedTimes.joinToString(", ")
        holder.medicineName.text = item.medicineName

        holder.hamsterImage.setImageResource(R.drawable.detail_hamster)
        holder.arrowImage.setImageResource(R.drawable.next)
    }


    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MedicineTodayData>) {
        items = newItems
        notifyDataSetChanged()
    }
}

