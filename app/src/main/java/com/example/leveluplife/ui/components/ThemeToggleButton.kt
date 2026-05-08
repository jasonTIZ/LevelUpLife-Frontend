package com.example.leveluplife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import com.example.leveluplife.ui.theme.ThemeController
import com.example.leveluplife.ui.theme.ThemeMode

@Composable
fun ThemeToggleButton(
    controller: ThemeController,
    modifier: Modifier = Modifier,
) {
    val mode by controller.mode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkEffective = when (mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val cdRes = if (isDarkEffective) R.string.cd_switch_to_light else R.string.cd_switch_to_dark

    IconButton(
        onClick = { controller.toggle(systemDark) },
        modifier = modifier
            .size(44.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Icon(
            imageVector = if (isDarkEffective) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription = stringResource(id = cdRes),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
