package com.example.leveluplife.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LulPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    shape: Shape = RoundedCornerShape(28.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    ),
    buttonHeight: Dp = 56.dp,
    trailingIcon: ImageVector = Icons.Outlined.ArrowForward,
    textFontWeight: FontWeight = FontWeight.Bold,
    textLetterSpacing: TextUnit = 1.sp,
    loadingIndicatorColor: Color = MaterialTheme.colorScheme.onPrimary,
    loadingStrokeWidth: Dp = 2.5.dp,
    loadingIndicatorSize: Dp = 22.dp,
    loadingTestTag: String? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = loadingIndicatorColor,
                strokeWidth = loadingStrokeWidth,
                modifier = Modifier
                    .size(loadingIndicatorSize)
                    .then(
                        if (loadingTestTag != null) Modifier.testTag(loadingTestTag) else Modifier,
                    ),
            )
        } else {
            Text(
                text = text,
                fontWeight = textFontWeight,
                letterSpacing = textLetterSpacing,
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
            )
        }
    }
}
