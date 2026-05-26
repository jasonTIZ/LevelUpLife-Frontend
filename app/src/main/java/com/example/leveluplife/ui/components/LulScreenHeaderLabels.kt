package com.example.leveluplife.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LulScreenHeaderLabels(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    titleFontWeight: FontWeight = FontWeight.Bold,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    titleModifier: Modifier = Modifier.semantics { heading() },
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    subtitleModifier: Modifier = Modifier,
    gapAfterTitle: Dp = 4.dp,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = titleTextStyle,
            fontWeight = titleFontWeight,
            color = titleColor,
            modifier = titleModifier,
        )
        Spacer(Modifier.height(gapAfterTitle))
        Text(
            text = subtitle,
            style = subtitleTextStyle,
            color = subtitleColor,
            modifier = subtitleModifier,
        )
    }
}
