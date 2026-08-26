package com.example.checkpoint.ui.sections.detail.components.bottomSection.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.Review

/**
 * Reviews tab combining the interactive personal review editor and the list of community reviews.
 */
@Composable
fun ReviewsTabContent(
    game: Game,
    communityReviews: List<Review>,
    isLoadingCommunityReviews: Boolean,
    onSaveReview: (rating: Float, reviewText: String) -> Unit,
    onDeleteReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Personal review editor card
        UserReviewEditorCard(
            initialRating = game.rating,
            initialReviewText = game.userReview,
            onSave = onSaveReview,
            onDelete = onDeleteReview
        )

        // Community reviews header
        Text(
            text = "Recensioni della Community",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        when {
            isLoadingCommunityReviews -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
            communityReviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun altro utente ha ancora recensito questo titolo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    communityReviews.forEach { review ->
                        val itemKey = review.userId.ifEmpty { "${review.username}_${review.timestamp}" }
                        key(itemKey) {
                            CommunityReviewItemCard(review = review)
                        }
                    }
                }
            }
        }
    }
}