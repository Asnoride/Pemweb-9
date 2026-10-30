package com.example.pemmob9.api

import com.example.pemmob9.model.Todo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit 2.9.0 API Service interface defining endpoints for fetching list and item by ID.
 */
interface ApiService {

    /**
     * Endpoint 1: Fetches the list of todos.
     */
    @GET("todos")
    fun getTodos(): Call<List<Todo>>

    /**
     * Endpoint 2: Fetches a specific todo item by its ID.
     */
    @GET("todos/{id}")
    fun getTodoById(@Path("id") id: Int): Call<Todo>
}
