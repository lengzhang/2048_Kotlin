package com.lengzhang.android.lz2048

import android.graphics.Canvas
import android.view.SurfaceHolder

private const val TAG = "GameThread"

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: GameView
) : Thread() {
    private var running = false
    private var targetFPS = 60L

    override fun run() {
        var startTime = 0L
        val targetTime = 1000L / targetFPS

        while (running) {
            var canvas: Canvas? = null

            if (gameView.isDrawing) {
                try {
                    startTime = System.nanoTime()
                    canvas = surfaceHolder.lockCanvas()
                    gameView.draw(canvas)
                } catch (e: Exception) {
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            val timeMillis = (System.nanoTime() - startTime) / 1000000L
            val waitTime = targetTime - timeMillis
            if (waitTime > 0L) {
                try {
                    sleep(waitTime)
                } catch (e: Exception) {
                }
            }
        }
    }

    fun setRunning(isRunning: Boolean) {
        running = isRunning
    }
}