package com.example.whentogokt.ui.home

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
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.whentogokt.GpsTracker
import com.example.whentogokt.R
import java.io.IOException
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private var gpsTracker: GpsTracker? = null
    private var textView_Date: TextView? = null
    private var textView_Time: TextView? = null
    private var dateCallbackMethod: DatePickerDialog.OnDateSetListener? = null
    private var timeCallbackMethod: TimePickerDialog.OnTimeSetListener? = null

    private var webView: WebView? = null
    private var handler: Handler? = null


    var REQUIRED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })

        val dateButton : Button = root.findViewById(R.id.button_date)
        val timeButton : Button = root.findViewById(R.id.button_time)

        // GPS 정보 입력
        val textview_address = root.findViewById<View>(R.id.textview) as TextView
        val ShowLocationButton: Button = root.findViewById<View>(R.id.button) as Button

        // 위치서비스 체크
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            checkRunTimePermission()
        }

        ShowLocationButton.setOnClickListener(View.OnClickListener {
            gpsTracker = GpsTracker(this@HomeFragment)
            val latitude: Double = gpsTracker!!.getLatitude()
            val longitude: Double = gpsTracker!!.getLongitude()
            val address = getCurrentAddress(latitude, longitude)
            textview_address.text = address
        })

        // Timezone 설정
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        val gc = GregorianCalendar(tz)

        // 현재 날짜 입력
        var year = gc.get(GregorianCalendar.YEAR).toString()
        var month = gc.get(GregorianCalendar.MONTH).toString()
        var day = gc.get(GregorianCalendar.DATE).toString()

        InitializeDateView()
        InitializeDateListener()
        dateButton.setOnClickListener( View.OnClickListener {
            val dialog = DatePickerDialog(this, dateCallbackMethod, year.toInt(), month.toInt(), day.toInt())
            dialog.show()
        })

        var hour= gc.get(GregorianCalendar.HOUR).toString()
        var min = gc.get(GregorianCalendar.MINUTE).toString()

        // 현재 시간 입력
        InitializeTimeView()
        InitializeTimeListener()
        timeButton.setOnClickListener( View.OnClickListener {
            val dialog = TimePickerDialog(this, timeCallbackMethod, hour.toInt(),min.toInt(),true)
            dialog.show()
        })

        return root
    }

    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String>, grandResults: IntArray) {
        // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {
            var check_result = true
            // 모든 퍼미션을 허용했는지 체크
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            //위치 값을 가져올 수 있음
            if (check_result) { }
            // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료 (2 가지 경우)
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(
                        this@Main2Activity,
                        "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                else {
                    Toast.makeText(
                        this@Main2Activity,
                        "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    //런타임 퍼미션 처리
    fun checkRunTimePermission() {
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this@Main2Activity, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this@Main2Activity, Manifest.permission.ACCESS_COARSE_LOCATION)
        // 2. 이미 퍼미션을 가지고 있다면
        // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)
        // 위치 값을 가져올 수 있음
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { }
        //3. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
        else {
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@Main2Activity, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(
                    this@Main2Activity,
                    "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Toast.LENGTH_LONG).show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult 에서 수신
                ActivityCompat.requestPermissions(
                    this@Main2Activity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )}
            // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 함
            // 요청 결과는 onRequestPermissionResult에서 수신
            else {
                ActivityCompat.requestPermissions(
                    this@Main2Activity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    // GPS를 주소로 변환
    fun getCurrentAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>

        addresses = try {
            geocoder.getFromLocation(
                latitude,
                longitude,
                7
            )
        } catch (ioException: IOException) { //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show()
            return "주소 미발견"
        }
        val address: Address = addresses[0]

        return address.getAddressLine(0).toString().toString() + "\n"
    }

    // GPS 활성화를 위한 메소드
    private fun showDialogForLocationServiceSetting() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@Main2Activity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                    + "위치 설정을 수정하실래요?"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(
                callGPSSettingIntent,
                GPS_ENABLE_REQUEST_CODE
            )
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->  //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    companion object {
        private const val GPS_ENABLE_REQUEST_CODE = 2001
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    // Date Dialog Initialize
    fun InitializeDateView() {
        textView_Date = findViewById<View>(R.id.textview_date) as TextView
    }

    fun InitializeDateListener() {
        dateCallbackMethod =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                textView_Date!!.text = year.toString() + "년" + monthOfYear + "월" + dayOfMonth + "일"
            }
    }

    fun InitializeTimeView() {
        textView_Time = findViewById<View>(R.id.textview_time) as TextView
    }

    fun InitializeTimeListener() {
        timeCallbackMethod =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                textView_Time!!.text = hourOfDay.toString() + "시" + minute + "분"
            }
    }

    // Daum Webview
    fun init_webView() {
        webView = findViewById(R.id.wv_daum) as WebView?
        // JavaScript 허용
        webView!!.getSettings().setJavaScriptEnabled(true)
        // JavaScript의 window.open 허용
        webView!!.getSettings().setJavaScriptCanOpenWindowsAutomatically(true)
        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        webView!!.addJavascriptInterface(AndroidBridge(), "TestApp")
        // web client 를 chrome 으로 설정
        webView!!.setWebChromeClient(WebChromeClient())
        // webview url load. php 파일 주소
        //webView!!.loadUrl("http://10.0.75.1:8080/postcode.v2.html")
        webView!!.loadUrl("http://naver.com")
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun setAddress(arg1: String?, arg2: String?, arg3: String?) {
            handler!!.post(Runnable {
                // WebView를 초기화 하지않으면 재사용할 수 없음
                init_webView()

                val intent = Intent()
                intent.putExtra("address1", arg2)
                intent.putExtra("address_building", arg3)
                setResult(1, intent)
                finish()

            })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean { //바깥레이어 클릭시 안닫히게
        return if (event.action == MotionEvent.ACTION_OUTSIDE) {
            false
        } else true
    }

}