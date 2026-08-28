package com.example.checkpoint.ui.sections.friends.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Reusable avatar component supporting network URLs, local file paths, drawable resources, and letter initials fallback.
 */
@Composable
fun FriendAvatar(
    username: String,
    avatarUrl: String?,
    size: Dp = 64.dp,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Extract first letter for placeholder fallback
    val initial = remember(username) {
        username.trim().firstOrNull()?.uppercase() ?: "?"
    }

    // Resolve model source (URL, Local File, or Drawable Identifier)
    val imageModel: Any? = remember(avatarUrl) {
        val raw = avatarUrl?.trim()
        when {
            raw.isNullOrEmpty() -> null
            raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("content://") || raw.startsWith("file://") -> raw
            raw.startsWith("/") -> File(raw)
            else -> {
                val resName = raw.substringBeforeLast(".")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) resId else null
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                contentDescription = username,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    LetterPlaceholder(initial = initial, size = size, color = contentColor)
                },
                error = {
                    LetterPlaceholder(initial = initial, size = size, color = contentColor)
                }
            )
        } else {
            LetterPlaceholder(initial = initial, size = size, color = contentColor)
        }
    }
}

@Composable
private fun LetterPlaceholder(
    initial: String,
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            ),
            color = color,
            textAlign = TextAlign.Center
        )
    }
}