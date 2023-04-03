package com.example.mycompose

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import com.example.mycompose.retrofit.ApiService
import com.example.mycompose.retrofit.ApiClient
import com.example.mycompose.model.CurrentWeatherResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.*
import kotlin.math.roundToInt

class WeatherViewModel(val apiService: ApiService) : ViewModel() {
    private val disposable = CompositeDisposable()

    fun getWeatherDetails(
        value: String,
        temp: MutableState<String?>,
        weatherDesc: MutableState<String?>,
        humidity: MutableState<String?>,
        wind: MutableState<String?>,
        image: MutableState<String?>
    ) {
        disposable.add(
            apiService.getCurrentWeather(
                value, APP_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CurrentWeatherResponse?>() {
                    override fun onSuccess(currentWeatherResponse: CurrentWeatherResponse) {
                        temp.value = String.format(
                            Locale.getDefault(),
                            DEGREE_SYSMBOL_FORMAT,
                            currentWeatherResponse.main.temp - 273.15f
                        )
                        weatherDesc.value = currentWeatherResponse.weather[0].description
                        humidity.value = "$HUMIDITY ${currentWeatherResponse.main.humidity} $PERCENT_SYMBOL"
                        wind.value = "$WIND ${currentWeatherResponse.wind.speed.roundToInt()} $KM_PER_HR"
                        image.value = "$IMAGE_URL_PREFIX${currentWeatherResponse.weather[0].icon}$IMAGE_URL_SUFFIX"
                    }

                    override fun onError(e: Throwable) {
                        try {
                            val error = e as HttpException
                        } catch (exception: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        )
    }

    fun getCurrentWeatherDetails(
        latitude: Double,
        longitude: Double,
        temp: MutableState<String?>,
        weatherDesc: MutableState<String?>,
        humidity: MutableState<String?>,
        wind: MutableState<String?>,
        image: MutableState<String?>
    ) {
        disposable.add(
            apiService.getCurrentWeatherDetails(
                latitude.toString(), longitude.toString(), APP_ID
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CurrentWeatherResponse?>() {
                    override fun onSuccess(currentWeatherResponse: CurrentWeatherResponse) {
                        temp.value = String.format(
                            Locale.getDefault(),
                            DEGREE_SYSMBOL_FORMAT,
                            currentWeatherResponse.main.temp - 273.15f
                        )
                        weatherDesc.value = currentWeatherResponse.weather[0].description
                        humidity.value = "$HUMIDITY ${currentWeatherResponse.main.humidity} $PERCENT_SYMBOL"
                        wind.value = "$WIND ${currentWeatherResponse.wind.speed.roundToInt()} $KM_PER_HR"
                        image.value = "$IMAGE_URL_PREFIX${currentWeatherResponse.weather[0].icon}$IMAGE_URL_SUFFIX"
                    }

                    override fun onError(e: Throwable) {
                        try {
                            val error = e as HttpException
                        } catch (exception: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        )
    }
    private companion object {
        const val APP_ID = "06e42231e188a48c9ba6a6eefa8d78b1"
        const val DEGREE_SYSMBOL_FORMAT = "%.0f°"
        const val HUMIDITY = "Humidity"
        const val WIND = "Wind"
        const val PERCENT_SYMBOL = "%"
        const val KM_PER_HR = "km/hr"
        const val IMAGE_URL_PREFIX = "https://openweathermap.org/img/wn/"
        const val IMAGE_URL_SUFFIX = "@2x.png"
    }
}