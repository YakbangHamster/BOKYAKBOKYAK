import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.Medicine

class MedicineAdapter(
    private var items: List<Medicine>,
    private val onItemClick: ((Medicine) -> Unit)? = null,
    private val onFooterClick: () -> Unit = {},
    private val showFooter: Boolean = false,
    private val footerLayoutRes: Int = R.layout.item_footer_more
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var registeredMedicines: MutableSet<String> = mutableSetOf()

    fun addRegisteredMedicine(name: String?) {
        if (!name.isNullOrBlank()) {
            registeredMedicines.add(name)
            notifyDataSetChanged()
        }
    }

    fun setRegisteredMedicines(names: Set<String>) {
        registeredMedicines.clear()
        registeredMedicines.addAll(names)
        notifyDataSetChanged()
    }


    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    override fun getItemCount(): Int = items.size + if (showFooter) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (showFooter && position == items.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.medicineName)
        val detail: TextView = view.findViewById(R.id.medicineDetail)
        val hamster: ImageView = view.findViewById(R.id.cardHamster)
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnFooter: View = view.findViewById(R.id.btn_more) ?: view.findViewById(R.id.btn_add)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pill_list, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(footerLayoutRes, parent, false)
            FooterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder && position < items.size) {
            val item = items[position]
            holder.name.text = item.name

            if (registeredMedicines.contains(item.name)) {
                holder.detail.text = "복용약 정보 등록이 완료되었어요!"
                holder.detail.setTextColor(android.graphics.Color.parseColor("#FFA655"))
            } else {
                holder.detail.text = ""
                holder.detail.setTextColor(android.graphics.Color.parseColor("#8391A1"))
            }

            holder.itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        } else if (holder is FooterViewHolder) {
            holder.btnFooter.setOnClickListener { onFooterClick() }
        }
    }

    fun updateItems(newItems: List<Medicine>) {
        items = newItems
        notifyDataSetChanged()
    }
}
