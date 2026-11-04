package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.local.AppMetadataDao
import com.example.data.model.AppMetadata
import com.example.data.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.Collator
import java.util.Locale

class AppsRepository(
    private val context: Context,
    private val appMetadataDao: AppMetadataDao
) {
    private val packageManager: PackageManager = context.packageManager

    // Provider to known streaming app package mappings
    val providerToPackageMap = mapOf(
        "Netflix" to "com.netflix.ninja",
        "Amazon Prime Video" to "com.amazon.amazonvideo.livingroom",
        "Prime Video" to "com.amazon.amazonvideo.livingroom",
        "Disney Plus" to "com.disney.disneyplus",
        "Disney+" to "com.disney.disneyplus",
        "Apple TV" to "com.apple.atve.androidtv.appletv",
        "Apple TV+" to "com.apple.atve.androidtv.appletv",
        "YouTube" to "com.google.android.youtube.tv",
        "Paramount Plus" to "com.cbs.app",
        "Paramount+" to "com.cbs.app",
        "HBO Max" to "com.hbo.hbonow",
        "Max" to "com.wbd.stream",
        "Crunchyroll" to "com.crunchyroll.crunchyroid",
        "WOW" to "de.sky.online.tv",
        "RTL+" to "de.rtli.tv",
        "Joyn" to "de.prosiebensat1digital.joyn",
        "ARD Mediathek" to "de.swr.ard.tv",
        "ZDFmediathek" to "de.zdf.android.mediathek.tv"
    )

    private fun queryInstalledRawApps(): List<InstalledApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val leanbackResolveInfos = packageManager.queryIntentActivities(leanbackIntent, 0)
        val standardResolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)

        val selfPackage = context.packageName
        val appsMap = mutableMapOf<String, InstalledApp>()

        // 1. Process Leanback first (higher priority for TV launcher)
        for (info in leanbackResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == selfPackage) continue
            val label = info.loadLabel(packageManager).toString()
            val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            appsMap[pkg] = InstalledApp(
                packageName = pkg,
                label = label,
                isLeanback = true,
                isSystemApp = isSystem
            )
        }

        // 2. Process standard launcher entries if not already present
        for (info in standardResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == selfPackage || appsMap.containsKey(pkg)) continue
            val label = info.loadLabel(packageManager).toString()
            val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            appsMap[pkg] = InstalledApp(
                packageName = pkg,
                label = label,
                isLeanback = false,
                isSystemApp = isSystem
            )
        }

        return appsMap.values.toList()
    }

    val installedAppsFlow: Flow<List<InstalledApp>> = combine(
        flow { emit(queryInstalledRawApps()) },
        appMetadataDao.getAllMetadataFlow()
    ) { rawApps, metadataList ->
        val metaMap = metadataList.associateBy { it.packageName }
        rawApps.map { app ->
            val meta = metaMap[app.packageName]
            if (meta != null) {
                app.copy(
                    isFavorite = meta.isFavorite,
                    favoriteOrder = meta.favoriteOrder,
                    isHidden = meta.isHidden,
                    lastLaunchedAt = meta.lastLaunchedAt,
                    customOrder = meta.customOrder
                )
            } else {
                app
            }
        }
    }.flowOn(Dispatchers.IO)

    fun sortApps(apps: List<InstalledApp>, sortMode: String): List<InstalledApp> {
        val collator = Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }
        return when (sortMode) {
            "LAST_USED" -> {
                apps.sortedWith { a, b ->
                    if (a.lastLaunchedAt != b.lastLaunchedAt) {
                        b.lastLaunchedAt.compareTo(a.lastLaunchedAt)
                    } else {
                        collator.compare(a.label, b.label)
                    }
                }
            }
            "CUSTOM" -> {
                apps.sortedWith { a, b ->
                    if (a.customOrder != b.customOrder) {
                        a.customOrder.compareTo(b.customOrder)
                    } else {
                        collator.compare(a.label, b.label)
                    }
                }
            }
            else -> { // "AZ"
                apps.sortedWith { a, b -> collator.compare(a.label, b.label) }
            }
        }
    }

    suspend fun setFavorite(packageName: String, isFavorite: Boolean) {
        val meta = appMetadataDao.getByPackage(packageName) ?: AppMetadata(packageName = packageName)
        val allMeta = appMetadataDao.getAllMetadataFlow()
        val currentFavCount = meta.isFavorite
        appMetadataDao.insert(
            meta.copy(
                isFavorite = isFavorite,
                favoriteOrder = if (isFavorite && !currentFavCount) System.currentTimeMillis().toInt() else meta.favoriteOrder
            )
        )
    }

    suspend fun updateFavoriteOrder(orderedPackageNames: List<String>) {
        orderedPackageNames.forEachIndexed { index, pkg ->
            val meta = appMetadataDao.getByPackage(pkg) ?: AppMetadata(packageName = pkg, isFavorite = true)
            appMetadataDao.insert(meta.copy(favoriteOrder = index))
        }
    }

    suspend fun setHidden(packageName: String, isHidden: Boolean) {
        val meta = appMetadataDao.getByPackage(packageName) ?: AppMetadata(packageName = packageName)
        appMetadataDao.insert(meta.copy(isHidden = isHidden))
    }

    suspend fun recordAppLaunch(packageName: String) {
        val meta = appMetadataDao.getByPackage(packageName) ?: AppMetadata(packageName = packageName)
        appMetadataDao.insert(meta.copy(lastLaunchedAt = System.currentTimeMillis()))
    }

    suspend fun updateCustomOrder(orderedPackageNames: List<String>) {
        orderedPackageNames.forEachIndexed { index, pkg ->
            val meta = appMetadataDao.getByPackage(pkg) ?: AppMetadata(packageName = pkg)
            appMetadataDao.insert(meta.copy(customOrder = index))
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val leanbackIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
            val launchIntent = leanbackIntent ?: packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
