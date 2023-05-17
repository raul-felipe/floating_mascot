package com.example.floatingmascot

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class WeatherManager(var latlong:String, var context: Context) {

    private var gifMap = GifMap()
    var baseUrlAPI = "https://api.weatherapi.com/v1/"
    //var keyAPI = "90fbafa29ed44a07b2570623230805"

    /** Retorna uma Instância do Client Retrofit para Requisições
     * @param path Caminho Principal da API
     */
    fun getRetrofitInstance(path : String) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(path)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun getData(highTemperature:Int,lowTemperature:Int, floatingService: FloatingService) {
        val retrofitClient = getRetrofitInstance(baseUrlAPI)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.getPosts(latlong)

        callback.enqueue(object : Callback<WeatherClientResponse> {
            override fun onFailure(call: Call<WeatherClientResponse>, t: Throwable) {
                Log.d("CREATED", t.message!!)
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<WeatherClientResponse>,
                response: retrofit2.Response<WeatherClientResponse>
            ) {
                Log.d("CREATED", response.body().toString())
                var actualTemperature = response.body()!!.current.temp_c
                Toast.makeText(context, actualTemperature.toString(), Toast.LENGTH_SHORT).show()
                if(actualTemperature>=highTemperature && gifMap.hasPriority(floatingService.getGif(),gifMap.HOT))//!floatingService.isSameBackgroud(gifMap.HOT))
                    floatingService.updateGif(gifMap.HOT)
                else if(actualTemperature<=lowTemperature && gifMap.hasPriority(floatingService.getGif(),gifMap.COLD))
                    floatingService.updateGif(gifMap.COLD)
            }

        })

    }
}

    interface Endpoint {
        @Headers("key: 90fbafa29ed44a07b2570623230805")
        @GET("current.json")
        fun getPosts(@Query("q") query: String) : Call<WeatherClientResponse>
    }

    data class WeatherClientResponse(
        val location : Object,
        val current : CurrentWeatherClient
    )

    data class CurrentWeatherClient(
        val last_updated : String,
        val last_updated_epoch : Int,
        val temp_c : Float,
        val temp_f : Float,
        val feelslike_c : Float,
        val feelslike_f : Float,
        val condition : ConditionWeatherClient,
        val wind_mph : Float,
        val wind_kph : Float,
        val wind_degree : Int,
        val wind_dir : String,
        val pressure_mb : Float,
        val pressure_in : Float,
        val precip_mm : Float,
        val precip_in : Float,
        val humidity : Int,
        val cloud : Int,
        val is_day : Int,
        val uv : Float,
        val gust_mph : Float,
        val gust_kph : Float,
    )

    data class ConditionWeatherClient(
        val text : String,
        val icon : String,
        val code : Int
    )


