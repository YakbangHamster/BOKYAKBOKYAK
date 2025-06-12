package com.example.yakbanghamster.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yakbanghamster.LoginActivity
import com.example.yakbanghamster.R
import com.example.yakbanghamster.viewmodel.UserDetailViewModel

class UserDetail1Fragment : Fragment(R.layout.fragment_user_detail1) {

    private lateinit var viewModel: UserDetailViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UserDetailViewModel::class.java]

        val editName = view.findViewById<EditText>(R.id.editName)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val editAge = view.findViewById<EditText>(R.id.editAge)
        val femaleOption = view.findViewById<FrameLayout>(R.id.femaleOption)
        val maleOption = view.findViewById<FrameLayout>(R.id.maleOption)
        var gender: String? = null

        backButton.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        femaleOption.setOnClickListener {
            gender = "여성"
            femaleOption.setBackgroundResource(R.drawable.gender_circle_active)
            maleOption.setBackgroundResource(R.drawable.gender_circle)
        }

        maleOption.setOnClickListener {
            gender = "남성"
            maleOption.setBackgroundResource(R.drawable.gender_circle_active)
            femaleOption.setBackgroundResource(R.drawable.gender_circle)
        }

        val nextButton = view.findViewById<Button>(R.id.btnNext1)
        nextButton.setOnClickListener {
            val name = editName.text.toString()
            val ageText = editAge.text.toString()
            if (name.isBlank() || ageText.isBlank()) {
                Toast.makeText(requireContext(), "이름과 나이를 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (gender == null) {
                Toast.makeText(requireContext(), "성별을 선택해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModel 업데이트 (선택)
            viewModel.updateName(name)
            viewModel.updateAge(ageText.toInt())
            viewModel.updateHeight(null)
            viewModel.updateWeight(null)

            // Bundle로 데이터 전달
            val bundle = Bundle().apply {
                putString("name", name)
                putString("age", ageText) // 문자열로 전달
                putString("gender", gender)
            }

            val fragment = UserDetail2Fragment().apply {
                arguments = bundle
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
