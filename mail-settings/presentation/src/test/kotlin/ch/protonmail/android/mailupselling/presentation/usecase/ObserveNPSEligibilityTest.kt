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

package ch.protonmail.android.mailupselling.presentation.usecase

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.MailFeatureId
import ch.protonmail.android.mailcommon.domain.sample.UserSample
import ch.protonmail.android.mailcommon.domain.usecase.ObserveMailFeature
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.featureflag.domain.entity.FeatureFlag
import org.junit.Test
import javax.inject.Provider
import kotlin.test.assertEquals

internal class ObserveNPSEligibilityTest {

    private val observePrimaryUser = mockk<ObservePrimaryUser>()
    private val observeMailFeature = mockk<ObserveMailFeature>()
    private val npsEnabled = mockk<Provider<Boolean>>()

    private val sut: ObserveNPSEligibility
        get() = ObserveNPSEligibility(
            observePrimaryUser,
            observeMailFeature,
            npsEnabled.get()
        )

    private val user = UserSample.Primary

    @Test
    fun `should emit false when primary user is null`() = runTest {
        every { observePrimaryUser() } returns flowOf(null)
        expectNPSEnabled(true)
        sut().test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should emit false when FF disabled`() = runTest {
        every { observePrimaryUser() } returns flowOf(user)
        expectNPSEnabled(false)
        sut().test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should emit false when per-user feature flag is disabled`() = runTest {
        every { observePrimaryUser() } returns flowOf(user)
        expectPerUserFlagEnabled(false)
        expectNPSEnabled(true)

        sut().test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `should emit true when feature enabled`() = runTest {
        every { observePrimaryUser() } returns flowOf(user)
        expectPerUserFlagEnabled(true)
        expectNPSEnabled(true)

        sut().test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }

    private fun expectPerUserFlagEnabled(enabled: Boolean) {
        every { observeMailFeature(user.userId, MailFeatureId.NPSFeedback) } returns
            flowOf(FeatureFlag.default("ff1", defaultValue = enabled))
    }

    private fun expectNPSEnabled(value: Boolean) {
        every { npsEnabled.get() } returns value
    }
}
