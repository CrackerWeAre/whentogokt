package com.example.whentogokt

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DatePickerDialog : AppCompatActivity() {
    private var textView_Date: TextView? = null
    private var callbackMethod: OnDateSetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        InitializeView()
        InitializeListener()
    }

    fun InitializeView() {
        textView_Date = findViewById<View>(R.id.textView_date) as TextView
    }

    fun InitializeListener() {
        callbackMethod =
            OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                textView_Date!!.text = year.toString() + "년" + monthOfYear + "월" + dayOfMonth + "일"
            }
    }

    fun OnClickHandler(view: View) {
        val dialog = DatePickerDialog(this, callbackMethod, 2019, 5, 24)
        dialog.show()
    }
}