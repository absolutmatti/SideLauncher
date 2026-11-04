package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_metadata")
data class AppMetadata(
    @PrimaryKey
    val packageName: String,
    val isFavorite: Boolean = false,
    val favoriteOrder: Int = 0,
    val isHidden: Boolean = false,
    val lastLaunchedAt: Long = 0L,
    val customOrder: Int = 0
)

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isLeanback: Boolean,
    val isFavorite: Boolean = false,
    val favoriteOrder: Int = 0,
    val isHidden: Boolean = false,
    val lastLaunchedAt: Long = 0L,
    val customOrder: Int = 0,
    val isSystemApp: Boolean = false
)
