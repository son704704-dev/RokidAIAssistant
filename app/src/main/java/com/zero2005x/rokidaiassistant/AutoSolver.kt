package com.zero2005x.rokidaiassistant

import kotlinx.coroutines.*

class AutoSolver(private val onTrigger: () -> Unit) {
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.Main)

    fun toggle() {
        isRunning = !isRunning
        if (isRunning) {
            startLoop()
        }
    }

    private fun startLoop() {
        scope.launch {
            while (isRunning) {
                onTrigger()
                delay(10000) // 10초 대기
            }
        }
    }
}
