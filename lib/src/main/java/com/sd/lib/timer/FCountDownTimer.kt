package com.sd.lib.timer

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

/**
 * 倒计时类
 */
abstract class FCountDownTimer {
    /** 倒计时间隔 */
    private var _interval: Long = 1000

    /** 倒计时是否已经启动 */
    @Volatile
    private var _isStarted: Boolean = false

    /** 暂停的时间点 */
    @Volatile
    private var _pauseTime: Long? = null

    /** 结束的时间点 */
    private var _endTime: Long? = null

    /** 倒计时总时长 */
    private var _duration: Long = 0

    /**
     * 倒计时是否已经启动
     */
    fun isStarted(): Boolean = _isStarted

    /**
     * 倒计时是否被暂停
     */
    fun isPaused(): Boolean = _pauseTime != null

    /**
     * 设置倒计时间隔，默认1000毫秒
     */
    @Synchronized
    fun setInterval(interval: Long) {
        require(interval > 0)
        _interval = interval
    }

    /**
     * 开始倒计时
     * @param millis 总时长（毫秒）
     */
    @Synchronized
    fun start(millis: Long) {
        cancel()
        _isStarted = true
        _duration = millis.coerceAtLeast(0)
        _mainTimer.start(_duration, _interval)
    }

    /**
     * 暂停
     */
    @Synchronized
    fun pause() {
        if (!_isStarted) return
        if (_pauseTime == null) {
            // 记录暂停的时间点
            _pauseTime = SystemClock.elapsedRealtime()
            // 取消Timer
            _mainTimer.cancel()
        }
    }

    /**
     * 恢复
     */
    @Synchronized
    fun resume() {
        if (!_isStarted) return
        _pauseTime?.let { pauseTime ->
            _pauseTime = null

            val endTime = _endTime
            if (endTime == null) {
                /**
                 * 这种情况一般发生在子线程调用start()，接着又调用了pause() -> resume()，
                 * 此时Timer还未启动，直接启动即可
                 */
                _mainTimer.start(_duration, _interval)
            } else {
                // 当前时间点
                val currentTime = SystemClock.elapsedRealtime()
                // 计算暂停到恢复一共花了多少时间
                val pauseDuration = (currentTime - pauseTime).coerceAtLeast(0)
                // 重新计算结束时间点
                val newEndTime = (endTime + pauseDuration).also { _endTime = it }
                // 计算剩余时长
                val newDuration = newEndTime - currentTime
                // 启动Timer
                _mainTimer.start(newDuration, _interval)
            }
        }
    }

    /**
     * 取消倒计时
     */
    @Synchronized
    fun cancel() {
        _mainTimer.cancel()
        _pauseTime = null
        _endTime = null
        _duration = 0
        _isStarted = false
    }

    private val _mainTimer = object : MainTimer() {
        override fun onStart() {
            check(Looper.myLooper() === Looper.getMainLooper())
            synchronized(this@FCountDownTimer) {
                if (_endTime == null) {
                    // 第一次启动，记录一下结束的时间点
                    _endTime = SystemClock.elapsedRealtime() + _duration
                }
            }
        }

        override fun onTick(leftTime: Long) {
            check(Looper.myLooper() === Looper.getMainLooper())
            this@FCountDownTimer.onTick(leftTime)
        }

        override fun onFinish() {
            check(Looper.myLooper() === Looper.getMainLooper())
            this@FCountDownTimer.cancel()
            this@FCountDownTimer.onFinish()
        }
    }

    protected abstract fun onTick(leftTime: Long)
    protected abstract fun onFinish()
}

private abstract class MainTimer {
    private val _mainHandler = Handler(Looper.getMainLooper())
    private var _timer: CountDownTimer? = null

    private var _duration: Long? = null
    private var _interval: Long? = null

    fun start(duration: Long, interval: Long) {
        cancel()
        _duration = duration
        _interval = interval
        if (Looper.myLooper() === Looper.getMainLooper()) {
            _createRunnable.run()
        } else {
            _mainHandler.post(_createRunnable)
        }
    }

    private val _createRunnable = Runnable {
        check(Looper.myLooper() === Looper.getMainLooper())
        check(_timer == null)
        val duration = checkNotNull(_duration)
        val interval = checkNotNull(_interval)

        object : CountDownTimer(duration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                this@MainTimer.onTick(millisUntilFinished)
            }

            override fun onFinish() {
                this@MainTimer.onFinish()
            }
        }.let { timer ->
            _timer = timer
            onStart()
            timer.start()
        }
    }

    fun cancel() {
        _mainHandler.removeCallbacks(_createRunnable)
        _timer?.let {
            it.cancel()
            _timer = null
        }
        _duration = null
        _interval = null
    }

    protected abstract fun onStart()
    protected abstract fun onTick(leftTime: Long)
    protected abstract fun onFinish()
}