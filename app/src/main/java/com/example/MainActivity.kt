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

        // Pre-cache primary welcome and UI phrases for seamless E-ink response without lag
        val textsToPreCache = mutableListOf<String>().apply {
            add("欢迎来到宝宝识图乐园！点击大按钮开始玩耍吧！")
            add("欢迎来到宝宝识图乐园！点击下面的大按钮开始玩耍吧！")
            add("小朋友，请选择一个奇妙的世界开始探索吧！有动物园、大森林和海洋世界哦！")
            add("欢迎来到开心动物园！")
            add("欢迎来到奇妙大森林！")
            add("欢迎来到梦幻大海洋！")
            add("现在是学习宝典模式。点击画面中可爱的小家伙，学习他们的名字 and 有趣知识吧！")
            add("现在是学习宝典模式。点击画面中可爱的小家伙，学习他们的名字和有趣知识吧！")
            add("进入学习宝典模式！指一指画面上的小动物，听听他们有什么秘密吧！")
            add("进入找一找小游戏！准备好了吗？")

            // Add all animal-specific texts
            com.example.model.SceneData.zooAnimals.forEach { animal ->
                add("${animal.name}！拼写是：${animal.pinyin}。英文叫 ${animal.english}。${animal.funFact}")
                add("请帮我找一找：${animal.name} 在哪里呢？")
                add("请帮我找一找：${animal.name}在哪里呢？")
                add("哇！你太棒啦！成功找到了 ${animal.name}！奖励一颗亮闪闪的小星星！")
                add("在哪里呢？再听一次：请帮我找一找 ${animal.name}")
            }
            com.example.model.SceneData.forestAnimals.forEach { animal ->
                add("${animal.name}！拼写是：${animal.pinyin}。英文叫 ${animal.english}。${animal.funFact}")
                add("请帮我找一找：${animal.name} 在哪里呢？")
                add("请帮我找一找：${animal.name}在哪里呢？")
                add("哇！你太棒啦！成功找到了 ${animal.name}！奖励一颗亮闪闪的小星星！")
                add("在哪里呢？再听一次：请帮我找一找 ${animal.name}")
            }
            com.example.model.SceneData.oceanAnimals.forEach { animal ->
                add("${animal.name}！拼写是：${animal.pinyin}。英文叫 ${animal.english}。${animal.funFact}")
                add("请帮我找一找：${animal.name} 在哪里呢？")
                add("请帮我找一找：${animal.name}在哪里呢？")
                add("哇！你太棒啦！成功找到了 ${animal.name}！奖励一颗亮闪闪的小星星！")
                add("在哪里呢？再听一次：请帮我找一找 ${animal.name}")
            }
        }
        soundEngine.preCache(textsToPreCache)

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
