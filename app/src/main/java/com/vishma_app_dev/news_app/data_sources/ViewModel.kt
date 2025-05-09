package com.vishma_app_dev.news_app.data_sources

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishma_app_dev.news_app.favourites.FavNewsEntity
import com.vishma_app_dev.news_app.favourites.Newsdb
import com.vishma_app_dev.news_app.network.Article
import com.vishma_app_dev.news_app.network.Source
import com.vishma_app_dev.news_app.network.newsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ViewModel1(context: Context) : ViewModel() {
    private val dao = Newsdb.getInstance(context)?.newsDao()

    companion object {
        // TODO: move to BuildConfig or secure storage
        private const val API_KEY = "340ee4a3e0054205a3f585058563f16b"
    }

    // News feed
    private val _news = MutableStateFlow<List<Article>>(emptyList())
    val news: StateFlow<List<Article>> = _news

    fun getAllNews(page: Int = 1) {
        viewModelScope.launch {
            try {
                val resp = newsService.getNews(API_KEY, page)
                _news.value = resp.articles
            } catch (e: Exception) {
                Log.e("ViewModel1", "Error fetching all news", e)
                _news.value = listOf(
                    Article(Source("Error"), "Error", "Error", "Error", "Error", null)
                )
            }
        }
    }

    fun getNewsByCategory(category: String, page: Int = 1) {
        viewModelScope.launch {
            try {
                val resp = newsService.getNewsByCategory(API_KEY, category, page)
                _news.value = resp.articles
            } catch (e: Exception) {
                Log.e("ViewModel1", "Error fetching news for $category", e)
                _news.value = listOf(
                    Article(Source("Error"), "Error", "Error", "Error", "Error", null)
                )
            }
        }
    }

    fun search(term:String){
        viewModelScope.launch {
            try {
                val resp = newsService.findNews(API_KEY, term)
                _news.value = resp.articles
            } catch (e: Exception) {
                Log.e("ViewModel1", "Error fetching all news", e)
                _news.value = listOf(
                    Article(Source("Error"), "Error", "Error", "Error", "Error", null)
                )
            }
        }
    }
    fun refresh(){
        _news.value=emptyList()
    }

    // CRUD on favourites
    fun insert(fav: FavNewsEntity) = viewModelScope.launch { dao?.insert(fav) }
    fun delete(fav: FavNewsEntity) = viewModelScope.launch { dao?.delete(fav) }
    fun update(fav: FavNewsEntity) = viewModelScope.launch { dao?.update(fav) }

    fun getAllFavorites(): Flow<List<FavNewsEntity>> =
        dao?.getAllFavNews() ?: flowOf(emptyList())


    fun isFavorite(url: String): Flow<Boolean> =
        dao
            ?.getByUrl(url)
            ?.map { it != null }
            ?: flowOf(false)
}
