package com.example.checkpoint.ui.sections.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.AppSettings
import com.example.checkpoint.data.model.PrivacyLevel

/**
 * Card container for configuring profile and library visibility settings.
 */
@Composable
fun PrivacySettingsCard(
    settings: AppSettings,
    onStatsPrivacyChange: (PrivacyLevel) -> Unit,
    onBadgesPrivacyChange: (PrivacyLevel) -> Unit,
    onLibraryPrivacyChange: (PrivacyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "PRIVACY & VISIBILITÀ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Stats visibility
            PrivacyDropdownRow(
                label = "Mostra Statistiche Profilo",
                currentValue = settings.showStatsPrivacy,
                onValueSelected = onStatsPrivacyChange
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Badges visibility
            PrivacyDropdownRow(
                label = "Mostra Badge Profilo",
                currentValue = settings.showBadgesPrivacy,
                onValueSelected = onBadgesPrivacyChange
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Library visibility
            PrivacyDropdownRow(
                label = "Mostra Libreria Personale",
                currentValue = settings.showLibraryPrivacy,
                onValueSelected = onLibraryPrivacyChange
            )
        }
    }
}