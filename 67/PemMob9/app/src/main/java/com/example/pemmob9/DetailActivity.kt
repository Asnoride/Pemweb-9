package com.example.pemmob9

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pemmob9.api.ApiService
import com.example.pemmob9.api.RetrofitClient
import com.example.pemmob9.databinding.ActivityDetailBinding
import com.example.pemmob9.model.Todo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Detail Activity displaying full information for a selected Todo item by ID.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var todoId: Int = -1
    private var isUsingFallback: Boolean = false

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
        const val EXTRA_USE_FALLBACK = "extra_use_fallback"
        private const val FALLBACK_BASE_URL = "https://jsonplaceholder.typicode.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoId = intent.getIntExtra(EXTRA_TODO_ID, -1)
        isUsingFallback = intent.getBooleanExtra(EXTRA_USE_FALLBACK, false)

        setupToolbar()
        setupListeners()
        fetchDetailData()
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupListeners() {
        binding.btnDetailRetry.setOnClickListener {
            fetchDetailData()
        }
    }

    private fun fetchDetailData() {
        if (todoId == -1) {
            showErrorState("Invalid Todo ID")
            return
        }

        showLoadingState()

        val apiService = if (isUsingFallback) {
            Retrofit.Builder()
                .baseUrl(FALLBACK_BASE_URL)
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        } else {
            RetrofitClient.getApiService()
        }

        apiService.getTodoById(todoId).enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful && response.body() != null) {
                    showSuccessState(response.body()!!)
                } else {
                    showErrorState("Server error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Todo>, t: Throwable) {
                showErrorState(t.localizedMessage ?: "Failed to connect to server")
            }
        })
    }

    private fun showLoadingState() {
        binding.layoutDetailLoading.visibility = View.VISIBLE
        binding.layoutDetailError.visibility = View.GONE
        binding.cardDetailContainer.visibility = View.GONE
    }

    private fun showSuccessState(todo: Todo) {
        binding.layoutDetailLoading.visibility = View.GONE
        binding.layoutDetailError.visibility = View.GONE
        binding.cardDetailContainer.visibility = View.VISIBLE

        binding.tvDetailId.text = getString(R.string.task_id_label, todo.id)
        binding.tvDetailTitle.text = todo.title
        binding.tvDetailUserId.text = todo.userId.toString()

        val baseUrl = if (isUsingFallback) FALLBACK_BASE_URL else RetrofitClient.BASE_URL
        binding.tvDetailEndpoint.text = "GET ${baseUrl}todos/${todo.id}"

        if (todo.completed) {
            binding.tvDetailStatus.text = getString(R.string.completed)
            binding.tvDetailStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_completed)
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text))
        } else {
            binding.tvDetailStatus.text = getString(R.string.pending)
            binding.tvDetailStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_pending)
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending_text))
        }
    }

    private fun showErrorState(errorMessage: String) {
        binding.layoutDetailLoading.visibility = View.GONE
        binding.cardDetailContainer.visibility = View.GONE
        binding.layoutDetailError.visibility = View.VISIBLE
        binding.tvDetailError.text = errorMessage
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
