package com.example.cinemacth.data.model

import com.google.gson.annotations.SerializedName

data class Cast(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?
) {
    val fullProfilePath: String?
        get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

data class CreditsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("cast") val cast: List<Cast>
)
