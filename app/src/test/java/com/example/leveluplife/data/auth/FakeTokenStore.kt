package com.example.leveluplife.data.auth

internal class FakeTokenStore : TokenStore {
    private var access: String? = null
    private var refresh: String? = null
    private var userId: String? = null

    var saveCallCount = 0
    var clearCallCount = 0

    override fun saveTokens(accessToken: String, refreshToken: String?, userId: String?) {
        saveCallCount++
        access = accessToken
        refresh = refreshToken
        this.userId = userId
    }

    override fun accessToken(): String? = access
    override fun refreshToken(): String? = refresh
    override fun userId(): String? = userId

    override fun clear() {
        clearCallCount++
        access = null
        refresh = null
        userId = null
    }
}
