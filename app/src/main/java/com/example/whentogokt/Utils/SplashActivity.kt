package com.example.whentogokt.Utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import com.example.whentogokt.Login.EmailPasswordActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SystemClock.sleep(300)
        val intent = Intent(this, EmailPasswordActivity::class.java)
        startActivity(intent)
        finish()
    }
}
