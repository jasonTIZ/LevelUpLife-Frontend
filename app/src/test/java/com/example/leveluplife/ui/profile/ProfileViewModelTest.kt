package com.example.leveluplife.ui.profile

import com.example.leveluplife.data.auth.FakeTokenStore
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.player.ProfileFetchResult
import com.example.leveluplife.domain.validation.AvatarValidator
import com.example.leveluplife.domain.validation.AvatarValidationError
import com.example.leveluplife.domain.validation.FieldError
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

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
    fun `load profile populates form fields`() = runTest(testDispatcher) {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            fetchResult = Result.success(ProfileFetchResult(profile, "\"etag-1\"")),
        )
        val cache = FakeProfileCache(initial = profile, etag = "\"etag-1\"")
        val vm = ProfileViewModel(repo, cache, testTokenStore())

        advanceUntilIdle()

        assertEquals("Aaron", vm.state.value.name)
        assertEquals("aarontest", vm.state.value.userName)
        assertFalse(vm.state.value.isLoading)
        assertEquals(1, repo.fetchCalls)
    }

    @Test
    fun `start editing enables form and cancel restores snapshot`() = runTest(testDispatcher) {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            fetchResult = Result.success(ProfileFetchResult(profile, "\"etag-1\"")),
        )
        val vm = ProfileViewModel(repo, FakeProfileCache(initial = profile), testTokenStore())
        advanceUntilIdle()

        vm.onStartEditing()
        vm.onNameChange("Changed")
        assertTrue(vm.state.value.isEditing)
        assertEquals("Changed", vm.state.value.name)

        vm.onCancelEditing()
        assertFalse(vm.state.value.isEditing)
        assertEquals("Aaron", vm.state.value.name)
    }

    @Test
    fun `username change sanitizes spaces and invalid chars`() = runTest(testDispatcher) {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            fetchResult = Result.success(ProfileFetchResult(profile, "\"etag-1\"")),
        )
        val vm = ProfileViewModel(repo, FakeProfileCache(initial = profile), testTokenStore())
        advanceUntilIdle()

        vm.onStartEditing()
        vm.onUserNameChange(" aaron dev! ")

        assertEquals("aarondev", vm.state.value.userName)
    }

    @Test
    fun `submit with invalid fields does not call API`() = runTest(testDispatcher) {
        val repo = FakeProfileRepository(
            fetchResult = Result.success(ProfileFetchResult(sampleProfile(), "\"etag-1\"")),
        )
        val vm = ProfileViewModel(repo, FakeProfileCache(initial = sampleProfile()), testTokenStore())
        advanceUntilIdle()

        vm.onStartEditing()
        vm.onNameChange("")
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals(FieldError.Required, vm.state.value.nameError)
        assertEquals(0, repo.updateCalls)
    }

    @Test
    fun `avatar too large blocks selection`() = runTest(testDispatcher) {
        val vm = ProfileViewModel(FakeProfileRepository(), FakeProfileCache(), testTokenStore())
        advanceUntilIdle()

        vm.onAvatarSelected(
            uri = "content://image/1",
            mimeType = "image/jpeg",
            sizeBytes = AvatarValidator.MAX_BYTES + 1L,
        )

        assertEquals(AvatarValidationError.TooLarge, vm.state.value.avatarError)
        assertNull(vm.state.value.avatarUri)
    }

    @Test
    fun `successful update emits saved event and updates cache`() = runTest(testDispatcher) {
        val profile = sampleProfile()
        val updated = profile.copy(name = "Aaron Updated")
        val repo = FakeProfileRepository(
            fetchResult = Result.success(ProfileFetchResult(profile, "\"etag-1\"")),
            updateResult = Result.success(updated),
        )
        val cache = FakeProfileCache(initial = profile, etag = "\"etag-1\"")
        val vm = ProfileViewModel(repo, cache, testTokenStore())
        advanceUntilIdle()

        vm.onStartEditing()
        vm.onNameChange("Aaron Updated")
        vm.onSubmit()
        advanceUntilIdle()

        assertTrue(vm.state.value.profileSaved)
        assertFalse(vm.state.value.isEditing)
        assertEquals(1, repo.updateCalls)
        assertEquals("Aaron Updated", cache.profile.value?.name)
    }

    @Test
    fun `server validation error shows inline field error without updating cache`() = runTest(testDispatcher) {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            fetchResult = Result.success(ProfileFetchResult(profile, "\"etag-1\"")),
            updateResult = Result.failure(validationException()),
        )
        val cache = FakeProfileCache(initial = profile, etag = "\"etag-1\"")
        val vm = ProfileViewModel(repo, cache, testTokenStore())
        advanceUntilIdle()

        vm.onStartEditing()
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals("Invalid email", vm.state.value.serverFieldErrors[ProfileFormField.EMAIL])
        assertEquals("Aaron", cache.profile.value?.name)
        assertFalse(vm.state.value.profileSaved)
        assertEquals(1, repo.updateCalls)
    }

    private fun testTokenStore(userId: String = "1"): TokenStore =
        FakeTokenStore().also { it.saveTokens("test-token", null, userId) }
}
