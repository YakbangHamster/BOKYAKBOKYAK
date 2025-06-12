import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.yakbanghamster.R
import com.example.yakbanghamster.data.RetrofitInstance
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class SideEffectBottomSheet(
    private val medicineName: String
) : BottomSheetDialogFragment() {


    override fun getTheme(): Int = R.style.BottomSheetDialogTheme_NoDim

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog.window?.setDimAmount(0f)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_side_effect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = view.findViewById<TextView>(R.id.sideEffectTitle)
        val content = view.findViewById<TextView>(R.id.sideEffectContent)
        title.text = "$medicineName 부작용 ⚠"
        content.text = "로딩 중..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = RetrofitInstance.medicineService.getSideEffect(medicineName)
                val json = JSONObject(result)
                val sideEffect = json.optString("data", "부작용 정보를 불러올 수 없습니다.")
                content.text = sideEffect
            } catch (e: Exception) {
                content.text = "불러오기 실패: ${e.message}"
            }
        }
    }
}
