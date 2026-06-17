package com.example.yakbanghamster

import MedicineAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbanghamster.data.Medicine
import com.example.yakbanghamster.data.RetrofitInstance
import com.example.yakbanghamster.network.MedicineService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.InputStream

class PillOcrResultActivity : BaseActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_pill_ocr_result

    private val medicineService: MedicineService by lazy {
        RetrofitInstance.medicineService
    }

    private lateinit var detailRegisterLauncher: ActivityResultLauncher<Intent>
    private val registeredMedicines: MutableSet<String> = mutableSetOf()
    private lateinit var adapter: MedicineAdapter

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageUri(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backbutton = findViewById<ImageView>(R.id.btn_back)
        backbutton.setOnClickListener {
            startActivity(Intent(this, PillOcrActivity::class.java))
            finish()
        }

        val loadingLayout = findViewById<LinearLayout>(R.id.loadingLayout)
        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)
        val recyclerView = findViewById<RecyclerView>(R.id.medicineRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicineAdapter(
            items = emptyList(),
            showFooter = true,
            footerLayoutRes = R.layout.item_footer_add,
            onItemClick = { medicine ->
                val intent = Intent(this, PillDetailActivity::class.java)
                intent.putExtra("medicine_name", medicine.name)
                detailRegisterLauncher.launch(intent)
            },
            onFooterClick = {
                Toast.makeText(this, "복용약 추가하기 클릭!", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        // 시작할 때 로딩만 보이고 내용은 숨김
        loadingLayout.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        // 상세 등록 결과 받기
        detailRegisterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val registeredName = result.data?.getStringExtra("registered_medicine_name")
                if (!registeredName.isNullOrBlank()) {
                    registeredMedicines.add(registeredName)
                    adapter.addRegisteredMedicine(registeredName)
                }
            }
        }

        galleryLauncher.launch("image/*")
    }

    // Imgur URL 변환 함수
    private fun convertImgurUrl(url: String): String {
        val regex = Regex("""https?://imgur\.com/(\w+)""")
        val match = regex.find(url)
        return if (match != null) {
            val imageId = match.groupValues[1]
            "https://i.imgur.com/$imageId.png"
        } else {
            url
        }
    }

    // 이미지 URI를 Imgur에 업로드
    private fun handleImageUri(uri: Uri) {
        lifecycleScope.launch {
            val imgurUrl = uploadImageToImgur(uri)
            if (imgurUrl != null) {
                val finalUrl = convertImgurUrl(imgurUrl)
                Log.d("ImgurUpload", "최종 이미지 URL: $finalUrl")
                // 2. 서버 OCR API 호출 (Retrofit 사용)
                val medicines = callOcrApi(finalUrl)
                showOcrResult(medicines)
            } else {
                Toast.makeText(this@PillOcrResultActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                hideLoading()
            }
        }
    }

    // Imgur 업로드 함수
    private suspend fun uploadImageToImgur(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val imageBytes = inputStream?.readBytes() ?: return@withContext null
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", null, RequestBody.create("image/*".toMediaType(), imageBytes))
                .build()

            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .addHeader("Authorization", "Client-ID b13fded8e639856")
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("ImgurUpload", "Imgur Response: $responseBody")
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                return@withContext json.getJSONObject("data").getString("link")
            }
        } catch (e: Exception) {
            Log.e("ImgurUpload", "업로드 실패", e)
        }
        return@withContext null
    }

    // 서버 OCR API 호출 함수 (Retrofit)
    private suspend fun callOcrApi(imgUrl: String): List<Medicine> = withContext(Dispatchers.IO) {
        Log.d("OCR API", "OCR API 호출 이미지 URL: $imgUrl")
        try {
            val json = JSONObject()
            json.put("url", imgUrl)
            val body = RequestBody.create("application/json".toMediaType(), json.toString())

            val responseBody = medicineService.ocrMedicines(body)
            val responseString = responseBody.string()
            Log.d("OCR API", "서버 응답: $responseString")

            val result = mutableListOf<Medicine>()
            val respJson = JSONObject(responseString)
            val dataArray = respJson.optJSONArray("data")
            if (dataArray != null) {
                for (i in 0 until dataArray.length()) {
                    val name = dataArray.getString(i)
                    result.add(Medicine(serial = "", name = name, image = "", efficacy = "", howToTake = ""))
                }
            }
            return@withContext result
        } catch (e: Exception) {
            Log.e("OCR API", "서버 OCR 호출 에러", e)
            return@withContext emptyList()
        }
    }

    private fun showOcrResult(medicineList: List<Medicine>) {
        val loadingLayout = findViewById<LinearLayout>(R.id.loadingLayout)
        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)

        runOnUiThread {
            loadingLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
            adapter.updateItems(medicineList)
            // 이미 등록된 약들 강조(뒤로가기 후에도 유지)
            adapter.setRegisteredMedicines(registeredMedicines)
        }
    }

    private fun hideLoading() {
        val loadingLayout = findViewById<LinearLayout>(R.id.loadingLayout)
        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)
        runOnUiThread {
            loadingLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
        }
    }
}
