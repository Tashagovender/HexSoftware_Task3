package com.example.timenow

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val timezones = listOf(
        "Africa/Johannesburg", "Europe/London",
        "America/New_York", "Asia/Tokyo", "Australia/Sydney"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fromSpinner = findViewById<Spinner>(R.id.fromTimezoneSpinner)
        val toSpinner = findViewById<Spinner>(R.id.toTimezoneSpinner)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val convertButton = findViewById<Button>(R.id.convertButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        // Set 24-hour view programmatically
        timePicker.setIs24HourView(true)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timezones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter

        convertButton.setOnClickListener {
            val fromZone = fromSpinner.selectedItem.toString()
            val toZone = toSpinner.selectedItem.toString()

            val hour = timePicker.hour
            val minute = timePicker.minute

            val inputTime = String.format("%02d:%02d", hour, minute)
            convertTime(fromZone, toZone, inputTime, resultTextView)
        }
    }

    private fun convertTime(fromZone: String, toZone: String, inputTime: String, resultTextView: TextView) {
        val queue = Volley.newRequestQueue(this)

        val fromUrl = "https://worldtimeapi.org/api/timezone/$fromZone"
        val toUrl = "https://worldtimeapi.org/api/timezone/$toZone"

        val fromRequest = JsonObjectRequest(Request.Method.GET, fromUrl, null, { fromResponse ->
            val fromOffset = fromResponse.getInt("raw_offset")

            val toRequest = JsonObjectRequest(Request.Method.GET, toUrl, null, { toResponse ->
                val toOffset = toResponse.getInt("raw_offset")

                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")

                val inputDate = sdf.parse(inputTime)
                val newDate = Date(inputDate.time + (toOffset - fromOffset) * 1000L)
                val result = sdf.format(newDate)

                resultTextView.text = "Time in $toZone: $result"
            }, { error ->
                resultTextView.text = "Error fetching target zone: ${error.message}"
            })

            queue.add(toRequest)
        }, { error ->
            resultTextView.text = "Error fetching source zone: ${error.message}"
        })

        queue.add(fromRequest)
    }
}

