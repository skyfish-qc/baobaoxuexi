package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.engine.KidsSoundEngine
import com.example.ui.screens.PlaygroundScreen
import com.example.ui.screens.SceneSelectScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LearningEvent
import com.example.viewmodel.LearningViewModel
import com.example.viewmodel.LearningViewModelFactory
import com.example.viewmodel.ScreenType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var soundEngine: KidsSoundEngine
    private lateinit var viewModel: LearningViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Persistent star rewards initialization via SharedPreferences
        val prefs = getSharedPreferences("BabyPictureParadisePrefs", Context.MODE_PRIVATE)
        val initialStarCount = prefs.getInt("star_count", 0)
        val saveStarCount = { count: Int ->
            prefs.edit().putInt("star_count", count).apply()
        }

        // Initialize sound logic
        soundEngine = KidsSoundEngine(this.applicationContext)

        // Instantiate state controller with Factory
        val factory = LearningViewModelFactory(initialStarCount, saveStarCount)
        viewModel = ViewModelProvider(this, factory)[LearningViewModel::class.java]

        // Start listening to the TTS/Synth event flow
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is LearningEvent.Speak -> {
                        soundEngine.speak(event.text, event.flush)
                    }
                    is LearningEvent.PlaySuccessSound -> {
                        soundEngine.playSuccess()
                    }
                    is LearningEvent.PlayIncorrectSound -> {
                        soundEngine.playIncorrect()
                    }
                    is LearningEvent.PlayClickSound -> {
                        soundEngine.playClick()
                    }
                }
            }
        }

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val isMute by viewModel.isMute.collectAsState()

                // Keep model state synced to the engine block
                soundEngine.isMute = isMute

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            ScreenType.WELCOME -> {
                                WelcomeScreen(viewModel = viewModel)
                            }
                            ScreenType.SCENE_SELECT -> {
                                SceneSelectScreen(viewModel = viewModel)
                            }
                            ScreenType.PLAYGROUND -> {
                                PlaygroundScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundEngine.shutdown()
    }
}
