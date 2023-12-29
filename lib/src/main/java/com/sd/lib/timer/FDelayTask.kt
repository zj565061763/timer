package com.sd.lib.timer

abstract class FDelayTask {
    /**
     * 是否已经启动
     */
    fun isStarted(): Boolean = _timer.isStarted()

    /**
     * 延迟[delay]毫秒后执行
     */
    fun start(delay: Long) {
        require(delay >= 0) { "require delay >= 0" }
        synchronized(_timer.lock) {
            _timer.setInterval(delay)
            _timer.start(delay)
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        _timer.pause()
    }

    /**
     * 恢复
     */
    fun resume() {
        _timer.resume()
    }

    /**
     * 取消执行
     */
    fun cancel() {
        _timer.cancel()
    }

    private val _timer = object : FCountDownTimer() {
        override fun onTick(leftTime: Long) {
        }

        override fun onFinish() {
            onRun()
        }
    }

    protected abstract fun onRun()
}