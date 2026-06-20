package com.example.leveluplife.ui.rewards

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.leveluplife.R

val GoldColor = Color(0xFFF59E0B)

@DrawableRes
fun drawableForItemId(id: Int): Int? = when (id) {
    1 -> R.drawable.escudo_1
    2 -> R.drawable.escudo_2
    3 -> R.drawable.escudo_3
    4 -> R.drawable.potenciacion_1
    5 -> R.drawable.potenciacion_2
    6 -> R.drawable.potenciacion_3
    7 -> R.drawable.mejora_1
    8 -> R.drawable.mejora_2
    9 -> R.drawable.mejora_3
    10 -> R.drawable.mejora_4
    else -> null
}

fun iconForTypeId(typeId: Int?): ImageVector = when (typeId) {
    1 -> Icons.Filled.Shield
    2 -> Icons.Filled.Bolt
    3 -> Icons.Filled.AddCircle
    4 -> Icons.Filled.Healing
    else -> Icons.Filled.Star
}
