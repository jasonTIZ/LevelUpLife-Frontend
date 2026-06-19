package com.example.leveluplife.ui.settings

import com.example.leveluplife.data.player.DeactivateAccountResult
import com.example.leveluplife.data.player.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cancel deactivation closes dialog without API call`() = runTest(testDispatcher) {
        val repo = FakePlayerRepository()
        val vm = SettingsViewModel(repo)

        vm.onRequestDeactivate()
        vm.onCancelDeactivate()

        advanceUntilIdle()

        assertFalse(vm.state.value.showConfirmDialog)
        assertEquals(0, repo.deactivateCalls)
    }

    @Test
    fun `confirm without acknowledgement does not call repository`() = runTest(testDispatcher) {
        val repo = FakePlayerRepository()
        val vm = SettingsViewModel(repo)

        vm.onRequestDeactivate()
        vm.confirmDeactivate()

        advanceUntilIdle()

        assertEquals(0, repo.deactivateCalls)
    }

    @Test
    fun `successful deactivation closes dialog`() = runTest(testDispatcher) {
        val repo = FakePlayerRepository(
            result = Result.success(
                DeactivateAccountResult(
                    message = "Cuenta desactivada correctamente",
                    deactivatedAt = "2026-05-29T00:00:00Z",
                ),
            ),
        )
        val vm = SettingsViewModel(repo)

        vm.onRequestDeactivate()
        vm.onConsequencesAcknowledgedChange(true)
        vm.confirmDeactivate()

        advanceUntilIdle()

        assertEquals(1, repo.deactivateCalls)
        assertFalse(vm.state.value.showConfirmDialog)
        assertFalse(vm.state.value.isDeactivating)
    }

    @Test
    fun `failed deactivation shows error and keeps dialog open`() = runTest(testDispatcher) {
        val repo = FakePlayerRepository(
            result = Result.failure(Exception("Sin conexión")),
        )
        val vm = SettingsViewModel(repo)

        vm.onRequestDeactivate()
        vm.onConsequencesAcknowledgedChange(true)
        vm.confirmDeactivate()

        advanceUntilIdle()

        assertEquals(1, repo.deactivateCalls)
        assertTrue(vm.state.value.showConfirmDialog)
        assertTrue(vm.state.value.errorMessage?.isNotBlank() == true)
    }

    private class FakePlayerRepository(
        private val result: Result<DeactivateAccountResult> = Result.success(
            DeactivateAccountResult("OK", ""),
        ),
    ) : PlayerRepository {
        var deactivateCalls = 0

        override suspend fun deactivateAccount(reason: String?): Result<DeactivateAccountResult> {
            deactivateCalls++
            return result
        }
    }
}
