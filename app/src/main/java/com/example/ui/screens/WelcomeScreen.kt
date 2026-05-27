package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
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
import com.example.viewmodel.LearningViewModel
import com.example.viewmodel.ScreenType

// Playful child color palette
val SoapYellow = Color(0xFFFFF9E6)
val SunshineYellow = Color(0xFFFFD23F)
val WarmCoral = Color(0xFFFF7096)
val SkyBlue = Color(0xFF4EA8DE)
val DeepCharcoal = Color(0xFF402E2A)

@Composable
fun WelcomeScreen(
    viewModel: LearningViewModel,
    modifier: Modifier = Modifier
) {
    val isMute by viewModel.isMute.collectAsState()

    // Slide/float translation for clouds
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud1"
    )
    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 1400f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud2"
    )

    // Bobbing scaling motion for the main logo
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo"
    )
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = SineCrossingEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_rot"
    )

    // Spring scaling when touching buttons
    var isPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0FAFF), // Soft morning cyan sky
                        SoapYellow
                    )
                )
            )
    ) {
        // Floating Clouds
        FloatingCloud(
            width = 120.dp,
            height = 60.dp,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .offset(x = cloudOffset1.dp, y = 40.dp)
        )

        FloatingCloud(
            width = 160.dp,
            height = 80.dp,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier
                .offset(x = cloudOffset2.dp, y = 140.dp)
        )

        // Main Landscape Content Row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            
            // Left Column: Logo & Animations
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .scale(logoScale)
                    .rotate(logoRotation)
            ) {
                // Shiny Sun Halo Ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(SunshineYellow.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(6.dp, SunshineYellow, CircleShape)
                        .shadow(8.dp, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "App Icon Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(CircleShape)
                            .testTag("welcome_app_logo"),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Right Column: Beautiful Title & Start Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(360.dp)
            ) {
                // Title Bubble Header
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(4.dp, SkyBlue, RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "宝宝识图乐园",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCharcoal,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "和小动物快乐做游戏 • 轻松认词学拼音",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Start Button with Spring Bounce Touch Handling
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(buttonScale)
                        .shadow(12.dp, RoundedCornerShape(32.dp))
                        .background(Brush.horizontalGradient(listOf(WarmCoral, Color(0xFFFF9E79))), RoundedCornerShape(32.dp))
                        .border(5.dp, Color.White, RoundedCornerShape(32.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                viewModel.navigateTo(ScreenType.SCENE_SELECT)
                            }
                        )
                        .padding(horizontal = 46.dp, vertical = 16.dp)
                        .testTag("start_button")
                ) {
                    Text(
                        text = "开始快乐探索 ✨",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Top-Right Corner Controls: Mute Toggle & Star indicators
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stars count indicator in top corner
            val starCount by viewModel.starCount.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, SunshineYellow, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("⭐", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$starCount",
                    color = DeepCharcoal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Custom Mute Button (VolumeUp / VolumeMute switcher)
            IconButton(
                onClick = { viewModel.setMute(!isMute) },
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(2.dp, SkyBlue, CircleShape)
                    .size(42.dp)
                    .testTag("mute_toggle")
            ) {
                Text(
                    text = if (isMute) "🔇" else "🔊",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun FloatingCloud(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width, height)
            .shadow(2.dp, RoundedCornerShape(height / 2))
            .background(color, RoundedCornerShape(height / 2))
    )
}

// Custom easing for smooth periodic swaying
val SineCrossingEasing = Easing { fraction ->
    kotlin.math.sin(fraction * Math.PI).toFloat()
}
