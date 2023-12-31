package com.sd.demo.timer

import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import com.sd.demo.timer.databinding.SampleDelayTaskBinding
import com.sd.lib.timer.FDelayTask

class SampleDelayTask : ComponentActivity() {
    private val _binding by lazy { SampleDelayTaskBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnStart.setOnClickListener {
            logMsg { "start" }
            _task.start(2_000)
        }
        _binding.btnPause.setOnClickListener {
            logMsg { "pause" }
            _task.pause()
        }
        _binding.btnResume.setOnClickListener {
            logMsg { "resume" }
            _task.resume()
        }
        _binding.btnCancel.setOnClickListener {
            logMsg { "cancel" }
            _task.cancel()
        }
    }

    private val _task = object : FDelayTask() {
        override fun onRun() {
            val isMain = Looper.getMainLooper() == Looper.myLooper()
            logMsg { "onRun $isMain" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _task.cancel()
    }
}