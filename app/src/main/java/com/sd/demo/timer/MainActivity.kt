package com.sd.demo.timer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.timer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnSampleCountDown.setOnClickListener {
            startActivity(Intent(this, SampleCountDown::class.java))
        }
        _binding.btnSampleDelayTask.setOnClickListener {
            startActivity(Intent(this, SampleDelayTask::class.java))
        }
    }
}

inline fun logMsg(block: () -> Any) {
    Log.i("timer-demo", block().toString())
}