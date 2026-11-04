package com.example.ui.overlays

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.InstalledApp
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppContextMenuOverlay(
    app: InstalledApp,
    favoriteCount: Int,
    onDismiss: () -> Unit,
    onToggleFavorite: (InstalledApp) -> Unit,
    onStartMoveMode: (InstalledApp) -> Unit,
    onHideApp: (InstalledApp) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val appIcon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(120f)
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with App Icon and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appIcon != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(appIcon).build(),
                        contentDescription = app.label,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = SurfaceElevatedDark)

            // 1. Add / Remove Favorite
            val canAddFavorite = !app.isFavorite && favoriteCount < 10
            val favText = when {
                app.isFavorite -> stringResource(R.string.apps_remove_from_fav)
                canAddFavorite -> stringResource(R.string.apps_add_to_fav)
                else -> stringResource(R.string.apps_fav_limit_reached)
            }

            ContextMenuItem(
                title = favText,
                icon = if (app.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                enabled = app.isFavorite || canAddFavorite,
                onClick = {
                    onToggleFavorite(app)
                    onDismiss()
                }
            )

            // 2. Move mode
            ContextMenuItem(
                title = "Verschieben (Reihenfolge ändern)",
                icon = Icons.Filled.OpenWith,
                onClick = {
                    onStartMoveMode(app)
                    onDismiss()
                }
            )

            // 3. App-Info
            ContextMenuItem(
                title = stringResource(R.string.apps_info),
                icon = Icons.Filled.Info,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${app.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (ignored: Exception) {}
                    onDismiss()
                }
            )

            // 4. Uninstall (only for non-system apps)
            if (!app.isSystemApp) {
                ContextMenuItem(
                    title = stringResource(R.string.apps_uninstall),
                    icon = Icons.Filled.Delete,
                    tint = DangerRed,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:${app.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (ignored: Exception) {}
                        onDismiss()
                    }
                )
            }

            // 5. Hide App
            ContextMenuItem(
                title = stringResource(R.string.apps_hide),
                icon = Icons.Filled.VisibilityOff,
                onClick = {
                    onHideApp(app)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun ContextMenuItem(
    title: String,
    icon: ImageVector,
    enabled: Boolean = true,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    TvFocusableSurface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(8.dp),
        focusedScale = 1.03f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (enabled) SurfaceElevatedDark else SurfaceDark)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (enabled) tint else Color.Gray)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) tint else Color.Gray
            )
        }
    }
}
