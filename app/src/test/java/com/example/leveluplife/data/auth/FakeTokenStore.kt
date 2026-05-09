package com.example.leveluplife.data.auth

internal class FakeTokenStore : TokenStore {
    private var access: String? = null
    private var refresh: String? = null

    var saveCallCount = 0
    var clearCallCount = 0

    override fun saveTokens(accessToken: String, refreshToken: String?) {
        saveCallCount++
        access = accessToken
        refresh = refreshToken
    }

    override fun accessToken(): String? = access
    override fun refreshToken(): String? = refresh

    override fun clear() {
        clearCallCount++
        access = null
        refresh = null
    }
}
