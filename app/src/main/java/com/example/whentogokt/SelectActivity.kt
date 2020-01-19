package com.example.whentogokt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, HomeActivity::class.java)
        var result_json : String = intent.getStringExtra("resultList")

        var tv_result : TextView = findViewById(R.id.tv_result)
        tv_result.text = result_json



    }
}
