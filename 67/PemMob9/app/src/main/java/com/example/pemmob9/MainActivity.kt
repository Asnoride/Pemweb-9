package com.example.pemmob9

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pemmob9.adapter.TodoAdapter
import com.example.pemmob9.api.ApiService
import com.example.pemmob9.api.RetrofitClient
import com.example.pemmob9.databinding.ActivityMainBinding
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
 * Main Activity featuring RecyclerView with Retrofit 2.9.0, GSON, custom animations,
 * swipe-to-refresh, animated loading state, and automated retry logic.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TodoAdapter
    private val mainHandler = Handler(Looper.getMainLooper())

    private var retryAttempt = 0
    private val maxAutoRetries = 3
    private var isUsingFallbackApi = false

    companion object {
        // Fallback public endpoint if local 10.0.2.2:8000 is not running
        private const val FALLBACK_BASE_URL = "https://jsonplaceholder.typicode.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        fetchTodoList()
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter { todo ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_TODO_ID, todo.id)
                putExtra(DetailActivity.EXTRA_USE_FALLBACK, isUsingFallbackApi)
            }
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            retryAttempt = 0
            fetchTodoList()
        }

        binding.btnRetry.setOnClickListener {
            retryAttempt = 0
            fetchTodoList()
        }
    }

    private fun fetchTodoList() {
        showLoadingState()

        val apiService = getActiveApiService()
        apiService.getTodos().enqueue(object : Callback<List<Todo>> {
            override fun onResponse(call: Call<List<Todo>>, response: Response<List<Todo>>) {
                binding.swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful && response.body() != null) {
                    retryAttempt = 0
                    val todoList = response.body()!!
                    showSuccessState(todoList)
                } else {
                    handleFetchError("Server returned code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Todo>>, t: Throwable) {
                binding.swipeRefreshLayout.isRefreshing = false
                handleFetchError(t.localizedMessage ?: "Network connection failed")
            }
        })
    }

    private fun getActiveApiService(): ApiService {
        return if (isUsingFallbackApi) {
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
    }

    private fun handleFetchError(errorMessage: String) {
        if (retryAttempt < maxAutoRetries) {
            retryAttempt++
            showAutoRetryState(errorMessage)
            mainHandler.postDelayed({
                fetchTodoList()
            }, 3000)
        } else if (!isUsingFallbackApi) {
            // Switch to fallback JSONPlaceholder if local emulator server (10.0.2.2:8000) is unreachable
            isUsingFallbackApi = true
            retryAttempt = 0
            fetchTodoList()
        } else {
            showErrorState(errorMessage)
        }
    }

    private fun showLoadingState() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.swipeRefreshLayout.visibility = View.VISIBLE
    }

    private fun showSuccessState(todos: List<Todo>) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.swipeRefreshLayout.visibility = View.VISIBLE

        adapter.submitList(todos)

        // Trigger smooth entrance animation
        val resId = R.anim.layout_animation_slide_up
        val animation = AnimationUtils.loadLayoutAnimation(this, resId)
        binding.recyclerView.layoutAnimation = animation
        binding.recyclerView.scheduleLayoutAnimation()
    }

    private fun showAutoRetryState(errorMessage: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = errorMessage.ifEmpty { getString(R.string.error_connection) }
        binding.tvAutoRetryMessage.visibility = View.VISIBLE
        binding.tvAutoRetryMessage.text = getString(
            R.string.auto_retrying,
            3,
            retryAttempt,
            maxAutoRetries
        )
        binding.btnRetry.isEnabled = false
    }

    private fun showErrorState(errorMessage: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = errorMessage
        binding.tvAutoRetryMessage.visibility = View.GONE
        binding.btnRetry.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
