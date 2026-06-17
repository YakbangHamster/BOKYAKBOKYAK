package com.example.yakbanghamster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.MedicineTodayData
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
            // 화살표만 클릭 - 캘린더로 이동
            arrowImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onArrowClick?.invoke(items[position])
                }
            }
        }
    }

    fun formatToAmPm(time: String): String {
        return try {
            if (time.isBlank()) return "시간미정"
            val sdf24 = SimpleDateFormat("HH:mm", Locale.KOREA)
            val sdf12 = SimpleDateFormat("a hh:mm", Locale.KOREA)
            val date = sdf24.parse(time) ?: return time
            sdf12.format(date)
        } catch (e: Exception) {
            // 파싱 실패시 원본 시간 또는 "시간미정" 반환
            time.ifBlank { "시간미정" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_home_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val formattedTimes = item.timeList.mapNotNull { time ->
            if (time.isNotBlank()) formatToAmPm(time) else null
        }.takeIf { it.isNotEmpty() } ?: listOf("시간미정")

        holder.medicineTime.text = formattedTimes.joinToString(", ")
        holder.medicineName.text = item.medicineName

        // 이미지가 있으면 식약처 사진, 없으면 기존 햄스터
        if (!item.image.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(item.image)
                .override(144, 144)
                .centerCrop()
                .placeholder(R.drawable.detail_hamster)
                .error(R.drawable.detail_hamster)
                .into(holder.hamsterImage)
        } else {
            holder.hamsterImage.setImageResource(R.drawable.detail_hamster)
        }

        holder.arrowImage.setImageResource(R.drawable.next)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MedicineTodayData>) {
        items = newItems
        notifyDataSetChanged()
    }

    private var onArrowClick: ((MedicineTodayData) -> Unit)? = null

    fun setOnArrowClickListener(listener: (MedicineTodayData) -> Unit) {
        this.onArrowClick = listener
    }
}
