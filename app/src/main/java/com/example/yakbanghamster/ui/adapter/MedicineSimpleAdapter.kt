import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.MedicineSimple

class MedicineSimpleAdapter(
    private var items: List<MedicineSimple>,
    private val onItemClick: ((MedicineSimple) -> Unit)? = null
) : RecyclerView.Adapter<MedicineSimpleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.medicineName)
        val detail: TextView = itemView.findViewById(R.id.medicineDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pill_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.medicineName
        holder.detail.text = item.date ?: "아직 상세 정보가 등록되지 않았어요!"
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MedicineSimple>) {
        items = newItems
        notifyDataSetChanged()
    }
}
