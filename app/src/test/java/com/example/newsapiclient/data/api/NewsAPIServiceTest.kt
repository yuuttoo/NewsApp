package com.example.newsapiclient.data.api

import android.util.Log
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsAPIServiceTest {
    private lateinit var service: NewsAPIService
    private lateinit var server: MockWebServer


    @Before
    fun setUp(){
        server = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(server.url(""))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }

    //模仿的http回覆
    private fun enqueueMockResponse(
        fileName:String
    ){
        val inputStream = javaClass.classLoader!!.getResourceAsStream(fileName)
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        mockResponse.setBody(source.readString(Charsets.UTF_8))
        server.enqueue(mockResponse)

    }


    @Test
    fun getTopHeadlines_sentRequest_receivedExpected(){
       runBlocking {
           enqueueMockResponse("newsresponse.json")
           val responseBody = service.getTopHeadlines("us",1).body()
           val request = server.takeRequest()
           //Log.i("TAG",request.path.toString())
           assertThat(responseBody).isNotNull()
           assertThat(request.path).isEqualTo("/v2/top-headlines?country=us&page=1&apiKey=40d8a4b5408b4152b543c7bbe8893055")
       }
    }

    @Test
    fun getTopHeadlines_receivedResponse_correctPageSize(){
        runBlocking {
            enqueueMockResponse("newsresponse.json")
            val responseBody = service.getTopHeadlines("us",1).body()
            val articleList = responseBody!!.articles
            assertThat(articleList.size).isEqualTo(20)
        }
    }


    @Test
    fun getTopHeadlines_receivedResponse_correctContent(){
        runBlocking {
            enqueueMockResponse("newsresponse.json")
            val responseBody = service.getTopHeadlines("us",1).body()
            val articleList = responseBody!!.articles
            val article = articleList[0]
            assertThat(article.author).isEqualTo("Jonathan Ayestas")
            assertThat(article.url).isEqualTo("https://www.kcra.com/article/sac-city-unified-students-return-class-strike-ends/39621519")
            assertThat(article.publishedAt).isEqualTo("2022-04-04T05:38:00Z")
        }
    }


    @After
    fun tearDown(){
        server.shutdown()//結束測試後 關閉server 這邊是關閉json檔案

    }


}