import java.net.URL
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.kidspicture.pztbqr"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("downloadMp3Resources") {
    doLast {
        // Use relative path from root directory directly to bypass Project object serialization in configuration cache
        val rawDir = File("./app/src/main/res/raw")
        if (!rawDir.exists()) {
            rawDir.mkdirs()
        }

        // Define animal specs: id, name, pinyin, english, funFact
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
            listOf("rubble", "小砾", "xiǎo lì", "Rubble", "小砾是一只力气很大的斗牛犬工程犬，驾驶着帅气的黄色挖掘机，最喜欢吃香甜的零食和滑滑板！")
        )

        val textMap = mutableMapOf<String, String>()

        // System / UI Speech Map
        textMap["welcome_btn"] = "欢迎来到宝宝识图乐园！点击大按钮开始玩耍吧！"
        textMap["welcome_btn_below"] = "欢迎来到宝宝识图乐园！点击下面的大按钮开始玩耍吧！"
        textMap["scene_select_intro"] = "小朋友，请选择一个奇妙的世界开始探索吧！有动物园、大森林和海洋世界哦！"
        textMap["zoo_enter"] = "欢迎来到开心动物园！"
        textMap["forest_enter"] = "欢迎来到奇妙大森林！"
        textMap["ocean_enter"] = "欢迎来到梦幻大海洋！"
        textMap["paw_patrol_enter"] = "欢迎来到汪汪队基地！"
        textMap["mode_learning_intro"] = "现在是学习宝典模式。点击画面中可爱的小家伙，学习他们的名字和有趣知识吧！"
        textMap["mode_learning_action"] = "进入学习宝典模式！指一指画面上的小动物，听听他们有什么秘密吧！"
        textMap["mode_find_action"] = "进入找一找小游戏！准备好了吗？"

        // Populate dynamic animal speech raw resource identifiers
        for (a in animals) {
            val id = a[0]
            val name = a[1]
            val pinyin = a[2]
            val english = a[3]
            val funFact = a[4]

            textMap["${id}_fact"] = "${name}！拼写是：${pinyin}。英文叫 ${english}。${funFact}"
            textMap["${id}_find"] = "请帮我找一找：${name}在哪里呢？"
            textMap["${id}_found"] = "哇！你太棒啦！成功找到了 ${name}！奖励一颗亮闪闪的小星星！"
            textMap["${id}_incorrect"] = "不对哦，那是${name}。"
            textMap["${id}_search_suffix"] = "再仔细找一找：${name}藏在哪里啦？"
            textMap["${id}_repronounce"] = "在哪里呢？再听一次：请帮我找一找${name}"
        }

        // Quick verbal sounds for alerts
        textMap["alert_correct"] = "答对了！"
        textMap["alert_incorrect"] = "答错了，再试试吧！"
        textMap["alert_click"] = "哒"

        println("Offline sound pre-cache running: preparing ${textMap.size} files in src/main/res/raw/")
        for ((id, text) in textMap) {
            val outFile = File(rawDir, "$id.mp3")
            if (outFile.exists() && outFile.length() > 0) {
                continue
            }
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                val encoded: String = URLEncoder.encode(text, "UTF-8")
                val url = URL("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-CN&client=tw-ob&q=$encoded")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/437.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9")
                conn.useCaches = false
                
                val status = conn.responseCode
                if (status >= 400) {
                    System.err.println("Warning: failed to download speech stream for $id: Server returned HTTP " + status)
                    continue
                }
                
                input = conn.inputStream
                output = FileOutputStream(outFile)
                val buffer = ByteArray(4096)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    output?.write(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
                println("Pre-fetched MP3: raw/$id.mp3 for text \"$text\"")
            } catch (e: Exception) {
                System.err.println("Warning: failed to download speech stream for $id: ${e.message}")
            } finally {
                try { input?.close() } catch (ignored: Exception) {}
                try { output?.close() } catch (ignored: Exception) {}
            }
        }
    }
}

tasks.register("downloadOfflineImages") {
    doLast {
        val drawableDir = File("./app/src/main/res/drawable")
        if (!drawableDir.exists()) {
            drawableDir.mkdirs()
        }

        val animalImageUrls = mapOf(
            "panda" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f43c.png",
            "elephant" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f418.png",
            "lion" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f981.png",
            "monkey" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f435.png",
            "giraffe" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f992.png",
            
            "bear" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f43b.png",
            "rabbit" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f430.png",
            "squirrel" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f43f.png",
            "owl" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f989.png",
            "deer" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f98c.png",
            
            "dolphin" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f42c.png",
            "whale" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f433.png",
            "octopus" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f419.png",
            "starfish" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u2b50.png",
            "turtle" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f422.png",
            
            "ryder" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f466.png",
            "chase" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f415.png",
            "marshall" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f429.png",
            "skye" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f436.png",
            "rubble" to "https://cdn.jsdelivr.net/gh/googlefonts/noto-emoji@main/png/128/emoji_u1f9ae.png"
        )

        println("Offline animal image pre-cache running: preparing ${animalImageUrls.size} files in src/main/res/drawable/")
        for ((id, urlStr) in animalImageUrls) {
            val outFile = File(drawableDir, "img_animal_$id.png")
            if (outFile.exists() && outFile.length() > 0) {
                continue
            }
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                conn.useCaches = false
                
                val status = conn.responseCode
                if (status >= 400) {
                    System.err.println("Warning: failed to download image for $id: Server returned HTTP " + status)
                    continue
                }
                
                input = conn.inputStream
                output = FileOutputStream(outFile)
                val buffer = ByteArray(4096)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    output?.write(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
                println("Pre-fetched PNG: drawable/img_animal_$id.png")
            } catch (e: Exception) {
                System.err.println("Warning: failed to download image for $id: ${e.message}")
            } finally {
                try { input?.close() } catch (ignored: Exception) {}
                try { output?.close() } catch (ignored: Exception) {}
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("downloadMp3Resources")
    dependsOn("downloadOfflineImages")
}

