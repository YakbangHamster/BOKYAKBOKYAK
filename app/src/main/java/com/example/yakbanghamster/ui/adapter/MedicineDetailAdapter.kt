import android.icu.text.SimpleDateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.MedicineDetailData
import java.util.Locale

class MedicineDetailAdapter(
    private var items: List<MedicineDetailData>
) : RecyclerView.Adapter<MedicineDetailAdapter.ViewHolder>() {

    // 선택된 아이템 position 저장
    private val selectedItems = mutableSetOf<Int>()

    // 복용 시작 버튼 클릭 여부
    var isStartButtonClicked = false

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txt_medicine_name)
        val timesLayout: LinearLayout = itemView.findViewById(R.id.layout_times)
        val checkBox: ImageView = itemView.findViewById(R.id.img_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name

        // 체크박스 상태 반영 (선택/비선택에 따라 이미지 바꾸고 싶으면 아래 코드 활용)
        holder.checkBox.isSelected = selectedItems.contains(position)
        holder.checkBox.setOnClickListener {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            notifyItemChanged(position)
        }

        // 기존 복용 시간 뷰 제거
        holder.timesLayout.removeAllViews()
        for (t in item.time) {
            // 24시간 → 12시간+AM/PM 변환 (항상 영어)
            val formattedTime = try {
                val sdf24 = SimpleDateFormat("HH:mm", Locale.ENGLISH)
                val sdf12 = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                sdf12.format(sdf24.parse(t)!!)
            } catch (e: Exception) {
                t
            }

            val tv = TextView(holder.timesLayout.context).apply {
                text = formattedTime
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColorStateList(context, R.color.time_button_text_color)!!)
                setBackgroundResource(R.drawable.time_button_bg)
                setPadding(10, 5, 10, 6)
                gravity = android.view.Gravity.CENTER

                // 반드시 isStartButtonClicked가 true일 때만 배경 적용!
                isSelected = isStartButtonClicked && selectedItems.contains(position)
            }



            val widthPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 73f, holder.timesLayout.context.resources.displayMetrics
            ).toInt()
            val heightPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f, holder.timesLayout.context.resources.displayMetrics
            ).toInt()
            val params = LinearLayout.LayoutParams(widthPx, heightPx)
            params.marginEnd = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, holder.timesLayout.context.resources.displayMetrics
            ).toInt()
            tv.layoutParams = params

            holder.timesLayout.addView(tv)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MedicineDetailData>) {
        items = newItems
        notifyDataSetChanged()
    }

    // 외부에서 복용 시작 버튼 클릭 시 호출
    fun onStartButtonClicked() {
        isStartButtonClicked = true
        notifyDataSetChanged()
    }

    // 선택된 아이템 정보 반환 (필요시)
    fun getSelectedItems(): List<MedicineDetailData> {
        return selectedItems.map { items[it] }
    }
}
