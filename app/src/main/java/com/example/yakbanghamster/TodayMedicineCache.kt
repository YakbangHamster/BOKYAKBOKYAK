package com.example.yakbanghamster

import com.example.yakbanghamster.data.MedicineTodayData

object TodayMedicineCache {
    var medicines: List<MedicineTodayData>? = null
    val imageCache: MutableMap<String, String?> = mutableMapOf()
}
