package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Animal
import com.example.model.GameMode
import com.example.model.SceneData
import com.example.model.SceneType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface LearningEvent {
    data class Speak(val text: String, val flush: Boolean = true) : LearningEvent
    object PlaySuccessSound : LearningEvent
    object PlayIncorrectSound : LearningEvent
    object PlayClickSound : LearningEvent
}

enum class ScreenType {
    WELCOME,
    SCENE_SELECT,
    PLAYGROUND
}

class LearningViewModel(
    private val initialStarCount: Int,
    private val saveStarCount: (Int) -> Unit
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ScreenType.WELCOME)
    val currentScreen: StateFlow<ScreenType> = _currentScreen.asStateFlow()

    private val _currentScene = MutableStateFlow<SceneType?>(null)
    val currentScene: StateFlow<SceneType?> = _currentScene.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.LEARNING)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _starCount = MutableStateFlow(initialStarCount)
    val starCount: StateFlow<Int> = _starCount.asStateFlow()

    private val _selectedAnimal = MutableStateFlow<Animal?>(null)
    val selectedAnimal: StateFlow<Animal?> = _selectedAnimal.asStateFlow()

    // Find-it Game States
    private val _targetAnimal = MutableStateFlow<Animal?>(null)
    val targetAnimal: StateFlow<Animal?> = _targetAnimal.asStateFlow()

    private val _wrongSelections = MutableStateFlow<Set<String>>(emptySet())
    val wrongSelections: StateFlow<Set<String>> = _wrongSelections.asStateFlow()

    private val _isCelebrationActive = MutableStateFlow(false)
    val isCelebrationActive: StateFlow<Boolean> = _isCelebrationActive.asStateFlow()

    private val _isMute = MutableStateFlow(false)
    val isMute: StateFlow<Boolean> = _isMute.asStateFlow()

    // Event output flow for Speech and Audio triggers
    private val _eventFlow = kotlinx.coroutines.flow.MutableSharedFlow<LearningEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val eventFlow = _eventFlow.asSharedFlow()

    fun triggerEvent(event: LearningEvent) {
        _eventFlow.tryEmit(event)
    }

    fun setMute(mute: Boolean) {
        _isMute.value = mute
        triggerEvent(LearningEvent.PlayClickSound)
    }

    fun navigateTo(screen: ScreenType) {
        _currentScreen.value = screen
        _selectedAnimal.value = null
        _isCelebrationActive.value = false
        triggerEvent(LearningEvent.PlayClickSound)

        if (screen == ScreenType.WELCOME) {
            triggerEvent(LearningEvent.Speak("欢迎来到宝宝识图乐园！点击下面的大按钮开始玩耍吧！"))
        } else if (screen == ScreenType.SCENE_SELECT) {
            triggerEvent(LearningEvent.Speak("小朋友，请选择一个奇妙的世界开始探索吧！有动物园、大森林和海洋世界哦！"))
        }
    }

    fun selectScene(scene: SceneType) {
        _currentScene.value = scene
        _currentScreen.value = ScreenType.PLAYGROUND
        _selectedAnimal.value = null
        _wrongSelections.value = emptySet()
        _isCelebrationActive.value = false
        triggerEvent(LearningEvent.PlayClickSound)

        // Initialize mode
        val sceneName = scene.displayName
        triggerEvent(LearningEvent.Speak("欢迎来到${sceneName}！"))

        if (_gameMode.value == GameMode.LEARNING) {
            triggerEvent(LearningEvent.Speak("现在是学习宝典模式。点击画面中可爱的小家伙，学习他们的名字和有趣知识吧！", flush = false))
        } else {
            startNewFindGame()
        }
    }

    fun setGameMode(mode: GameMode) {
        if (_gameMode.value == mode) return
        _gameMode.value = mode
        _selectedAnimal.value = null
        _wrongSelections.value = emptySet()
        _isCelebrationActive.value = false
        triggerEvent(LearningEvent.PlayClickSound)

        if (mode == GameMode.LEARNING) {
            triggerEvent(LearningEvent.Speak("进入学习宝典模式！指一指画面上的小动物，听听他们有什么秘密吧！"))
        } else {
            triggerEvent(LearningEvent.Speak("进入找一找小游戏！准备好了吗？"))
            startNewFindGame()
        }
    }

    fun startNewFindGame() {
        val scene = _currentScene.value ?: return
        val animals = SceneData.getAnimalsForScene(scene)
        if (animals.isEmpty()) return

        // Pick a random target animal
        val nextTarget = animals.random()
        _targetAnimal.value = nextTarget
        _wrongSelections.value = emptySet()
        _isCelebrationActive.value = false

        triggerEvent(LearningEvent.Speak("请帮我找一找：${nextTarget.name} 在哪里呢？", flush = false))
    }

    fun onAnimalClicked(animal: Animal) {
        if (_isCelebrationActive.value) return // Block quick spam clicks during stars flurry

        if (_gameMode.value == GameMode.LEARNING) {
            // Learning mode: Speak name, spelling, and interesting facts
            _selectedAnimal.value = animal
            triggerEvent(LearningEvent.PlayClickSound)
            val pinyinText = animal.pinyin.replace(" ", "")
            val speakText = "${animal.name}！拼写是：${animal.pinyin}。英文叫 ${animal.english}。${animal.funFact}"
            triggerEvent(LearningEvent.Speak(speakText))
        } else {
            // Find-it game mode
            val target = _targetAnimal.value ?: return
            if (animal.id == target.id) {
                // Correct!
                _isCelebrationActive.value = true
                // No points counting as requested by the user: "找一找模式不需要有积分"

                triggerEvent(LearningEvent.PlaySuccessSound)
                triggerEvent(LearningEvent.Speak("哇！你太棒啦！成功找到了 ${target.name}！"))

                // Automatically advance to a new find task after a nice celebration delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(4500)
                    if (_isCelebrationActive.value) { // Make sure we didn't exit scene during wait
                        _isCelebrationActive.value = false
                        startNewFindGame()
                    }
                }
            } else {
                // Incorrect
                if (animal.id !in _wrongSelections.value) {
                    _wrongSelections.value = _wrongSelections.value + animal.id
                }
                triggerEvent(LearningEvent.PlayIncorrectSound)
                val responseText = "不对哦，那是 ${animal.name}。再仔细找一找：${target.name} 藏在哪里啦？"
                triggerEvent(LearningEvent.Speak(responseText))
            }
        }
    }

    fun clearSelectedAnimal() {
        _selectedAnimal.value = null
        triggerEvent(LearningEvent.PlayClickSound)
    }

    fun triggerTargetRepronounce() {
        val target = _targetAnimal.value ?: return
        triggerEvent(LearningEvent.PlayClickSound)
        triggerEvent(LearningEvent.Speak("在哪里呢？再听一次：请帮我找一找 ${target.name}"))
    }

    init {
        // Welcome greet
        triggerEvent(LearningEvent.Speak("欢迎来到宝宝识图乐园！点击大按钮开始玩耍吧！"))
    }
}
