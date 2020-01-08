package com.example.whentogokt

import android.os.Bundle
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SearchAddress : AppCompatActivity() {
    private var webView: WebView? = null
    private var txt_address: TextView? = null
    private var handler: Handler? = null

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        txt_address = findViewById(R.id.txt_address)
        // WebView 초기화
        init_webView()
        // 핸들러를 통한 JavaScript 이벤트 반응
        handler = Handler()
    }

    fun init_webView() { // WebView 설정
        webView = findViewById(R.id.webView_address) as WebView?
        // JavaScript 허용
        webView!!.getSettings().setJavaScriptEnabled(true)
        // JavaScript의 window.open 허용
        webView!!.getSettings().setJavaScriptCanOpenWindowsAutomatically(true)
        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        webView!!.addJavascriptInterface(AndroidBridge(), "TestApp")
        // web client 를 chrome 으로 설정
        webView!!.setWebChromeClient(WebChromeClient())
        // webview url load. php 파일 주소
        webView!!.loadUrl("http://http://10.0.75.1:8080/daumwebview.html")
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun setAddress(
            arg1: String?,
            arg2: String?,
            arg3: String?
        ) {
            handler!!.post(Runnable {
                txt_address!!.setText(String.format("(%s) %s %s", arg1, arg2, arg3))
                // WebView를 초기화 하지않으면 재사용할 수 없음
                init_webView()
            })
        }
    }
}