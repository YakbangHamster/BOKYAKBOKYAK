package com.example.yakbanghamster.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yakbanghamster.R
import com.example.yakbanghamster.HomeActivity
import com.example.yakbanghamster.data.RetrofitInstance
import com.example.yakbanghamster.viewmodel.UserDetail
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDetail3Fragment : Fragment() {

    private val selectedDiseases = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_detail3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextOtherDisease = view.findViewById<EditText>(R.id.otherDiseaseInput)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)

        val diseaseButtons = listOf(
            view.findViewById<MaterialButton>(R.id.btnHypertension) to "고혈압",
            view.findViewById<MaterialButton>(R.id.btnDiabetes) to "당뇨",
            view.findViewById<MaterialButton>(R.id.btnHyperlipidemia) to "고지혈증",
            view.findViewById<MaterialButton>(R.id.btnAsthma) to "천식",
            view.findViewById<MaterialButton>(R.id.btnGastritis) to "위염",
            view.findViewById<MaterialButton>(R.id.btnUlcer) to "궤양",
            view.findViewById<MaterialButton>(R.id.btnIrritableBowel) to "과민성대장증후군",
            view.findViewById<MaterialButton>(R.id.btnReflux) to "역류성 식도염",
            view.findViewById<MaterialButton>(R.id.btnAtopy) to "아토피",
            view.findViewById<MaterialButton>(R.id.btnDryEye) to "안구건조증",
            view.findViewById<MaterialButton>(R.id.btnConjunctivitis) to "결막염",
            view.findViewById<MaterialButton>(R.id.btnDepression) to "우울증",
            view.findViewById<MaterialButton>(R.id.btnInsomnia) to "불면증",
            view.findViewById<MaterialButton>(R.id.btnAnxiety) to "불안장애"
        )

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        diseaseButtons.forEach { (button, diseaseName) ->
            button.setOnClickListener {
                button.isSelected = !button.isSelected
                if (button.isSelected) {
                    selectedDiseases.add(diseaseName)
                } else {
                    selectedDiseases.remove(diseaseName)
                }
            }
        }

        val btnStart = view.findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {

            val finalSelectedDiseases = mutableListOf<String>().apply {
                addAll(selectedDiseases)


                val otherDisease = editTextOtherDisease.text.toString().trim()
                if (otherDisease.isNotEmpty()) {
                    add(otherDisease)
                }
            }

            val genderString = arguments?.getString("gender")
            val sexBoolean = when(genderString) {
                "남성" -> true
                "여성" -> false
                else -> null
            }

            val userDetail = UserDetail(
                name = arguments?.getString("name"),
                age = arguments?.getString("age")?.toIntOrNull(),
                sex = sexBoolean,
                height = arguments?.getString("height")?.toDoubleOrNull(),
                weight = arguments?.getString("weight")?.toDoubleOrNull(),
                disease = finalSelectedDiseases
            )


            sendUserDetailToApi(userDetail)
        }
    }

    private fun sendUserDetailToApi(userDetail: UserDetail) {
        val call = RetrofitInstance.userDetailService.sendUserDetail(userDetail)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "정보가 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
