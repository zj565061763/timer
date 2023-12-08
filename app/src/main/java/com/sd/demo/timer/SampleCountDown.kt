package com.sd.demo.timer

import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import com.sd.demo.timer.databinding.SampleCountDownBinding
import com.sd.lib.timer.FCountDownTimer

class SampleCountDown : ComponentActivity() {
    private val _binding by lazy { SampleCountDownBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnStart.setOnClickListener {
            _timer.start(10_000)
        }
        _binding.btnPause.setOnClickListener {
            _timer.pause()
        }
        _binding.btnResume.setOnClickListener {
            _timer.resume()
        }
        _binding.btnCancel.setOnClickListener {
            _timer.cancel()
        }
    }

    private val _timer = object : FCountDownTimer() {
        override fun onTick(leftTime: Long) {
            val isMain = Looper.getMainLooper() == Looper.myLooper()
            logMsg { "onTick ${leftTime / 1000} $leftTime $isMain" }
        }

        override fun onFinish() {
            val isMain = Looper.getMainLooper() == Looper.myLooper()
            logMsg { "onFinish $isMain" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _timer.cancel()
    }
}