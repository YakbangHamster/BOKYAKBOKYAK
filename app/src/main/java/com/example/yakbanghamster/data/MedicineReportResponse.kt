import com.example.yakbanghamster.data.MedicineReportData

data class MedicineReportResponse(
    val status: Int,
    val message: String,
    val data: MedicineReportData?
)