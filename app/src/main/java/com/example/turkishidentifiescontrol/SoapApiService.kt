package com.example.turkishidentifiescontrol

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SoapApiService {
    @Headers(
        "Content-Type: application/soap+xml; charset=utf-8",
        "SOAPAction: \"http://tckimlik.nvi.gov.tr/WS/TCKimlikNoDogrula\""
    )
    @POST("Service/KPSPublic.asmx")
    suspend fun verifyIdentity(@Body requestBody: RequestBody): Response<String>
}