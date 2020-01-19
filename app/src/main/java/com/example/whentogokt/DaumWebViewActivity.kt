package com.example.whentogokt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_daum.*


class DaumWebViewActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var handler: Handler? = null

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_daum)

        // WebView 초기화
        init_webView()
        // 핸들러를 통한 JavaScript 이벤트 반응
        handler = Handler()

    }

    fun init_webView() { // WebView 설정
        webView = findViewById(R.id.daum_webview) as WebView?
        // JavaScript 허용
        webView!!.getSettings().setJavaScriptEnabled(true)
        // JavaScript의 window.open 허용
        webView!!.getSettings().setJavaScriptCanOpenWindowsAutomatically(true)
        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        webView!!.addJavascriptInterface(AndroidBridge(), "TestApp")
        // web client 를 chrome 으로 설정
        webView!!.setWebChromeClient(WebChromeClient())
        // webview url load. php 파일 주소
        webView!!.loadUrl("http://180.68.35.60/static/daum.html")
    }

    fun sendResult(arg1: String?, arg2: String?, arg3: String?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("arg1",arg1)
        intent.putExtra("arg2",arg2)
        intent.putExtra("arg3",arg3)
        startActivity(intent)
        finish()
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun setAddress(arg1: String?, arg2: String?, arg3: String?) {
            handler!!.post(Runnable {
                // WebView를 초기화 하지않으면 재사용할 수 없음
                init_webView()
                sendResult(arg1, arg2, arg3)
            })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean { //바깥레이어 클릭시 안닫히게
        return if (event.action == MotionEvent.ACTION_OUTSIDE) {
            false } else true
    }
}