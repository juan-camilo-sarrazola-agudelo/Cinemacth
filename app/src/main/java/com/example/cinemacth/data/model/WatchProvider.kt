package com.example.cinemacth.data.model

import com.google.gson.annotations.SerializedName

data class WatchProvider(
    @SerializedName("provider_id") val providerId: Int,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("logo_path") val logoPath: String?
) {
    val fullLogoPath: String?
        get() = logoPath?.let { "https://image.tmdb.org/t/p/w92$it" }
}

data class WatchProviderResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("results") val results: Map<String, WatchProviderCountry>
)

data class WatchProviderCountry(
    @SerializedName("flatrate") val flatrate: List<WatchProvider>? = null,
    @SerializedName("buy") val buy: List<WatchProvider>? = null,
    @SerializedName("rent") val rent: List<WatchProvider>? = null
)
