package com.zero2005x.rokidaiassistant

import kotlinx.coroutines.*
import android.os.Handler
import android.os.Looper

// 기존 프로젝트의 기능을 그대로 가져다 쓰는 자동화 클래스
class AutoSolver(private val mainActivity: MainActivity) {
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
                // 기존 프로젝트의 촬영 함수 호출 (MainActivity 내의 함수 접근)
                mainActivity.takePictureAndAnalyze() 
                delay(10000) // 10초 대기
            }
        }
    }
}
