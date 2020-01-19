package com.example.whentogokt.Model

import com.google.gson.annotations.SerializedName

class APIResponseModel {
    @SerializedName("result")
    val resultList: List<APIRepoModel> = listOf()
}