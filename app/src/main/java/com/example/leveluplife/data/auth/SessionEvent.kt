package com.example.leveluplife.data.auth

/**
 * Global session lifecycle signals consumed by navigation and other UI layers.
 *
 * [SESSION_LOGIN_SUCCESS] is reserved for future profile refresh hooks; login navigation
 * still uses screen-level callbacks today.
 */
enum class SessionEvent {
    SESSION_LOGIN_SUCCESS,
    SESSION_LOGOUT,
    SESSION_EXPIRED,
    SESSION_FORBIDDEN,
    SESSION_ACCOUNT_DEACTIVATED,
}
