package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.DeletePlayerAccountRequest
import com.example.leveluplife.data.network.dto.DeletePlayerAccountResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH

interface PlayerApi {
    @PATCH("api/player/delete")
    suspend fun deactivateAccount(
        @Body body: DeletePlayerAccountRequest? = null,
    ): Response<DeletePlayerAccountResponse>
}
