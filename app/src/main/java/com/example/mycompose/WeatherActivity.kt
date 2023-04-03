package com.example.mycompose

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.location.LocationManagerCompat
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mycompose.retrofit.ApiClient
import com.example.mycompose.retrofit.ApiService
import com.example.mycompose.ui.theme.MyComposeTheme
import com.example.mycompose.util.ApplicationConstants
import com.google.accompanist.permissions.*

class WeatherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    color = MaterialTheme.colors.background,
                ) {
                    WeatherScreen()
                }
            }
        }
    }
}

@SuppressLint("MissingPermission", "PermissionLaunchedDuringComposition")
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel = WeatherViewModel(
    ApiClient.getClient().create(
        ApiService::class.java))) {
    val context = LocalContext.current
    val preferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    val temp = remember {
        mutableStateOf(preferences.getString(ApplicationConstants.TEMP, ApplicationConstants.EMPTY))
    }
    val weatherDesc = remember {
        mutableStateOf(preferences.getString(ApplicationConstants.WEATHER_DESC, ApplicationConstants.EMPTY))
    }
    val humidity = remember {
        mutableStateOf(
            preferences.getString(
                ApplicationConstants.HUMIDITY, ApplicationConstants.EMPTY
            )
        )
    }
    val wind = remember {
        mutableStateOf(preferences.getString(ApplicationConstants.WIND, ApplicationConstants.EMPTY))
    }
    val image = remember {
        mutableStateOf(
            preferences.getString(
                ApplicationConstants.WEATHER_IMAGE, ApplicationConstants.EMPTY
            )
        )
    }
    var value by remember {
        val name = preferences.getString(ApplicationConstants.CITY, ApplicationConstants.EMPTY)
        if (!name.equals(ApplicationConstants.EMPTY, ignoreCase = true)) {
            mutableStateOf(name.toString())
        } else {
            mutableStateOf(ApplicationConstants.EMPTY)
        }
    }
    weatherViewModel.getWeatherDetails(value, temp, weatherDesc, humidity, wind, image)

    val permissions = remember {
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    val permissionsHandler = remember(permissions) { PermissionsHandler() }
    val permissionsStates by permissionsHandler.state.collectAsState()
    HandlePermissionsRequest(permissions = permissions, permissionsHandler = permissionsHandler)

    if (permissionsStates.multiplePermissionsState?.allPermissionsGranted == true) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (LocationManagerCompat.isLocationEnabled(lm)) {
            val locationGPS: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val latitude = locationGPS?.latitude
            val longitude = locationGPS?.longitude
            latitude?.let {
                longitude?.let { it1 ->
                    weatherViewModel.getCurrentWeatherDetails(
                        it, it1, temp, weatherDesc, humidity, wind, image
                    )
                }
            }
        }
    } else {
        permissionsHandler.onEvent(PermissionsHandler.Event.PermissionRequired)
    }

    Column {
        val preferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
        TextField(value = value, onValueChange = {
            value = it
        }, Modifier.fillMaxWidth(), singleLine = true, label = {
            Text(text = "Please enter city")
        }, keyboardActions = KeyboardActions(onDone = {
            val editor = preferences.edit()
            editor.putString(ApplicationConstants.CITY, value)
            editor.putString(ApplicationConstants.TEMP, temp.value)
            editor.putString(ApplicationConstants.HUMIDITY, humidity.value)
            editor.putString(ApplicationConstants.WIND, wind.value)
            editor.putString(ApplicationConstants.WEATHER_IMAGE, image.value)
            editor.putString(ApplicationConstants.WEATHER_DESC, weatherDesc.value)
            editor.apply()
            weatherViewModel.getWeatherDetails(value, temp, weatherDesc, humidity, wind, image)
        }))
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .wrapContentHeight()
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .background(color = Color.Magenta)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.TopCenter)) {
                    temp.value?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            style = TextStyle(fontSize = 60.sp),
                            modifier = Modifier.align(CenterHorizontally)
                        )
                    }
                    weatherDesc.value?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            style = TextStyle(fontSize = 20.sp),
                            modifier = Modifier
                                .align(CenterHorizontally)
                                .padding(top = 18.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .align(Alignment.BottomStart)
                ) {
                    humidity.value?.let { Text(text = it, color = Color.White) }
                    wind.value?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                            color = Color.White
                        )
                    }
                }
                GlideImage(
                    model = image.value,
                    contentDescription = "nothing",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(120.dp)
                        .width(120.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissionAction(
    action: PermissionsHandler.Action,
    permissionStates: MultiplePermissionsState?,
    @StringRes rationaleText: Int,
    @StringRes neverAskAgainText: Int,
    onOkTapped: () -> Unit,
    onSettingsTapped: () -> Unit,
) {
    when (action) {
        PermissionsHandler.Action.REQUEST_PERMISSION -> {
            LaunchedEffect(true) {
                permissionStates?.launchMultiplePermissionRequest()
            }
        }
        PermissionsHandler.Action.SHOW_RATIONALE -> {
        }
        PermissionsHandler.Action.NO_ACTION -> Unit
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissionsRequest(permissions: List<String>, permissionsHandler: PermissionsHandler) {

    val state by permissionsHandler.state.collectAsState()
    val permissionsState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(permissionsState) {
        permissionsHandler.onEvent(PermissionsHandler.Event.PermissionsStateUpdated(permissionsState))
        when {
            permissionsState.allPermissionsGranted -> {
                permissionsHandler.onEvent(PermissionsHandler.Event.PermissionsGranted)
            }
            else -> {
                permissionsHandler.onEvent(PermissionsHandler.Event.PermissionDenied)
            }
        }
    }

    HandlePermissionAction(
        action = state.permissionAction,
        permissionStates = state.multiplePermissionsState,
        rationaleText = R.string.permission_rationale,
        neverAskAgainText = R.string.never_ask,
        onOkTapped = { permissionsHandler.onEvent(PermissionsHandler.Event.PermissionRationaleOkTapped) },
        onSettingsTapped = { permissionsHandler.onEvent(PermissionsHandler.Event.PermissionSettingsTapped) },
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyComposeTheme {
        WeatherScreen()
    }
}