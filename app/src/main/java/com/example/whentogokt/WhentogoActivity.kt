package com.example.whentogokt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class WhentogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whentogo)

        val intent = Intent(this, SelectActivity::class.java)
        var time_start : String = intent.getStringExtra("time_start")

        var startTime : TextView = findViewById(R.id.tv_time_start)

        startTime.text = time_start

        var startBtn : Button = findViewById(R.id.btn_start)

        startBtn.setOnClickListener(View.OnClickListener {
            val intentToStartPage = Intent(this, OnTheWayActivity::class.java)
            startActivity(intentToStartPage)
            finish()
        })


    }


}
