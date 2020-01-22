package com.example.whentogokt

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.whentogokt.Model.APIResponseModel
import com.example.whentogokt.Retrofit.OdsayAPI
import com.example.whentogokt.Utils.GpsTracker
import com.example.whentogokt.Utils.RecyclerAdapter
import com.example.whentogokt.Utils.ResultItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_select.*
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable
import java.util.*

class SelectActivity : AppCompatActivity() {

    lateinit var compositeDisposable: CompositeDisposable

    private var gpsTracker: GpsTracker? = null

    var curruntLatitude: Double = 0.0
    var currunLongitude: Double = 0.0

    var REQUIRED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        // 현재 GPS
        gpsTracker = GpsTracker(this@SelectActivity)
        curruntLatitude = gpsTracker!!.getLatitude()
        currunLongitude = gpsTracker!!.getLongitude()
        // 현재 주소
        // val address = getCurrentAddress(curruntLatitude, currunLongitude)

        // 위치서비스 체크
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            checkRunTimePermission()
        }

        val intent = Intent(this, HomeActivity::class.java)


    }

    override fun onStart() {
        super.onStart()

        // API 호출 관련 Initialization
        compositeDisposable = CompositeDisposable()

        // TODO: 모든 정보가 유효한지 확인
        //1. 시작지점
        //2. 도착지점
        //3. 출발시간
        //4. 도착시간
        //5. 이동수단(지하철)

        // 모든 정보가 유효하다면
        // api 호출

        var desLatitude : String= intent.getStringExtra("desLatitude")
        var desLongitude : String= intent.getStringExtra("desLongitude")

        var opt = 0
        var SearchType = 0
        var SearchPathType = 0

        val jsonParam = JSONObject()

        //jsonParam.put("sx", curruntLatitude)
        //jsonParam.put("sy", currunLongitude)
        jsonParam.put("sx", "127.03910567579072")
        jsonParam.put("sy", "37.563639139935894")
        jsonParam.put("ex", desLongitude)
        jsonParam.put("ey", desLatitude)

        jsonParam.put("opt", opt.toString())
        jsonParam.put("SearchType", SearchType.toString())
        jsonParam.put("SearchPathType", SearchPathType.toString())

        compositeDisposable.add(
            OdsayAPI.getRepoList(jsonParam)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({ response: APIResponseModel ->
                    // TODO : 시간 내에 도착하지 못할 때 noresult
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
                    Log.d("SelectActivity", error?.localizedMessage)
                    Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT)
                        .show()
                })
        )

    }

    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String>, grandResults: IntArray) {
        // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
        if (permsRequestCode == SelectActivity.PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {
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
                        this@SelectActivity,
                        "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                else {
                    Toast.makeText(
                        this@SelectActivity,
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
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this@SelectActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this@SelectActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
        // 2. 이미 퍼미션을 가지고 있다면
        // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)
        // 위치 값을 가져올 수 있음
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { }
        //3. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
        else {
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@SelectActivity, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(
                    this@SelectActivity,
                    "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Toast.LENGTH_LONG).show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult 에서 수신
                ActivityCompat.requestPermissions(
                    this@SelectActivity, REQUIRED_PERMISSIONS,
                    SelectActivity.PERMISSIONS_REQUEST_CODE
                )}
            // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 함
            // 요청 결과는 onRequestPermissionResult에서 수신
            else {
                ActivityCompat.requestPermissions(
                    this@SelectActivity, REQUIRED_PERMISSIONS,
                    SelectActivity.PERMISSIONS_REQUEST_CODE
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@SelectActivity)
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
                SelectActivity.GPS_ENABLE_REQUEST_CODE
            )
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SelectActivity.GPS_ENABLE_REQUEST_CODE ->  //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    companion object {
        private const val GPS_ENABLE_REQUEST_CODE = 2001
        private const val PERMISSIONS_REQUEST_CODE = 100
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}

