package com.example.pemmob9.model

import com.google.gson.annotations.SerializedName

/**
 * Model class representing a Todo item fetched via Retrofit & GSON.
 */
data class Todo(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("completed")
    val completed: Boolean
)
