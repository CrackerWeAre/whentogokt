package com.example.whentogokt.Model

import com.google.gson.annotations.SerializedName

class APIRepoModel {
    @SerializedName("totaltime")
    val totaltime: Int = 0

    @SerializedName("subpath")
    val subPath: List<SubPathModel> = listOf()
}