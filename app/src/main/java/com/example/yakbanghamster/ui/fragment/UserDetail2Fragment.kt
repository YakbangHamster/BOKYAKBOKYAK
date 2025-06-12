package com.example.yakbanghamster.ui.fragment

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

class UserDetail2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_detail2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val heightInput = view.findViewById<EditText>(R.id.heightInput)
        val weightInput = view.findViewById<EditText>(R.id.weightInput)
        val nextButton = view.findViewById<Button>(R.id.btnNext2)


        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }


        nextButton.setOnClickListener {
            val name = arguments?.getString("name") ?: ""
            val age = arguments?.getString("age") ?: ""
            val gender = arguments?.getString("gender") ?: ""


            val height = heightInput.text.toString()
            val weight = weightInput.text.toString()


            if (height.isBlank() || weight.isBlank()) {
                Toast.makeText(requireContext(), "키와 몸무게를 모두 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("name", name)
                putString("age", age)
                putString("gender", gender)
                putString("height", height)
                putString("weight", weight)
            }

            val fragment = UserDetail3Fragment().apply {
                arguments = bundle
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
