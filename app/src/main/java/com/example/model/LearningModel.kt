package com.example.model

enum class SceneType(val displayName: String, val bgResName: String) {
    ZOO("开心动物园", "img_bg_zoo"),
    FOREST("奇妙大森林", "img_bg_forest"),
    OCEAN("梦幻大海洋", "img_bg_ocean")
}

enum class GameMode(val displayName: String) {
    LEARNING("学习宝典"),
    FIND_IT("找一找游戏")
}

data class Animal(
    val id: String,
    val name: String,
    val pinyin: String,
    val english: String,
    val emoji: String,
    val funFact: String,
    val xPercent: Float, // horizontal placement percentage on screen (5% to 85%)
    val yPercent: Float  // vertical placement percentage on screen (10% to 80%)
)

object SceneData {
    val zooAnimals = listOf(
        Animal(
            id = "panda",
            name = "熊猫",
            pinyin = "xióng māo",
            english = "Panda",
            emoji = "🐼",
            funFact = "大熊猫最喜欢吃香甜脆爽的竹子，胖嘟嘟的超级可爱哦！",
            xPercent = 65f,
            yPercent = 66f
        ),
        Animal(
            id = "elephant",
            name = "大象",
            pinyin = "dà xiàng",
            english = "Elephant",
            emoji = "🐘",
            funFact = "大象有一根神奇的长鼻子，能吸水给自己洗澡喷泉呢！",
            xPercent = 74f,
            yPercent = 25f
        ),
        Animal(
            id = "lion",
            name = "狮子",
            pinyin = "shī zi",
            english = "Lion",
            emoji = "🦁",
            funFact = "狮子是森林草原之王，他笑起来嗷呜一声，超级威风！",
            xPercent = 42f,
            yPercent = 35f
        ),
        Animal(
            id = "monkey",
            name = "猴子",
            pinyin = "hóu zi",
            english = "Monkey",
            emoji = "🐒",
            funFact = "小猴子是个攀爬小能手，最喜欢在树上荡秋千吃香蕉！",
            xPercent = 15f,
            yPercent = 24f
        ),
        Animal(
            id = "giraffe",
            name = "长颈鹿",
            pinyin = "cháng jǐng lù",
            english = "Giraffe",
            emoji = "🦒",
            funFact = "长颈鹿的脖子特别特别长，能轻松吃到大树顶上最嫩的绿叶！",
            xPercent = 20f,
            yPercent = 65f
        )
    )

    val forestAnimals = listOf(
        Animal(
            id = "bear",
            name = "大熊",
            pinyin = "dà xióng",
            english = "Bear",
            emoji = "🐻",
            funFact = "大熊毛茸茸的，冬天会躲在温暖的树洞里睡一个大懒觉！",
            xPercent = 76f,
            yPercent = 28f
        ),
        Animal(
            id = "rabbit",
            name = "兔子",
            pinyin = "tù zi",
            english = "Rabbit",
            emoji = "🐰",
            funFact = "小兔子长着长长的耳朵、红红的眼睛，最爱吃甜甜的胡萝卜！",
            xPercent = 45f,
            yPercent = 32f
        ),
        Animal(
            id = "squirrel",
            name = "松鼠",
            pinyin = "sōng shǔ",
            english = "Squirrel",
            emoji = "🐿️",
            funFact = "小松鼠有一条蓬松的大尾巴，喜欢到各处收集好吃的松果！",
            xPercent = 23f,
            yPercent = 58f
        ),
        Animal(
            id = "owl",
            name = "猫头鹰",
            pinyin = "māo tóu yīng",
            english = "Owl",
            emoji = "🦉",
            funFact = "猫头鹰是大森林的夜间卫士，在黑夜里眼睛会闪闪发光！",
            xPercent = 16f,
            yPercent = 18f
        ),
        Animal(
            id = "deer",
            name = "小鹿",
            pinyin = "xiǎo lù",
            english = "Deer",
            emoji = "🦌",
            funFact = "小鹿身上有美丽的斑点，跑起步来轻悄悄，又快又优美！",
            xPercent = 66f,
            yPercent = 60f
        )
    )

    val oceanAnimals = listOf(
        Animal(
            id = "dolphin",
            name = "海豚",
            pinyin = "hǎi tún",
            english = "Dolphin",
            emoji = "🐬",
            funFact = "小海豚最喜欢在海浪里跳舞，是海洋里最爱笑的小天使！",
            xPercent = 15f,
            yPercent = 28f
        ),
        Animal(
            id = "whale",
            name = "鲸鱼",
            pinyin = "jīng yú",
            english = "Whale",
            emoji = "🐳",
            funFact = "大蓝鲸是海洋里最大的巨无霸，头顶会喷出好高的小喷泉！",
            xPercent = 48f,
            yPercent = 22f
        ),
        Animal(
            id = "octopus",
            name = "章鱼",
            pinyin = "zhāng yú",
            english = "Octopus",
            emoji = "🐙",
            funFact = "章鱼小八有八只长长的小脚丫，生气的时候能喷出黑黑的墨汁！",
            xPercent = 80f,
            yPercent = 32f
        ),
        Animal(
            id = "starfish",
            name = "海星",
            pinyin = "hǎi xīng",
            english = "Starfish",
            emoji = "⭐",
            funFact = "亮晶晶的海星就像是掉落在神秘海底夜空里的小星星！",
            xPercent = 27f,
            yPercent = 68f
        ),
        Animal(
            id = "turtle",
            name = "海龟",
            pinyin = "hǎi guī",
            english = "Turtle",
            emoji = "🐢",
            funFact = "海龟背着厚厚重重的安全壳，在大海里划水游泳，像飞翔一样！",
            xPercent = 68f,
            yPercent = 66f
        )
    )

    fun getAnimalsForScene(type: SceneType): List<Animal> {
        return when (type) {
            SceneType.ZOO -> zooAnimals
            SceneType.FOREST -> forestAnimals
            SceneType.OCEAN -> oceanAnimals
        }
    }
}
