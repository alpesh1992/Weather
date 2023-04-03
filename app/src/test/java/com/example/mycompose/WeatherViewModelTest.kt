package com.example.mycompose

import androidx.compose.runtime.MutableState
import com.example.mycompose.model.CurrentWeatherResponse
import com.example.mycompose.model.Main
import com.example.mycompose.model.WeatherItem
import com.example.mycompose.model.Wind
import com.example.mycompose.retrofit.ApiService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Callable


class WeatherViewModelTest {
    private lateinit var weatherViewModel: WeatherViewModel

    @MockK
    private lateinit var tempMutableState: MutableState<String?>
    @MockK
    private lateinit var weatherDescMutableState: MutableState<String?>
    @MockK
    private lateinit var humidityMutableState: MutableState<String?>
    @MockK
    private lateinit var windMutableState: MutableState<String?>
    @MockK
    private lateinit var imageMutableState: MutableState<String?>
    @MockK
    private lateinit var apiService: ApiService
    @MockK
    private lateinit var single: Single<CurrentWeatherResponse>
    @MockK
    private lateinit var singleObserver: DisposableSingleObserver<CurrentWeatherResponse>
    @MockK
    private lateinit var currentWeatherResponse: CurrentWeatherResponse
    @MockK
    private lateinit var main: Main
    @MockK
    private lateinit var weatherList: List<WeatherItem>
    @MockK
    private lateinit var weather: WeatherItem
    @MockK
    private lateinit var wind: Wind

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler: Callable<Scheduler?>? -> Schedulers.trampoline() }
        weatherViewModel = WeatherViewModel(
            apiService
        )
    }

    @Test
    fun getWeatherDetails() {
        val capturingSlot = CapturingSlot<DisposableSingleObserver<CurrentWeatherResponse>>()
        every {
            apiService.getCurrentWeather(CITY, APP_ID)
        } returns single
        every { single.subscribeOn(any()) } returns single
        every { single.observeOn(any()) } returns single
        every { single.subscribeWith(any()) } returns singleObserver
        every { currentWeatherResponse.main } returns main
        every { main.temp } returns TEMP
        every { currentWeatherResponse.weather } returns weatherList
        every { weatherList[0] } returns weather
        every { weather.description } returns WEATHER_DESC
        every { main.humidity } returns HUMIDITY
        every { currentWeatherResponse.wind } returns wind
        every { wind.speed } returns WIND
        every { weather.icon } returns IMAGE_URL
        weatherViewModel.getWeatherDetails(
            CITY,
            tempMutableState,
            weatherDescMutableState,
            humidityMutableState,
            windMutableState,
            imageMutableState
        )
        verify {
            single.subscribeWith(capture(capturingSlot))
        }
        capturingSlot.captured.onSuccess(currentWeatherResponse)
    }

    @Test
    fun getCurrentWeatherDetails() {
        val capturingSlot = CapturingSlot<DisposableSingleObserver<CurrentWeatherResponse>>()
        every {
            apiService.getCurrentWeatherDetails(LATITUDE.toString(), LONGITUDE.toString(), APP_ID)
        } returns single
        every { single.subscribeOn(any()) } returns single
        every { single.observeOn(any()) } returns single
        every { single.subscribeWith(any()) } returns singleObserver
        every { currentWeatherResponse.main } returns main
        every { main.temp } returns TEMP
        every { currentWeatherResponse.weather } returns weatherList
        every { weatherList[0] } returns weather
        every { weather.description } returns WEATHER_DESC
        every { main.humidity } returns HUMIDITY
        every { currentWeatherResponse.wind } returns wind
        every { wind.speed } returns WIND
        every { weather.icon } returns IMAGE_URL
        weatherViewModel.getCurrentWeatherDetails(
            LATITUDE,
            LONGITUDE,
            tempMutableState,
            weatherDescMutableState,
            humidityMutableState,
            windMutableState,
            imageMutableState
        )
        verify {
            single.subscribeWith(capture(capturingSlot))
        }
        capturingSlot.captured.onSuccess(currentWeatherResponse)
    }

    private companion object {
        const val APP_ID = "06e42231e188a48c9ba6a6eefa8d78b1"
        const val CITY = "london"
        const val LATITUDE = 87.61
        const val LONGITUDE = 12.98
        const val WEATHER_DESC = "cloudy"
        const val IMAGE_URL = "image_url"
        const val TEMP = 12.0
        const val HUMIDITY = 61
        const val WIND = 3.0
    }
}