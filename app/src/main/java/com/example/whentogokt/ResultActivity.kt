package com.example.whentogokt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val intent = Intent(this, Main3Activity::class.java)
        var result_json : String = intent.getStringExtra("result_json")
        val tv_json = findViewById<TextView>(R.id.tv_json) as TextView

        tv_json.text = result_json

    }
}
