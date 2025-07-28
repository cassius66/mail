/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mailupselling.domain.repository

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetInstalledProtonApps @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    operator fun invoke(): InstalledProtonApps {
        val installed = ProtonPackages
            .mapNotNull { queryInstalledAppInfo(it, appContext.packageManager) }

        return InstalledProtonApps(appsAndVersions = installed)
    }
}

@Suppress("SwallowedException")
private fun queryInstalledAppInfo(packageName: String, pm: PackageManager): InstalledProtonApps.AppInfo? = try {
    val info = pm.getPackageInfo(packageName, 0)
    InstalledProtonApps.AppInfo(packageName = packageName, version = info.versionName.orEmpty())
} catch (_: PackageManager.NameNotFoundException) {
    null // not installed
}

data class InstalledProtonApps(
    val appsAndVersions: List<AppInfo>
) {
    data class AppInfo(
        val packageName: String,
        val version: String
    )
}

private val ProtonPackages = listOf(
    "ch.protonvpn.android",
    "me.proton.android.drive",
    "me.proton.android.calendar",
    "proton.android.pass",
    "me.proton.wallet.android",
    "me.proton.android.lumo"
)
