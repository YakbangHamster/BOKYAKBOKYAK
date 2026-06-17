package com.example.yakbanghamster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.annotations.SerializedName

data class UserDetail(
    @SerializedName("username")
    var name: String? = null,
    var age: Int? = null,
    var sex: Boolean? = null, // true = 남성, false = 여성, null = 미선택
    var height: Double? = null,
    var weight: Double? = null,
    var disease: List<String>? = null
)

class UserDetailViewModel : ViewModel() {
    val userDetail = MutableLiveData<UserDetail>(UserDetail())

    fun updateName(name: String) {
        val current = userDetail.value ?: UserDetail()
        current.name = name
        userDetail.value = current
    }

    fun updateAge(age: Int) {
        val current = userDetail.value ?: UserDetail()
        current.age = age
        userDetail.value = current
    }

    fun updateSex(sex: Boolean?) {
        val current = userDetail.value ?: UserDetail()
        current.sex = sex
        userDetail.value = current
    }

    fun updateHeight(height: Double?) {
        val current = userDetail.value ?: UserDetail()
        current.height = height
        userDetail.value = current
    }

    fun updateWeight(weight: Double?) {
        val current = userDetail.value ?: UserDetail()
        current.weight = weight
        userDetail.value = current
    }

    fun updateDisease(diseaseList: List<String>) {
        val current = userDetail.value ?: UserDetail()
        current.disease = diseaseList
        userDetail.value = current
    }

}
