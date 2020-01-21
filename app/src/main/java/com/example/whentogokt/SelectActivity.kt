package com.example.whentogokt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.whentogokt.Model.APIResponseModel
import com.example.whentogokt.Retrofit.OdsayAPI
import com.example.whentogokt.Utils.RecyclerAdapter
import com.example.whentogokt.Utils.ResultItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_select.*
import org.json.JSONObject
import java.io.Serializable
import java.util.ArrayList

class SelectActivity : AppCompatActivity() {

    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
    }

    override fun onStart() {
        super.onStart()

        // API 호출 관련 Initialization
        compositeDisposable = CompositeDisposable()

        //모든 정보가 유효한지 확인
        //1. 시작지점
        //2. 도착지점
        //3. 출발시간
        //4. 도착시간
        //5. 이동수단(지하철)

        // 모든 정보가 유효하다면
        // api 호출

        // 임시 파라미터
        var sx = 127.03884584921066
        var sy = 37.56460329171426
        var ex = 127.05675385527476
        var ey = 37.50724735841729
        var opt = 0
        var SearchType = 0
        var SearchPathType = 0

        val jsonParam = JSONObject()

        jsonParam.put("sx", sx.toString())
        jsonParam.put("sy", sy.toString())
        jsonParam.put("ex", ex.toString())
        jsonParam.put("ey", ey.toString())
        jsonParam.put("opt", opt.toString())
        jsonParam.put("SearchType", SearchType.toString())
        jsonParam.put("SearchPathType", SearchPathType.toString())

        compositeDisposable.add(
            OdsayAPI.getRepoList(jsonParam)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({ response: APIResponseModel ->
                    val list = ArrayList<ResultItem>()
                    for (item in response.resultList) {
                        println(item.totaltime)
                        list.clear()
                        for (i in item.subPath) {
                            if ("지하철" in i.name) {
                                list.add(
                                    ResultItem(
                                        getDrawable(R.drawable.subway)!!,
                                        i.name,
                                        i.sectionTime,
                                        i.startName,
                                        i.endName
                                    )
                                )
                            } else if ("걷기" in i.name) {
                                list.add(
                                    ResultItem(
                                        getDrawable(R.drawable.walk)!!,
                                        i.name,
                                        i.sectionTime,
                                        i.startName,
                                        i.endName
                                    )
                                )
                            } else if ("버스" in i.name) {
                                list.add(
                                    ResultItem(
                                        getDrawable(R.drawable.bus)!!,
                                        i.name,
                                        i.sectionTime,
                                        i.startName,
                                        i.endName
                                    )
                                )
                            }
                        }
                    }

                    val adapter = RecyclerAdapter(list)
                    recyclerView.adapter = adapter

                    recyclerView.addItemDecoration(
                        DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
                    )

                }, { error: Throwable ->
                    Log.d("MainActivity", error?.localizedMessage)
                    Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT)
                        .show()
                })
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


}

