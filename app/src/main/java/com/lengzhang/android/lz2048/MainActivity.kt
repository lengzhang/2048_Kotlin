package com.lengzhang.android.lz2048

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lengzhang.android.lz2048.database.GameStatus

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val gameViewModel: GameViewModel by lazy {
        ViewModelProvider(this).get(GameViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GameRepository.initialize(this)

        // Tells Android that the app will be a full-screen app
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // Indicates that should not be a title bar for the app
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)

        this.findViewById<Header>(R.id.header).attachGameViewModel(gameViewModel, this)
        this.findViewById<FrameLayout>(R.id.game_view_container)
            .addView(GameView(this, gameViewModel = gameViewModel))

        // Watch Games
        gameViewModel.games.observe(this) {
            Log.d(TAG, it.toString())
            val recentGame = if (it.isNotEmpty()) it[0] else null
            Log.d(TAG, recentGame.toString())
            gameViewModel.setBestScore(it)
            if (gameViewModel.currentGame.value == null) {
                if (recentGame == null || recentGame.status != GameStatus.PLAYING) {
                    gameViewModel.newGame()
                } else {
                    gameViewModel.loadGame(recentGame)
                }
            }
        }
    }
}