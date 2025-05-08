package com.vishma_app_dev.news_app.network

import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

data class Response(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val source: Source,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?
)

data class Source(
    val name: String?
)


interface newsApi {

//    ALL NEWS
    @GET("/v2/everything")
    suspend fun getNews(
        @Query("apiKey") api_key:String,
        @Query("page") page:Int=1
    ):Response

//    TOP HEADLINES
    @GET("/v2/top-headlines")
    suspend fun getNewsByCategory(
        @Query("apiKey") api_key: String,
        @Query("category") category: String,
        @Query("page") page: Int=1
    ):Response

    //      SEARCH RESULTS
    @GET("/v2/everything")
    suspend fun findNews(
        @Query("apiKey") api_key: String,
        @Query("q") query:String
    ):Response
}

val retrofit  = Retrofit.Builder().baseUrl("https://newsapi.org/")
    .addConverterFactory(GsonConverterFactory.create()).build()

var newsService= retrofit.create(newsApi::class.java)