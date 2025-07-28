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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetInstalledProtonAppsTest {

    private val pm = mockk<PackageManager>()
    private val context = mockk<Context> {
        every { packageManager } returns pm
    }
    private lateinit var sut: GetInstalledProtonApps

    @Before
    fun setUp() {
        sut = GetInstalledProtonApps(context)
    }

    @Test
    fun `returns empty list when no Proton apps are installed`() {
        // Given
        mockInstalled()

        // When
        val result = sut()

        // Then
        assertTrue(result.appsAndVersions.isEmpty())
    }

    @Test
    fun `returns only VPN when only VPN is installed`() {
        // Given
        mockInstalled("ch.protonvpn.android" to "v2")

        // When
        val result = sut()

        // Then
        assertEquals(
            listOf(
                InstalledProtonApps.AppInfo(
                    packageName = "ch.protonvpn.android",
                    version = "v2"
                )
            ),
            result.appsAndVersions
        )
    }

    @Test
    fun `returns only Drive when only Drive is installed`() {
        // Given
        mockInstalled("me.proton.android.drive" to "v1")

        // When
        val result = sut()

        // Then
        assertEquals(
            listOf(
                InstalledProtonApps.AppInfo(
                    packageName = "me.proton.android.drive",
                    version = "v1"
                )
            ),
            result.appsAndVersions
        )
    }

    @Test
    fun `returns only Calendar when only Calendar is installed`() {
        // Given
        mockInstalled("me.proton.android.calendar" to "2.27.0")

        // When
        val result = sut()

        // Then
        assertEquals(
            listOf(
                InstalledProtonApps.AppInfo(
                    packageName = "me.proton.android.calendar",
                    version = "2.27.0"
                )
            ),
            result.appsAndVersions
        )
    }

    @Test
    fun `returns all Proton apps when all are installed`() {
        // Given
        val all = listOf(
            "ch.protonvpn.android" to "v1",
            "me.proton.android.drive" to "v2",
            "me.proton.android.calendar" to "v3",
            "proton.android.pass" to "v4",
            "me.proton.wallet.android" to "v5",
            "me.proton.android.lumo" to "v6"
        )
        mockInstalled(*all.toTypedArray())

        // When
        val result = sut()

        // Then
        val expected = all.map { InstalledProtonApps.AppInfo(it.first, it.second) }
        assertEquals(expected, result.appsAndVersions)
    }

    private fun mockInstalled(vararg installed: Pair<String, String>) {
        every { pm.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        installed.forEach { (pkg, pkgVersion) ->
            val pi = PackageInfo().apply { versionName = pkgVersion }
            every { pm.getPackageInfo(pkg, 0) } returns pi
        }
    }
}
