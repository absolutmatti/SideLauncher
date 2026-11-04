package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Standard TV Focusable Card / Container.
 * Scales to 1.08x on focus with 150ms easeOut, displays a 3dp focus outline in accent/white.
 * Supports D-Pad clicks (DPAD_CENTER, ENTER) and long-clicks (MENU key, long click).
 */
@Composable
fun TvFocusableSurface(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    focusBorderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 3.dp,
    focusedScale: Float = 1.08f,
    focusRequester: FocusRequester? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    var surfaceModifier = modifier
        .scale(scale)
        .zIndex(if (isFocused) 10f else 1f)

    if (focusRequester != null) {
        surfaceModifier = surfaceModifier.focusRequester(focusRequester)
    }

    surfaceModifier = surfaceModifier
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
            onFocusChanged?.invoke(focusState.isFocused)
        }
        .focusable(interactionSource = interactionSource)
        .onKeyEvent { keyEvent ->
            if (keyEvent.key == Key.Menu && onLongClick != null) {
                onLongClick()
                true
            } else if ((keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)) {
                // Let normal click handle this, but catch menu
                false
            } else {
                false
            }
        }
        .clip(shape)
        .border(
            width = if (isFocused) borderWidth else 0.dp,
            color = if (isFocused) focusBorderColor else Color.Transparent,
            shape = shape
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )

    Box(
        modifier = surfaceModifier,
        propagateMinConstraints = false
    ) {
        content(isFocused)
    }
}
