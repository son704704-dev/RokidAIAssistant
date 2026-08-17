package com.example.rokidphone

import kotlinx.coroutines.*

class AutoSolveManager(
    private val captureAndAnalyzeAction: suspend (String) -> Unit
) {
    private var autoJob: Job? = null
    var isRunning = false
        private set

    fun toggleAutoLoop() {
        if (isRunning) {
            stopAutoLoop()
        } else {
            startAutoLoop()
        }
    }

    private fun startAutoLoop() {
        isRunning = true
        autoJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && isRunning) {
                val problemPrompt = "이 사진은 시험 문제 또는 학업 문제지이다. 다른 설명은 절대 하지 말고, 오직 정답과 핵심 풀이 과정만 글래스 화면에 맞게 아주 짧고 간결하게 출력하라."
                try {
                    captureAndAnalyzeAction(problemPrompt)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000) // 10초 간격 대기
            }
        }
    }

    fun stopAutoLoop() {
        isRunning = false
        autoJob?.cancel()
        autoJob = null
    }
}
