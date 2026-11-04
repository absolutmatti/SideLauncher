package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.ui.navigation.Destination
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TvSidebar(
    currentDestination: Destination,
    onDestinationSelected: (Destination) -> Unit,
    autoCollapse: Boolean,
    onSidebarFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var hasFocusInSidebar by remember { mutableStateOf(false) }

    val isExpanded = if (autoCollapse) hasFocusInSidebar else true
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 260.dp else 80.dp,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_width"
    )

    Box(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .zIndex(50f)
            .background(SurfaceDark)
            .onFocusChanged { focusState ->
                val focused = focusState.hasFocus
                hasFocusInSidebar = focused
                onSidebarFocusChanged(focused)
            }
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Home, Apps, Watchlist)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // App Logo / Header in Sidebar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SL",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    if (isExpanded) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SidebarItem(
                    destination = Destination.HOME,
                    label = stringResource(R.string.nav_home),
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    isSelected = currentDestination == Destination.HOME,
                    isExpanded = isExpanded,
                    onClick = { onDestinationSelected(Destination.HOME) }
                )

                SidebarItem(
                    destination = Destination.APPS,
                    label = stringResource(R.string.nav_apps),
                    selectedIcon = Icons.Filled.Apps,
                    unselectedIcon = Icons.Outlined.Apps,
                    isSelected = currentDestination == Destination.APPS,
                    onClick = { onDestinationSelected(Destination.APPS) },
                    isExpanded = isExpanded
                )

                SidebarItem(
                    destination = Destination.WATCHLIST,
                    label = stringResource(R.string.nav_watchlist),
                    selectedIcon = Icons.Filled.Bookmark,
                    unselectedIcon = Icons.Outlined.BookmarkBorder,
                    isSelected = currentDestination == Destination.WATCHLIST,
                    onClick = { onDestinationSelected(Destination.WATCHLIST) },
                    isExpanded = isExpanded
                )
            }

            // Bottom Section (Settings)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SidebarItem(
                    destination = Destination.SETTINGS,
                    label = stringResource(R.string.nav_settings),
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    isSelected = currentDestination == Destination.SETTINGS,
                    onClick = { onDestinationSelected(Destination.SETTINGS) },
                    isExpanded = isExpanded
                )
            }
        }
    }
}

@Composable
private fun SidebarItem(
    destination: Destination,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    TvFocusableSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        focusedScale = 1.02f,
        borderWidth = 2.dp,
        focusBorderColor = MaterialTheme.colorScheme.primary,
        onFocusChanged = { focused ->
            isFocused = focused
            if (focused) {
                // Instantly switch destination preview on focus according to specification
                onClick()
            }
        }
    ) {
        val backgroundColor = when {
            isFocused -> SurfaceElevatedDark
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else -> Color.Transparent
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 14.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar indicator on left if active
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isFocused -> TextPrimary
                    else -> TextMuted
                },
                modifier = Modifier.size(26.dp)
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isSelected -> TextPrimary
                        isFocused -> TextPrimary
                        else -> TextSecondary
                    },
                    maxLines = 1
                )
            }
        }
    }
}
