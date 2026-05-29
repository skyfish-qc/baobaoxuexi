package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.math.sin

class KidsSoundEngine(private val context: Context) {
    private val soundScope = CoroutineScope(Dispatchers.IO)
    private var currentMediaPlayer: MediaPlayer? = null
    var isMute = false

    private val activePlayers = java.util.Collections.synchronizedSet(java.util.HashSet<MediaPlayer>())

    private fun safeRelease(player: MediaPlayer?) {
        if (player == null) return
        val removed = activePlayers.remove(player)
        if (removed) {
            try {
                player.release()
            } catch (e: Throwable) {
                Log.e("KidsSoundEngine", "Error releasing player: ${e.message}")
            }
        }
    }

    private val cacheDir = File(context.cacheDir, "audio_cache").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    // Map of normalized sentence -> raw resource name
    private val offlineSoundResources: Map<String, String> by lazy {
        val map = java.util.HashMap<String, String>()
        
        fun add(id: String, text: String) {
            val normal = text.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "").lowercase()
            map[normal] = id
        }

        // Add System / UI Phrases
        add("welcome_btn", "欢迎来到宝宝识图乐园！点击大按钮开始玩耍吧！")
        add("welcome_btn_below", "欢迎来到宝宝识图乐园！点击下面的大按钮开始玩耍吧！")
        add("scene_select_intro", "小朋友，请选择一个奇妙的世界开始探索吧！有动物园、大森林和海洋世界哦！")
        add("zoo_enter", "欢迎来到开心动物园！")
        add("forest_enter", "欢迎来到奇妙大森林！")
        add("ocean_enter", "欢迎来到梦幻大海洋！")
        add("paw_patrol_enter", "欢迎来到汪汪队基地！")
        add("mode_learning_intro", "现在是学习宝典模式。点击画面中可爱的小家伙，学习他们的名字 and 有趣知识吧！")
        add("mode_learning_action", "进入学习宝典模式！指一指画面上的小动物，听听他们有什么秘密吧！")
        add("mode_find_action", "进入找一找小游戏！准备好了吗？")

        // Add animals: id, name, pinyin, english, funFact
        val animals = listOf(
            listOf("panda", "熊猫", "xióng māo", "Panda", "大熊猫最喜欢吃香甜脆爽的竹子，胖嘟嘟的超级可爱哦！"),
            listOf("elephant", "大象", "dà xiàng", "Elephant", "大象有一根神奇的长鼻子，能吸水给自己洗澡喷泉呢！"),
            listOf("lion", "狮子", "shī zi", "Lion", "狮子是森林草原之王，他笑起来嗷呜一声，超级威风！"),
            listOf("monkey", "猴子", "hóu zi", "Monkey", "小猴子是个攀爬小能手，最喜欢在树上荡秋千吃香蕉！"),
            listOf("giraffe", "长颈鹿", "cháng jǐng lù", "Giraffe", "长颈鹿的脖子特别特别长，能轻松吃到大树顶上最嫩的绿叶！"),
            listOf("bear", "大熊", "dà xióng", "Bear", "大熊毛茸茸的，冬天会躲在温暖的树洞里睡一个大懒觉！"),
            listOf("rabbit", "兔子", "tù zi", "Rabbit", "小兔子长着长长的耳朵、红红的眼睛，最爱吃甜甜的胡萝卜！"),
            listOf("squirrel", "松鼠", "sōng shǔ", "Squirrel", "小松鼠有一条蓬松的大尾巴，喜欢到各处收集好吃的松果！"),
            listOf("owl", "猫头鹰", "māo tóu yīng", "Owl", "猫头鹰是大森林的夜间卫士，在黑夜里眼睛会闪闪发光！"),
            listOf("deer", "小鹿", "xiǎo lù", "Deer", "小鹿身上有美丽的斑点，跑起步来轻悄悄，又快又优美！"),
            listOf("dolphin", "海豚", "hǎi tún", "Dolphin", "小海豚最喜欢在海浪里跳舞，是海洋里最爱笑的小天使！"),
            listOf("whale", "鲸鱼", "jīng yú", "Whale", "大蓝鲸是海洋里最大的巨无霸，头顶会喷出好高的小喷泉！"),
            listOf("octopus", "章鱼", "zhāng yú", "Octopus", "章鱼小八有八只长长的小脚丫，生气的时候能喷出黑黑的墨汁！"),
            listOf("starfish", "海星", "hǎi xīng", "Starfish", "亮晶晶的海星就像是掉落在神秘海底夜空里的小星星！"),
            listOf("turtle", "海龟", "hǎi guī", "Turtle", "海龟背着厚厚重重的安全壳，在大海里划水游泳，像飞翔一样！"),
            listOf("ryder", "莱德队长", "lǎi dé duì zhǎng", "Ryder", "莱德队长是汪汪队的帅气队长，他精通各种高科技，最著名的口号是：没有困难的工作，只有勇敢的狗狗！"),
            listOf("chase", "阿奇", "ā qí", "Chase", "阿奇是一只特别神气的警犬牧羊犬，不仅跑得快，听力嗅觉也超级厉害，时刻守护着大家的平安！"),
            listOf("marshall", "毛毛", "máo máo", "Marshall", "毛毛是一只超级可爱的斑点消防犬，虽然经常笨手笨脚摔跟头，但救援和救火时总是最勇敢的！"),
            listOf("skye", "天天", "tiān tiān", "Skye", "天天是一只甜美活泼的飞行犬，她戴着粉色飞行头盔，最喜欢在蔚蓝的蓝天中驾驶直升机自由飞翔！"),
            listOf("rubble", "小砾", "xiǎo lì", "Rubble", "小砾是一只力气很大的斗牛犬工程犬，驾驶着帅气的黄色挖掘机，最喜欢吃香甜的零食 and 滑滑板！")
        )

        for (a in animals) {
            val id = a[0]
            val name = a[1]
            val pinyin = a[2]
            val english = a[3]
            val funFact = a[4]

            add("${id}_fact", "${name}！拼写是：${pinyin}。英文叫 ${english}。${funFact}")
            add("${id}_find", "请帮我找一找：${name}在哪里呢？")
            add("${id}_found", "哇！你太棒啦！成功找到了 ${name}！奖励一颗亮闪闪的小星星！")
            add("${id}_incorrect", "不对哦，那是${name}。")
            add("${id}_search_suffix", "再仔细找一找：${name}藏在哪里啦？")
            add("${id}_repronounce", "在哪里呢？再听一次：请帮我找一找${name}")
        }
        
        map
    }

    private fun playSequentialResources(resId1: Int, resId2: Int) {
        soundScope.launch {
            synchronized(this@KidsSoundEngine) {
                stopPlaybackInternal()
                var player1: MediaPlayer? = null
                try {
                    player1 = MediaPlayer.create(context, resId1)
                    if (player1 != null) {
                        activePlayers.add(player1)
                        currentMediaPlayer = player1
                        player1.setOnCompletionListener {
                            synchronized(this@KidsSoundEngine) {
                                if (currentMediaPlayer == player1) {
                                    currentMediaPlayer = null
                                }
                            }
                            safeRelease(player1)

                            soundScope.launch {
                                synchronized(this@KidsSoundEngine) {
                                    if (currentMediaPlayer == null) {
                                        var player2: MediaPlayer? = null
                                        try {
                                            player2 = MediaPlayer.create(context, resId2)
                                            if (player2 != null) {
                                                activePlayers.add(player2)
                                                currentMediaPlayer = player2
                                                player2.setOnCompletionListener {
                                                    synchronized(this@KidsSoundEngine) {
                                                        if (currentMediaPlayer == player2) {
                                                            currentMediaPlayer = null
                                                        }
                                                    }
                                                    safeRelease(player2)
                                                }
                                                player2.start()
                                            } else {
                                                Log.e("KidsSoundEngine", "player2 is null for resId2")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("KidsSoundEngine", "Error playing second sequence: ${e.message}")
                                            safeRelease(player2)
                                        }
                                    }
                                }
                            }
                        }
                        player1.start()
                    } else {
                        Log.e("KidsSoundEngine", "player1 is null for resId1")
                    }
                } catch (e: Exception) {
                    Log.e("KidsSoundEngine", "Error playing first sequence: ${e.message}")
                    safeRelease(player1)
                }
            }
        }
    }

    private fun playRawResource(resName: String, fbToneFreq: Double, fbToneDuration: Int) {
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (resId != 0) {
            soundScope.launch {
                synchronized(this@KidsSoundEngine) {
                    stopPlaybackInternal()
                    var player: MediaPlayer? = null
                    try {
                        player = MediaPlayer.create(context, resId)
                        if (player != null) {
                            activePlayers.add(player)
                            currentMediaPlayer = player
                            player.setOnCompletionListener {
                                synchronized(this@KidsSoundEngine) {
                                    if (currentMediaPlayer == player) {
                                        currentMediaPlayer = null
                                    }
                                }
                                safeRelease(player)
                            }
                            player.start()
                        } else {
                            Log.e("KidsSoundEngine", "player is null in playRawResource for $resName")
                            playTone(fbToneFreq, fbToneDuration)
                        }
                    } catch (e: Exception) {
                        Log.e("KidsSoundEngine", "Error playing raw effect $resName: ${e.message}")
                        safeRelease(player)
                        playTone(fbToneFreq, fbToneDuration)
                    }
                }
            }
        } else {
            soundScope.launch {
                playTone(fbToneFreq, fbToneDuration)
            }
        }
    }

    /**
     * MD5 Hashing helper to generate uniquely recognizable, clean file keys for text sentences.
     */
    private fun String.md5(): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            this.hashCode().toString()
        }
    }

    /**
     * Securely fetches and caches high-fidelity MP3 voice outputs from Youdao's speech synthesis engine.
     */
    private fun downloadAudioFile(text: String, file: File): Boolean {
        var connection: java.net.URLConnection? = null
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://dict.youdao.com/dictvoice?audio=$encodedText&le=zh"
            val url = URL(urlString)
            connection = url.openConnection()
            connection.connectTimeout = 4000
            connection.readTimeout = 4000

            connection.getInputStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return file.exists() && file.length() > 0
        } catch (e: Exception) {
            Log.e("KidsSoundEngine", "Failed to download pronunciation audio for: \"$text\". Error: ${e.message}")
            if (file.exists()) {
                file.delete()
            }
            return false
        }
    }

    /**
     * Speaks the sentence by loading from persistent cache or fetching it over the network.
     * Stops any currently playing audio if stopPrevious is enabled.
     */
    fun speak(text: String, stopPrevious: Boolean = true) {
        if (isMute) return

        // 1. Detect and parse "Incorrect" combinations dynamically
        if (text.startsWith("不对哦")) {
            val animalList = listOf(
                "panda" to "熊猫", "elephant" to "大象", "lion" to "狮子", "monkey" to "猴子", "giraffe" to "长颈鹿",
                "bear" to "大熊", "rabbit" to "兔子", "squirrel" to "松鼠", "owl" to "猫头鹰", "deer" to "小鹿",
                "dolphin" to "海豚", "whale" to "鲸鱼", "octopus" to "章鱼", "starfish" to "海星", "turtle" to "海龟",
                "ryder" to "莱德", "chase" to "阿奇", "marshall" to "毛毛", "skye" to "天天", "rubble" to "小砾"
            )
            val parts = text.split("再仔细找一找")
            if (parts.size >= 2) {
                val firstPart = parts[0]
                val secondPart = parts[1]
                
                val animal1 = animalList.firstOrNull { firstPart.contains(it.second) }?.first
                val animal2 = animalList.firstOrNull { secondPart.contains(it.second) }?.first
                
                if (animal1 != null && animal2 != null) {
                    val res1Name = "${animal1}_incorrect"
                    val res2Name = "${animal2}_search_suffix"
                    val resId1 = context.resources.getIdentifier(res1Name, "raw", context.packageName)
                    val resId2 = context.resources.getIdentifier(res2Name, "raw", context.packageName)
                    if (resId1 != 0 && resId2 != 0) {
                        playSequentialResources(resId1, resId2)
                        return
                    }
                }
            }
        }

        // 2. Standard Offline mapping matching
        val normalizedInput = text.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "").lowercase()
        val offlineResName = offlineSoundResources[normalizedInput]
        if (offlineResName != null) {
            val resId = context.resources.getIdentifier(offlineResName, "raw", context.packageName)
            if (resId != 0) {
                soundScope.launch {
                    synchronized(this@KidsSoundEngine) {
                        if (stopPrevious) {
                            stopPlaybackInternal()
                        }
                        var player: MediaPlayer? = null
                        try {
                            player = MediaPlayer.create(context, resId)
                            if (player != null) {
                                activePlayers.add(player)
                                currentMediaPlayer = player
                                player.setOnCompletionListener {
                                    synchronized(this@KidsSoundEngine) {
                                        if (currentMediaPlayer == player) {
                                            currentMediaPlayer = null
                                        }
                                    }
                                    safeRelease(player)
                                }
                                player.start()
                            } else {
                                Log.e("KidsSoundEngine", "player is null in speak offline for $offlineResName")
                            }
                        } catch (e: Exception) {
                            Log.e("KidsSoundEngine", "Error playing offline resource: ${e.message}")
                            safeRelease(player)
                        }
                    }
                }
                return
            }
        }

        // 3. Fallback cache/network check
        soundScope.launch {
            if (stopPrevious) {
                stopPlaybackInternal()
            }

            val key = text.md5()
            val cachedFile = File(cacheDir, "$key.mp3")

            // Determine if asset is ready locally
            val isReady = if (cachedFile.exists() && cachedFile.length() > 0) {
                true
            } else {
                downloadAudioFile(text, cachedFile)
            }

            if (isReady && cachedFile.exists()) {
                playMp3FileInternal(cachedFile)
            } else {
                Log.w("KidsSoundEngine", "Audios cache miss and offline. Falling back to synth chime.")
                // Play short synth melody as resilient secondary indicator
                playTone(550.0, 120)
            }
        }
    }

    /**
     * Play a list of text sentences in background on demand to build a smooth local sound buffer.
     */
    fun preCache(texts: List<String>) {
        soundScope.launch {
            for (text in texts) {
                val key = text.md5()
                val cachedFile = File(cacheDir, "$key.mp3")
                if (!cachedFile.exists() || cachedFile.length() == 0L) {
                    val success = downloadAudioFile(text, cachedFile)
                    if (success) {
                        Log.d("KidsSoundEngine", "Pre-cached: \"$text\"")
                    }
                    kotlinx.coroutines.delay(120)
                }
            }
        }
    }

    private fun stopPlaybackInternal() {
        synchronized(this) {
            val player = currentMediaPlayer
            currentMediaPlayer = null
            if (player != null) {
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (e: Exception) {
                    Log.e("KidsSoundEngine", "Error stopping player: ${e.message}")
                }
                safeRelease(player)
            }
        }
    }

    private fun playMp3FileInternal(file: File) {
        synchronized(this) {
            stopPlaybackInternal()
            var player: MediaPlayer? = null
            try {
                player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                }
                activePlayers.add(player)
                currentMediaPlayer = player
                player.setOnCompletionListener {
                    synchronized(this) {
                        if (currentMediaPlayer == player) {
                            currentMediaPlayer = null
                        }
                    }
                    safeRelease(player)
                }
            } catch (e: Exception) {
                Log.e("KidsSoundEngine", "Error playing MP3 file: ${e.message}")
                safeRelease(player)
                playTone(600.0, 100)
            }
        }
    }

    /**
     * Play reward sound: Play localized raw success chimes.
     */
    fun playSuccess() {
        if (isMute) return
        playRawResource("alert_correct", 523.25, 400)
    }

    /**
     * Play incorrect chime: Play localized raw incorrect speech warning.
     */
    fun playIncorrect() {
        if (isMute) return
        playRawResource("alert_incorrect", 164.81, 400)
    }

    /**
     * Play a cute, high-frequency bubble clicking pop sound.
     */
    fun playClick() {
        if (isMute) return
        playRawResource("alert_click", 987.77, 60)
    }

    /**
     * DSP Tone synthesis helper generating instant, artifact-free sinusoidal waves on child DeX support.
     */
    private fun playTone(frequency: Double, durationMs: Int) {
        try {
            val sampleRate = 8000
            val numSamples = (durationMs * sampleRate) / 1000
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)

            for (i in 0 until numSamples) {
                // simple sine wave formula with a short volume envelope (fade-out in last 20%)
                val fadeEnvelope = if (i > numSamples * 0.8) {
                    (numSamples - i).toDouble() / (numSamples * 0.2)
                } else {
                    1.0
                }
                sample[i] = sin(2 * Math.PI * i / (sampleRate / frequency)) * fadeEnvelope
            }

            var idx = 0
            for (dVal in sample) {
                val valShort = (dVal * 25000).toInt().toShort() // Amplitude scaled below clipping limits
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 10)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("KidsSoundEngine", "Error generating synth tone: ${e.message}")
        }
    }

    fun shutdown() {
        stopPlaybackInternal()
    }
}
