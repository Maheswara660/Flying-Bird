package com.maheswara660.flyingbird.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import com.maheswara660.flyingbird.game.GameEngine
import com.maheswara660.flyingbird.game.GameState
import com.maheswara660.flyingbird.domain.Settings
import com.maheswara660.flyingbird.domain.UserProfile
import com.maheswara660.flyingbird.domain.LeaderboardEntry
import com.maheswara660.flyingbird.physics.PhysicsEngine

// Styled Color Tokens
val BrandAmber = Color(0xFFF2A541)
val BrandRed = Color(0xFFD94A4A)
val BrandCyan = Color(0xFF53D8FB)
val TextLight = Color(0xFFF2F2F2)
val DarkTerminal = Color(0xFF151D24)
val DarkCard = Color(0xCC202C39)

val themeBgColor: Color
    @Composable
    get() = when (LocalTheme.current) {
        "sunset" -> Color(0xDD2A1414)
        "winter" -> Color(0xDD0F1F2C)
        else -> DarkCard
    }

val themeBorderColor: Color
    @Composable
    get() = when (LocalTheme.current) {
        "sunset" -> Color(0xFFE67E22)
        "winter" -> Color(0xFF81D4FA)
        else -> BrandAmber
    }

@Composable
fun SplashScreenContent() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animate bird flying across background silhouette
    val birdOffset by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 950f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E272C)),
        contentAlignment = Alignment.Center
    ) {
        // Drifting fog / skyline simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x66000000))
                    )
                )
        )

        // Flying bird silhouette animation
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleY = size.height / PhysicsEngine.LOGICAL_HEIGHT
            val y = 250f * scaleY
            
            // Draw simple wing flap silhouette
            val isFlapUp = (birdOffset.toInt() / 40) % 2 == 0
            val path = Path()
            path.moveTo(birdOffset, y)
            if (isFlapUp) {
                path.quadraticBezierTo(birdOffset + 15f, y - 25f, birdOffset + 30f, y - 10f)
                path.quadraticBezierTo(birdOffset + 15f, y - 5f, birdOffset, y)
            } else {
                path.quadraticBezierTo(birdOffset + 15f, y + 25f, birdOffset + 30f, y - 10f)
                path.quadraticBezierTo(birdOffset + 15f, y - 5f, birdOffset, y)
            }
            path.close()
            drawPath(path, Color(0x3353D8FB))
        }

        // Center Logo and Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "FLYING BIRD",
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                color = BrandAmber,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SURVIVE THE FALLEN WORLD",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB0BEC5),
                letterSpacing = 2.sp
            )
        }

        // Loading text
        Text(
            text = "LOADING SYSTEM...",
            fontSize = 12.sp,
            color = Color(0xFF78909C),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun MainMenuScreenContent(
    engine: GameEngine,
    onPlay: () -> Unit,
    onSettings: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    val themeBg = themeBgColor
    val themeBorder = themeBorderColor

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(themeBg)
                .border(1.dp, themeBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(28.dp)
        ) {
            Text(
                text = "FLYING BIRD",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = themeBorder,
                letterSpacing = 2.sp
            )
            Text(
                text = "Survive The Fallen World",
                fontSize = 14.sp,
                color = TextLight.copy(alpha = 0.6f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // About Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF151D24))
                        .border(1.5.dp, themeBorder.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .clickable { showAboutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "ℹ", color = themeBorder, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Play Button (2.7x larger)
                val playInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val playPressed by playInteractionSource.collectIsPressedAsState()
                val playScale by animateFloatAsState(if (playPressed) 0.92f else 1.0f)
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(playScale)
                        .shadow(8.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF8B5A2B), Color(0xFF5C3C1E))
                            )
                        )
                        .border(3.dp, themeBorder, RoundedCornerShape(50))
                        .clickable(interactionSource = playInteractionSource, indication = null, onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = themeBorder,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF151D24))
                        .border(1.5.dp, themeBorder.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .clickable { onSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚙", color = themeBorder, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAboutDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .clickable { showAboutDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeBg)
                        .border(1.5.dp, themeBorder, RoundedCornerShape(12.dp))
                        .clickable(enabled = false) {}
                        .padding(24.dp)
                ) {
                    Text(
                        text = "ABOUT THE MISSION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeBorder,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "FLYING BIRD is a tactical survival simulator set in a post-apocalyptic decaying world.\n\n" +
                               "Controls:\n" +
                               "• SPACE / TAP: Flap wings to fly upward\n" +
                               "• ESCAPE: Pause run / Return to terminal\n\n" +
                               "Avoid obstacles and navigate hazardous biomes as the speed scaling intensifies.",
                        fontSize = 13.sp,
                        color = TextLight,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    RustedButton(
                        text = "DISMISS",
                        onClick = { showAboutDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun GameplayHUD(
    theme: String,
    highScore: Int,
    onPause: () -> Unit
) {
    val themeBorder = themeBorderColor
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC151D24))
                .border(1.dp, themeBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Styled Pause Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33FFFFFF))
                    .clickable { onPause() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "‖", color = themeBorder, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(themeBorder.copy(alpha = 0.3f))
            )

            // Theme Info
            Column {
                Text(text = "THEME", fontSize = 8.sp, color = TextLight.copy(0.5f), fontWeight = FontWeight.Bold)
                Text(text = theme.uppercase(), fontSize = 11.sp, color = themeBorder, fontWeight = FontWeight.Black)
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(themeBorder.copy(alpha = 0.3f))
            )

            // High Score Info
            Column {
                Text(text = "HI-SCORE", fontSize = 8.sp, color = TextLight.copy(0.5f), fontWeight = FontWeight.Bold)
                Text(text = "$highScore", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    val themeBg = themeBgColor
    val themeBorder = themeBorderColor
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(themeBg)
                .border(1.5.dp, themeBorder, RoundedCornerShape(12.dp))
                .padding(28.dp)
        ) {
            Text(
                text = "SYSTEM PAUSED",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = themeBorder,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF151D24))
                        .border(1.5.dp, themeBorder.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .clickable { onRestart() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⟳", color = themeBorder, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Resume Button (2.7x larger)
                val resumeInteractionSource = remember { MutableInteractionSource() }
                val resumePressed by resumeInteractionSource.collectIsPressedAsState()
                val resumeScale by animateFloatAsState(if (resumePressed) 0.92f else 1.0f)
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(resumeScale)
                        .shadow(8.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF8B5A2B), Color(0xFF5C3C1E))
                            )
                        )
                        .border(3.dp, themeBorder, RoundedCornerShape(50))
                        .clickable(interactionSource = resumeInteractionSource, indication = null, onClick = onResume),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = themeBorder,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Menu Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF151D24))
                        .border(1.5.dp, themeBorder.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .clickable { onMainMenu() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☰", color = themeBorder, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameOverScreenContent(
    score: Int,
    bestScore: Int,
    distance: Float,
    obstaclesPassed: Int,
    onRetry: () -> Unit,
    onMainMenu: () -> Unit
) {
    val themeBg = themeBgColor
    val borderCol = when (LocalTheme.current) {
        "sunset" -> Color(0xFFE67E22)
        "winter" -> Color(0xFF81D4FA)
        else -> BrandRed
    }
    val titleCol = when (LocalTheme.current) {
        "sunset" -> Color(0xFFE67E22)
        "winter" -> Color(0xFF81D4FA)
        else -> BrandRed
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(themeBg)
                .border(1.5.dp, borderCol, RoundedCornerShape(12.dp))
                .padding(28.dp)
        ) {
            Text(
                text = "MISSION FAILED",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = titleCol,
                letterSpacing = 2.sp
            )
            Text(
                text = "The bird has collapsed in decay.",
                fontSize = 11.sp,
                color = TextLight.copy(0.5f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Statistics Board
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF151D24))
                    .padding(14.dp)
            ) {
                StatRow(label = "FINAL SCORE", value = "$score", highlight = true)
                StatRow(label = "BEST RECORD", value = "$bestScore", highlight = false)
                StatRow(label = "DISTANCE FLOWN", value = "${distance.toInt()} meters", highlight = false)
                StatRow(label = "HAZARDS CLEARED", value = "$obstaclesPassed", highlight = false)
            }

            Spacer(modifier = Modifier.height(24.dp))
            RustedButton(text = "REDEPLOY (SPACE)", onClick = onRetry, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            RustedButton(text = "MAIN TERMINAL", onClick = onMainMenu, modifier = Modifier.fillMaxWidth())
        }
    }
}


@Composable
fun SettingsScreenContent(
    settings: Settings,
    onSaveVolumes: (music: Float, effects: Float) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectGraphics: (String) -> Unit,
    onBack: () -> Unit
) {
    val themeBg = themeBgColor
    val themeBorder = themeBorderColor
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(480.dp)
                .height(350.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(themeBg)
                .border(1.dp, themeBorder.copy(0.4f), RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "SYSTEM CONFIGURATION",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = themeBorder,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Audio Section
            Text(text = "AUDIO OUTPUT", fontSize = 12.sp, color = BrandCyan, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(text = "MUSIC", modifier = Modifier.width(80.dp), fontSize = 13.sp, color = TextLight)
                Slider(
                    value = settings.musicVolume,
                    onValueChange = { onSaveVolumes(it, settings.effectsVolume) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = themeBorder, activeTrackColor = themeBorder),
                    modifier = Modifier.weight(1f)
                )
                Text(text = "${settings.musicVolume.toInt()}%", fontSize = 12.sp, color = TextLight, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(text = "SOUND FX", modifier = Modifier.width(80.dp), fontSize = 13.sp, color = TextLight)
                Slider(
                    value = settings.effectsVolume,
                    onValueChange = { onSaveVolumes(settings.musicVolume, it) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = themeBorder, activeTrackColor = themeBorder),
                    modifier = Modifier.weight(1f)
                )
                Text(text = "${settings.effectsVolume.toInt()}%", fontSize = 12.sp, color = TextLight, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Graphics Section
            Text(text = "GRAPHICS QUALITY", fontSize = 12.sp, color = BrandCyan, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("low", "medium", "high", "ultra").forEach { quality ->
                    val selected = settings.graphicsQuality == quality
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) themeBorder else Color(0xFF151D24))
                            .clickable { onSelectGraphics(quality) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = quality.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.Black else TextLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Theme Selection
            Text(text = "APOCALYPSE THEME", fontSize = 12.sp, color = BrandCyan, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("default", "sunset", "winter").forEach { themeName ->
                    val selected = settings.theme == themeName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) themeBorder else Color(0xFF151D24))
                            .clickable { onSelectTheme(themeName) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = themeName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.Black else TextLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            RustedButton(text = "SAVE & CLOSE", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

// Custom Helpers
@Composable
fun RustedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val themeBorder = themeBorderColor
    
    // Scale animations on click
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1.0f)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF8B5A2B), Color(0xFF5C3C1E))
                )
            )
            .border(1.5.dp, themeBorder.copy(0.7f), RoundedCornerShape(6.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = themeBorder,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, highlight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextLight.copy(0.6f))
        Text(
            text = value,
            fontSize = 14.sp,
            color = if (highlight) themeBorderColor else TextLight,
            fontWeight = FontWeight.Bold
        )
    }
}
