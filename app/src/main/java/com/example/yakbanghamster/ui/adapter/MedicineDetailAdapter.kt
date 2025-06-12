import android.icu.text.SimpleDateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.MedicineDetailData
import java.util.Locale

class MedicineDetailAdapter(
    private var items: List<MedicineDetailData>
) : RecyclerView.Adapter<MedicineDetailAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txt_medicine_name)
        val timesLayout: LinearLayout = itemView.findViewById(R.id.layout_times)
        val checkBox: ImageView = itemView.findViewById(R.id.img_checkbox)

        // 필요하다면 startDate, endDate용 TextView도 추가
        // val startDate: TextView = itemView.findViewById(R.id.txt_start_date)
        // val endDate: TextView = itemView.findViewById(R.id.txt_end_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.checkBox.setOnClickListener {
            holder.checkBox.isSelected = !holder.checkBox.isSelected
        }

        // 기존 뷰 제거
        holder.timesLayout.removeAllViews()
        for (t in item.time) {
            // 1. 24시간 문자열을 12시간+AM/PM으로 변환
            val formattedTime = try {
                val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                val sdf12 = SimpleDateFormat("h:mm a", Locale.getDefault())
                sdf12.format(sdf24.parse(t)!!)
            } catch (e: Exception) {
                t
            }

            val tv = TextView(holder.timesLayout.context).apply {
                text = formattedTime
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(holder.timesLayout.context.getColor(R.color.time_button_text))
                setBackgroundResource(R.drawable.time_button_bg)
                setPadding(10, 5, 10, 6)
                gravity = android.view.Gravity.CENTER
            }


            val widthPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 73f, holder.timesLayout.context.resources.displayMetrics
            ).toInt()
            val heightPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f, holder.timesLayout.context.resources.displayMetrics
            ).toInt()
            val params = LinearLayout.LayoutParams(widthPx, heightPx)

            // **여기서 간격(margin) 추가**
            params.marginEnd = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, holder.timesLayout.context.resources.displayMetrics
            ).toInt() // 8dp 정도 권장

            tv.layoutParams = params

            holder.timesLayout.addView(tv)

        }
    }


        override fun getItemCount(): Int = items.size

        fun updateItems(newItems: List<MedicineDetailData>) {
            items = newItems
            notifyDataSetChanged()
        }
    }

