package com.example.whentogokt.Retrofit

import com.example.whentogokt.Model.APIResponseModel
import io.reactivex.Observable
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST


class OdsayAPI {

    interface OdsayAPIImpl {
        @POST("/odsay/getGpsdata/")
        fun getRepoList(@Body body: JSONObject): Observable<APIResponseModel>

    }

    companion object {
        fun getRepoList(body: JSONObject): Observable<APIResponseModel> {
            return RetrofitCreator.create(OdsayAPIImpl::class.java).getRepoList(body)
        }
    }


}