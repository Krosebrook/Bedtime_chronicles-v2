package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun DeepSpaceStarryBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    isMidnightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    if (!isDarkMode) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F8))
        ) {
            content()
        }
    } else if (isMidnightMode) {
        // High-contrast, completely flat pure black background for zero-glare late-night reading
        // With a tiny scattering of low-intensity starry pinpoints that twinkle staticly to maintain cozy sky context
        val stars = remember {
            val seededRand = Random(777)
            List(15) {
                StarData(
                    x = seededRand.nextFloat(),
                    y = seededRand.nextFloat(),
                    size = seededRand.nextFloat() * 1.5f + 0.5f,
                    speedGroup = seededRand.nextInt(2)
                )
            }
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                stars.forEach { star ->
                    // Faint stars
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = star.size,
                        center = Offset(star.x * width, star.y * height)
                    )
                }
            }
            content()
        }
    } else {
        // Deep space cozy navy to dark nebula purple vertical gradient
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF030310), // Midnight Navy Void
                Color(0xFF0B0625), // Twilight Purple Shadow
                Color(0xFF160938), // Stellar Violet Gas
                Color(0xFF07041A)  // Bottom space shadow
            )
        )

        val infiniteTransition = rememberInfiniteTransition(label = "StarNebulaTwinkle")
        
        val twinkle1 by infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "t1"
        )
        
        val twinkle2 by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "t2"
        )

        // Seeded random coordinate allocation for perfectly stable and even star spread
        val stars = remember {
            val seededRand = Random(1337)
            List(65) {
                StarData(
                    x = seededRand.nextFloat(),
                    y = seededRand.nextFloat(),
                    size = seededRand.nextFloat() * 2.8f + 0.8f,
                    speedGroup = seededRand.nextInt(2)
                )
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                stars.forEach { star ->
                    val alpha = if (star.speedGroup == 0) twinkle1 else twinkle2
                    val finalSize = star.size * (alpha * 0.4f + 0.6f)
                    
                    // Tiny pinpoint core
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = finalSize,
                        center = Offset(star.x * width, star.y * height)
                    )
                    
                    // Starlight cloud aura
                    if (star.size > 2.0f) {
                        drawCircle(
                            color = Color(0xFFC084FC).copy(alpha = alpha * 0.35f),
                            radius = finalSize * 3.5f,
                            center = Offset(star.x * width, star.y * height)
                        )
                    }
                }
            }
            content()
        }
    }
}

data class StarData(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedGroup: Int
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  isMidnightTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (isMidnightTheme) {
      MidnightColorScheme
  } else if (darkTheme) {
      DarkColorScheme
  } else {
      LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

