import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yakbanghamster.R
import com.example.yakbanghamster.TodayMedicineCache
import com.example.yakbanghamster.data.MedicineSimple

class MedicineSimpleAdapter(
    private var items: List<MedicineSimple>,
    private val onItemClick: ((MedicineSimple) -> Unit)? = null
) : RecyclerView.Adapter<MedicineSimpleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.medicineName)
        val detail: TextView = itemView.findViewById(R.id.medicineDetail)
        val thumb: ImageView = itemView.findViewById(R.id.cardHamster) // 기존 이미지뷰 재사용
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pill_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.medicineName

        val periodText = when {
            item.startDate != null && item.endDate != null ->
                "${item.startDate} ~ ${item.endDate}"
            item.startDate != null ->
                "시작: ${item.startDate}"
            item.endDate != null ->
                "종료: ${item.endDate}"
            item.date != null ->
                item.date
            else ->
                "상세 정보 등록 필요"
        }
        holder.detail.text = periodText

        val cachedImageUrl = TodayMedicineCache.imageCache[item.medicineName]
        if (!cachedImageUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(cachedImageUrl)
                .override(144, 144)
                .centerCrop()
                .placeholder(R.drawable.detail_hamster)
                .error(R.drawable.detail_hamster)
                .into(holder.thumb)
        } else {
            holder.thumb.setImageResource(R.drawable.detail_hamster)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MedicineSimple>) {
        Log.d("MedicineSimpleAdapter", "updateItems 호출: $newItems")
        items = newItems
        notifyDataSetChanged()
    }
}
