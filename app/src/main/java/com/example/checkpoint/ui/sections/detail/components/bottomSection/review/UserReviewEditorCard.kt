package com.example.checkpoint.ui.sections.detail.components.bottomSection.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Interactive card allowing users to rate a game, write or edit a personal review, and delete it.
 */
@Composable
fun UserReviewEditorCard(
    initialRating: Float,
    initialReviewText: String,
    onSave: (rating: Float, reviewText: String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputRating by remember(initialRating) {
        mutableFloatStateOf(if (initialRating > 0f) initialRating else 1f)
    }
    var inputText by remember(initialReviewText) {
        mutableStateOf(initialReviewText)
    }

    val hasUserReview = initialRating > 0f || initialReviewText.isNotBlank()
    val isFormDirty = inputRating != initialRating || inputText != initialReviewText

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card Title
            Text(
                text = if (hasUserReview) "LA TUA RECENSIONE" else "LASCIA UNA RECENSIONE",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Interactive 5-star rating selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        val isSelected = starIndex <= inputRating.toInt()
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { inputRating = starIndex.toFloat() }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Voto $starIndex",
                                tint = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Voto: ${inputRating.toInt()} / 5",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Review text input
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Cosa ne pensi del gioco?") },
                placeholder = { Text("Condividi la tua opinione con la community...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (hasUserReview) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasUserReview) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Elimina")
                    }
                }

                Button(
                    onClick = { onSave(inputRating, inputText.trim()) },
                    enabled = isFormDirty || !hasUserReview,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (hasUserReview) "Aggiorna" else "Pubblica")
                }
            }
        }
    }
}