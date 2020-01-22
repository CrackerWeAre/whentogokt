package com.example.whentogokt

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.whentogokt.Retrofit.OdsayAPI
import com.example.whentogokt.Model.APIResponseModel
import com.example.whentogokt.Utils.GpsTracker
import com.example.whentogokt.Utils.RecyclerAdapter
import com.example.whentogokt.Utils.ResultItem
import com.odsay.odsayandroidsdk.API
import com.odsay.odsayandroidsdk.ODsayData
import com.odsay.odsayandroidsdk.OnResultCallbackListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_select.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class HomeActivity : AppCompatActivity() {

    private var textView_Time: TextView? = null
    private var timeCallbackMethod: TimePickerDialog.OnTimeSetListener? = null
    private var destination: String? = null
    private var arrivaltime: String? = null
    private var desLatitude: String? = null
    private var desLongitude: String? = null

    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val timeButton: Button = findViewById(R.id.btn_time)

        // 주소 정보 입력
        val ShowLocationButton: Button = findViewById<View>(R.id.btn_gps) as Button

        // Daum 도로명주소 api
        ShowLocationButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, DaumWebViewActivity::class.java)
            startActivityForResult(intent, 1)
        })

        // Timezone 설정
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        val gc = GregorianCalendar(tz)

        // 도착 시간 입력
        InitializeTimeView()
        InitializeTimeListener()
        timeButton.setOnClickListener(View.OnClickListener {
            // 현재 시간
            var hour = gc.get(GregorianCalendar.HOUR).toString()
            var min = gc.get(GregorianCalendar.MINUTE).toString()
            val dialog = TimePickerDialog(this, timeCallbackMethod, hour.toInt(), min.toInt(), true)
            dialog.show()
        })

        // 검색 버튼 클릭
        var button_click: Button = findViewById(R.id.btn_search)

        // API 호출 관련 Initialization
        compositeDisposable = CompositeDisposable()

        button_click.setOnClickListener(View.OnClickListener {

            // Log 전송
            var jsonParam = JSONObject()

            jsonParam.put("arrivaltime", arrivaltime)
            jsonParam.put("destination", destination)
            jsonParam.put("userinfo", Build.ID)
            jsonParam.put("lognum", 1)

            compositeDisposable.add(
                OdsayAPI.sendLog(jsonParam)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe({ response: String ->
                        println(response)
                    }, { error: Throwable ->
                            Log.d("HomeActivity", error?.localizedMessage)
                            Toast.makeText(
                                this,
                                "Error ${error.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }))
            getDestinationGps(destination!!)

            val intent = Intent(this, SelectActivity::class.java)
            intent.putExtra("desLatitude", desLatitude)
            intent.putExtra("desLongitude", desLongitude)
            startActivity(intent)
            finish()
        })
    }

    // GPS를 주소로 변환
    fun getDestinationGps(address: String): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val gpsList: List<Address>

        gpsList = try {
            geocoder.getFromLocationName(
                address,
                1
            )
        } catch (ioException: IOException) { //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 도로명 주소", Toast.LENGTH_LONG).show()
            return "잘못된 도로명 주소"
        }
        if (gpsList == null || gpsList.size == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show()
            return "GPS 미발견"
        }
        val gps: Address = gpsList[0]

        desLatitude = gps.latitude.toString()
        desLongitude = gps.longitude.toString()

        return ""

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            RESULT_OK ->  // 정상 반환일 경우에만 동작하겠다
                setAddress(data)
        }
    }

    fun setAddress(intent:Intent?) {
        var arg1: String? = intent?.getStringExtra("arg1")
        var arg2: String? = intent?.getStringExtra("arg2")
        var arg3: String? = intent?.getStringExtra("arg3")

        destination = String.format("(%s) %s %s", arg1, arg2, arg3)

    }

    fun InitializeTimeView() {
        textView_Time = findViewById<View>(R.id.tv_time) as TextView
    }

    fun InitializeTimeListener() {
        timeCallbackMethod =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                arrivaltime = hourOfDay.toString() + ":" + minute
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
