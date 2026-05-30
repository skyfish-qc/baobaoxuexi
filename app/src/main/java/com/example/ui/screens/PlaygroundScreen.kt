package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.Animal
import com.example.model.GameMode
import com.example.model.SceneData
import com.example.model.SceneType
import com.example.model.imageResId
import com.example.viewmodel.LearningEvent
import com.example.viewmodel.LearningViewModel
import com.example.viewmodel.ScreenType
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import coil.request.ImageRequest

// Beautiful colors for playground UI details
val SoftMint = Color(0xFF06D6A0)
val SoftCyanLight = Color(0xFFE0FAFF)

@Composable
fun AnimalImage(
    animal: Animal,
    size: Dp,
    emojiSize: TextUnit,
    modifier: Modifier = Modifier
) {
    if (animal.imageResId != 0) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(animal.imageResId)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build(),
            contentDescription = animal.name,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        Text(
            text = animal.emoji,
            fontSize = emojiSize,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    val color: Color,
    val size: Float,
    var vx: Float,
    var vy: Float,
    val shape: Int // 0: star, 1: circle, 2: square
)

@Composable
fun PlaygroundScreen(
    viewModel: LearningViewModel,
    modifier: Modifier = Modifier
) {
    val currentScene by viewModel.currentScene.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val starCount by viewModel.starCount.collectAsState()
    val isMute by viewModel.isMute.collectAsState()

    val selectedAnimal by viewModel.selectedAnimal.collectAsState()
    val targetAnimal by viewModel.targetAnimal.collectAsState()
    val wrongSelections by viewModel.wrongSelections.collectAsState()
    val isCelebrationActive by viewModel.isCelebrationActive.collectAsState()

    val scene = currentScene ?: SceneType.ZOO
    val animals = remember(scene) { SceneData.getAnimalsForScene(scene) }

    // Resolve the background layout image resource correctly
    val bgDrawableRes = when (scene) {
        SceneType.ZOO -> R.drawable.img_bg_zoo
        SceneType.FOREST -> R.drawable.img_bg_forest
        SceneType.OCEAN -> R.drawable.img_bg_ocean
        SceneType.PAW_PATROL -> R.drawable.img_bg_paw_patrol
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        // 1. Fullscreen picture-book scenery illustration background
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(bgDrawableRes)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build(),
            contentDescription = scene.displayName,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Interactive Animal Floating Nodes
        animals.forEach { animal ->
            val bobOffset = 0f

            // Pulse border animation if this animal is the correct target in Find-it Mode
            val isTarget = (gameMode == GameMode.FIND_IT && targetAnimal?.id == animal.id)

            // Error wiggles if wrong
            val isWrongPressed = (animal.id in wrongSelections)

            // Setup placement calculations based on absolute coordinates defined in the model
            val leftMargin = maxWidth * (animal.xPercent / 100f)
            val topMargin = maxHeight * (animal.yPercent / 100f)

            Box(
                modifier = Modifier
                    .offset(x = leftMargin, y = topMargin + bobOffset.dp)
                    .size(86.dp)
            ) {
                // Outer static highlight ring for correct targets to gently help the kid
                if (isTarget) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(78.dp)
                            .background(SunshineYellow.copy(alpha = 0.45f), CircleShape)
                    )
                }

                // Inner Main Clickable Animal Bubble
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(70.dp)
                        .shadow(if (isWrongPressed) 2.dp else 6.dp, CircleShape)
                        .background(
                            color = if (isWrongPressed) Color.LightGray.copy(alpha = 0.5f) else Color.White,
                            shape = CircleShape
                        )
                        .border(
                            width = if (isTarget) 4.dp else 2.5.dp,
                            color = if (isTarget) SunshineYellow else if (isWrongPressed) Color.Gray else SoftMint,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                viewModel.onAnimalClicked(animal)
                            }
                        )
                        .scale(if (isWrongPressed) 0.95f else 1.0f)
                        .testTag("animal_node_${animal.id}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimalImage(
                            animal = animal,
                            size = 52.dp,
                            emojiSize = 32.sp
                        )
                    }
                }

                // Name tag indicator bubble on bottom of each animal
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 12.dp)
                        .shadow(3.dp, RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(DeepCharcoal, Color(0xFF5E4945))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = animal.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // If wrong, overlay a small cute red sign to help visual exclusion
                if (isWrongPressed) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .size(24.dp)
                            .background(Color(0xFFFF595E), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Dynamic Playful Mode & Control Toolbar (Floating Top bar)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Left block: Back to selector Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenType.SCENE_SELECT) },
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, Color.LightGray, CircleShape)
                        .size(42.dp)
                        .testTag("back_to_scene_select")
                ) {
                    Text(
                        text = "🔙",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Scene Title Display
                Box(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = scene.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCharcoal
                    )
                }
            }

            // Middle block: Cute Toggle Switch between modes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(2.5.dp, SunshineYellow, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                // Learning Mode Segment Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            color = if (gameMode == GameMode.LEARNING) SkyBlue else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setGameMode(GameMode.LEARNING) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("mode_toggle_learning")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📖 学习宝典",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameMode == GameMode.LEARNING) Color.White else DeepCharcoal
                        )
                    }
                }

                // Find-it Mode Segment Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            color = if (gameMode == GameMode.FIND_IT) WarmCoral else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setGameMode(GameMode.FIND_IT) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("mode_toggle_find")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔍 找一找游戏",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameMode == GameMode.FIND_IT) Color.White else DeepCharcoal
                        )
                    }
                }
            }

            // Right block: Stars & Sound
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Star display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(2.dp, SunshineYellow, RoundedCornerShape(16.dp))
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    Text("⭐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$starCount",
                        color = DeepCharcoal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mute
                IconButton(
                    onClick = { viewModel.setMute(!isMute) },
                    modifier = Modifier
                        .shadow(3.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .size(38.dp)
                        .testTag("playground_mute_toggle")
                ) {
                    Text(
                        text = if (isMute) "🔇" else "🔊",
                        fontSize = 16.sp
                    )
                }
            }
        }

        // 5. Find-it game mode visual instruction card
        if (gameMode == GameMode.FIND_IT) {
            targetAnimal?.let { target ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-14).dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(3.5.dp, WarmCoral, RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "请点击 ☞ ",
                            fontSize = 16.sp,
                            color = DeepCharcoal,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(SunshineYellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${target.name} ${target.emoji}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmCoral
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Audio Instruction repeating helper button
                        Button(
                            onClick = { viewModel.triggerTargetRepronounce() },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("repronounce_button")
                        ) {
                            Text("🔊 再听一次", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 6. Celebration Screen Overlay (When correctly matched)
        if (isCelebrationActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .shadow(20.dp, RoundedCornerShape(28.dp))
                        .border(5.dp, SunshineYellow, RoundedCornerShape(28.dp))
                        .width(360.dp)
                        .padding(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "🎉 太棒啦！ 🎉",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmCoral,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .background(SoftMint.copy(alpha = 0.15f), CircleShape)
                        ) {
                            val target = targetAnimal
                            if (target != null) {
                                AnimalImage(
                                    animal = target,
                                    size = 72.dp,
                                    emojiSize = 52.sp
                                )
                            } else {
                                Text(
                                    text = "⭐",
                                    fontSize = 56.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "你找到了: ${targetAnimal?.name ?: ""}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "奖励 🌟 +1",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SunshineYellow
                        )
                    }
                }
            }
        }

        // 7. Learning mode animal facts details bottom sheet dialog / modal
        selectedAnimal?.let { animal ->
            Dialog(onDismissRequest = { viewModel.clearSelectedAnimal() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .shadow(16.dp, RoundedCornerShape(28.dp))
                        .border(4.dp, SkyBlue, RoundedCornerShape(28.dp))
                        .testTag("fact_dialog"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Big Symbol
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(0.38f)
                                .background(SoftCyanLight, RoundedCornerShape(20.dp))
                                .padding(12.dp)
                        ) {
                            AnimalImage(
                                animal = animal,
                                size = 80.dp,
                                emojiSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = animal.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = DeepCharcoal
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right Column: Fact explanation details
                        Column(
                            modifier = Modifier.weight(0.62f)
                        ) {
                            // Close Button row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pinyin Spell Caption
                                Text(
                                    text = "拼音: ${animal.pinyin}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBlue
                                )

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                                        .clickable { viewModel.clearSelectedAnimal() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✕",
                                        color = DeepCharcoal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "English: ${animal.english}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmCoral
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Fun Fact bubble
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoapYellow, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "探索趣味秘密:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE5A93C)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = animal.funFact,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DeepCharcoal,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action button to pronounce again
                            Button(
                                onClick = {
                                    val ttsText = "${animal.name}！拼写是：${animal.pinyin}。英文叫 ${animal.english}。${animal.funFact}"
                                    viewModel.triggerEvent(LearningEvent.Speak(ttsText))
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("fact_pronounce_button")
                            ) {
                                Text("🔊 再听一遍", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
