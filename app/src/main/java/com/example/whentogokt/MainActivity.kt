package com.example.whentogokt

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.odsay.odsayandroidsdk.API
import com.odsay.odsayandroidsdk.ODsayData
import com.odsay.odsayandroidsdk.ODsayService
import com.odsay.odsayandroidsdk.OnResultCallbackListener
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var sp_api: Spinner? = null
    private var rg_object_type: RadioGroup? = null
    private var rb_json: RadioButton? = null
    private var rb_map: RadioButton? = null
    private var bt_api_call: Button? = null
    private var tv_data: TextView? = null
    private var context: Context? = null
    private var spinnerSelectedName: String? = null
    private var odsayService: ODsayService? = null
    private var jsonObject: JSONObject? = null
    private var mapObject: Map<*, *>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        odsayService?.setReadTimeout(5000)  // 서버연결 제한시간
        odsayService?.setConnectionTimeout(5000) // 데이터획득 제한시간
    }

    private fun init() {
        context = this
        sp_api = findViewById(R.id.sp_api) as Spinner?
        rg_object_type = findViewById(R.id.rg_object_type) as RadioGroup?
        bt_api_call = findViewById(R.id.bt_api_call) as Button?
        rb_json = findViewById(R.id.rb_json) as RadioButton?
        rb_map = findViewById(R.id.rb_map) as RadioButton?
        tv_data = findViewById(R.id.tv_data) as TextView?
        sp_api!!.setSelection(0)

        odsayService = ODsayService.init(this@MainActivity, getString(R.string.odsay_key))

        bt_api_call!!.setOnClickListener(onClickListener)
        sp_api!!.onItemSelectedListener = onItemSelectedListener
        rg_object_type!!.setOnCheckedChangeListener(onCheckedChangeListener)

    }

    private val onCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { group,checkedId->
            if (rg_object_type!!.checkedRadioButtonId == rb_json!!.id) {
                if (jsonObject == null){
                    tv_data!!.text = "정보가없습니다."
                }

                tv_data!!.text = jsonObject.toString()
            } else if (rg_object_type!!.checkedRadioButtonId == rb_map!!.id) {
                if (mapObject == null){
                    tv_data!!.text = "정보가없습니다."
                }
                tv_data!!.text = mapObject.toString()
            }
        }
    private val onItemSelectedListener: OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View,
            position: Int, id: Long
        ) {
            spinnerSelectedName = parent.getItemAtPosition(position) as String
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    private val onResultCallbackListener: OnResultCallbackListener =
        object : OnResultCallbackListener {
            override fun onSuccess(oDsayData: ODsayData, api: API) {
                jsonObject = oDsayData.json
                mapObject = oDsayData.map

                if (rg_object_type!!.checkedRadioButtonId == rb_json!!.id) {
                    if (jsonObject == null){
                        tv_data!!.text = "정보가없습니다."
                    }

                    tv_data!!.text = jsonObject.toString()
                } else if (rg_object_type!!.checkedRadioButtonId == rb_map!!.id) {
                    if (mapObject == null){
                        tv_data!!.text = "정보가없습니다."
                    }
                    tv_data!!.text = mapObject.toString()
                }
            }

            override fun onError(
                i: Int,
                errorMessage: String,
                api: API
            ) {
                tv_data!!.text = "API : " + api.name + "\n" + errorMessage
            }
        }
    private val onClickListener =
        View.OnClickListener {
            when (spinnerSelectedName) {
                "버스 노선 조회" -> odsayService?.requestSearchBusLane(
                    "10",
                    "1000",
                    "no",
                    "10",
                    "1",
                    onResultCallbackListener
                )
                "버스노선 상세정보 조회" -> odsayService?.requestBusLaneDetail(
                    "12018",
                    onResultCallbackListener
                )
                "버스정류장 세부정보 조회" -> odsayService?.requestBusStationInfo(
                    "107475",
                    onResultCallbackListener
                )
                "열차•KTX 운행정보 검색" -> odsayService?.requestTrainServiceTime(
                    "3300128",
                    "3300108",
                    onResultCallbackListener
                )
                "고속버스 운행정보 검색" -> odsayService?.requestExpressServiceTime(
                    "4000057",
                    "4000030",
                    onResultCallbackListener
                )
                "시외버스 운행정보 검색" -> odsayService?.requestIntercityServiceTime(
                    "4000022",
                    "4000255",
                    onResultCallbackListener
                )
                "항공 운행정보 검색" -> odsayService?.requestAirServiceTime(
                    "3500001",
                    "3500003",
                    "6",
                    onResultCallbackListener
                )
                "운수회사별 버스노선 조회" -> odsayService?.requestSearchByCompany(
                    "792",
                    "100",
                    onResultCallbackListener
                )
                "지하철역 세부 정보 조회" -> odsayService?.requestSubwayStationInfo(
                    "130",
                    onResultCallbackListener
                )
                "지하철역 전체 시간표 조회" -> odsayService?.requestSubwayTimeTable(
                    "130",
                    "1",
                    onResultCallbackListener
                )
                "노선 그래픽 데이터 검색" -> odsayService?.requestLoadLane(
                    "0:0@12018:1:-1:-1",
                    onResultCallbackListener
                )
                "대중교통 정류장 검색" -> odsayService?.requestSearchStation(
                    "11",
                    "1000",
                    "1:2",
                    "10",
                    "1",
                    "127.0363583:37.5113295",
                    onResultCallbackListener
                )
                "반경내 대중교통 POI 검색" -> odsayService?.requestPointSearch(
                    "126.933361407195",
                    "37.3643392278118",
                    "250",
                    "1:2",
                    onResultCallbackListener
                )
                "지도 위 대중교통 POI 검색" -> odsayService?.requestBoundarySearch(
                    "127.045478316811:37.68882830829:127.055063420699:37.6370465749586",
                    "127.045478316811:37.68882830829:127.055063420699:37.6370465749586",
                    "1:2",
                    onResultCallbackListener
                )
                "지하철 경로검색 조회(지하철 노선도)" -> odsayService?.requestSubwayPath(
                    "1000",
                    "201",
                    "222",
                    "1",
                    onResultCallbackListener
                )
                "대중교통 길찾기" -> odsayService?.requestSearchPubTransPath(
                    "126.926493082645",
                    "37.6134436427887",
                    "127.126936754911",
                    "37.5004198786564",
                    "0",
                    "0",
                    "0",
                    onResultCallbackListener
                )
                "지하철역 환승 정보 조회" -> odsayService?.requestSubwayTransitInfo(
                    "133",
                    onResultCallbackListener
                )
                "고속버스 터미널 조회" -> odsayService?.requestExpressBusTerminals(
                    "1000",
                    "서울",
                    onResultCallbackListener
                )
                "시외버스 터미널 조회" -> odsayService?.requestIntercityBusTerminals(
                    "1000",
                    "서울",
                    onResultCallbackListener
                )
                "도시코드 조회" -> odsayService?.requestSearchCID("서울", onResultCallbackListener)
            }
        }
}