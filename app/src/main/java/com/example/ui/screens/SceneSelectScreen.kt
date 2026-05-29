package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SceneType
import com.example.viewmodel.LearningViewModel
import com.example.viewmodel.ScreenType

@Composable
fun SceneSelectScreen(
    viewModel: LearningViewModel,
    modifier: Modifier = Modifier
) {
    val isMute by viewModel.isMute.collectAsState()
    val starCount by viewModel.starCount.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD), // Soft atmospheric blue
                        Color(0xFFFFFDF2)  // Soft warm sand
                    )
                )
            )
    ) {
        // Back Button on Top Left
        IconButton(
            onClick = { viewModel.navigateTo(ScreenType.WELCOME) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .shadow(4.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(2.dp, SkyBlue, CircleShape)
                .size(44.dp)
                .testTag("back_to_welcome_button")
        ) {
            Text(
                text = "🔙",
                fontSize = 20.sp
            )
        }

        // Top Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(3.dp, SunshineYellow, RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🌟 请选择你想探索的奇妙世界 🌟",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepCharcoal,
                    letterSpacing = 1.sp
                )
            }
        }

        // Action Buttons Top Right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Star counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, SunshineYellow, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

            // Mute Switcher
            IconButton(
                onClick = { viewModel.setMute(!isMute) },
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(2.dp, SkyBlue, CircleShape)
                    .size(40.dp)
                    .testTag("scene_mute_toggle")
            ) {
                Text(
                    text = if (isMute) "🔇" else "🔊",
                    fontSize = 18.sp
                )
            }
        }

        // Horizontal Row containing the 4 gorgeous scene selection cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SceneCard(
                sceneType = SceneType.ZOO,
                emoji = "🐼",
                themeColor = Color(0xFF06D6A0), // Bright green
                subLabel = "大象 熊猫 长颈鹿... 🐘",
                bgImgRes = R.drawable.img_bg_zoo,
                onClicked = { viewModel.selectScene(SceneType.ZOO) },
                modifier = Modifier.testTag("select_zoo_card")
            )

            SceneCard(
                sceneType = SceneType.FOREST,
                emoji = "🌲",
                themeColor = Color(0xFFFFB703), // Friendly forest orange-gold
                subLabel = "小松鼠 兔子 夜猫头鹰... 🦉",
                bgImgRes = R.drawable.img_bg_forest,
                onClicked = { viewModel.selectScene(SceneType.FOREST) },
                modifier = Modifier.testTag("select_forest_card")
            )

            SceneCard(
                sceneType = SceneType.OCEAN,
                emoji = "🐬",
                themeColor = Color(0xFF2196F3), // Aquatic deep blue
                subLabel = "大蓝鲸 八爪鱼 海海龟... 🐳",
                bgImgRes = R.drawable.img_bg_ocean,
                onClicked = { viewModel.selectScene(SceneType.OCEAN) },
                modifier = Modifier.testTag("select_ocean_card")
            )

            SceneCard(
                sceneType = SceneType.PAW_PATROL,
                emoji = "🐾",
                themeColor = Color(0xFFE63946), // Vibrant PAW red
                subLabel = "莱德 阿奇 毛毛 天天... 🐾",
                bgImgRes = R.drawable.img_bg_paw_patrol,
                onClicked = { viewModel.selectScene(SceneType.PAW_PATROL) },
                modifier = Modifier.testTag("select_paw_patrol_card")
            )
        }
    }
}

@Composable
fun SceneCard(
    sceneType: SceneType,
    emoji: String,
    themeColor: Color,
    subLabel: String,
    bgImgRes: Int,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardScale = 1.0f

    Box(
        modifier = modifier
            .width(220.dp)
            .height(180.dp)
            .scale(cardScale)
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(4.dp, themeColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    onClicked()
                }
            )
    ) {
        // High quality scenery preview behind
        Image(
            painter = painterResource(id = bgImgRes),
            contentDescription = sceneType.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .scale(1.1f) // zoom background inside slightly
        )

        // Tinted overlay on top of scene preview to highlight text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // Large Floating Emoji Badge on Top Center
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .shadow(4.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(3.dp, themeColor, CircleShape)
                .size(48.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }

        // Labels at the bottom of the card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = sceneType.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}
