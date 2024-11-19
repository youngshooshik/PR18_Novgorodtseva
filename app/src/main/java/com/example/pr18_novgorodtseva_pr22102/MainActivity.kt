package com.example.pr18_novgorodtseva_pr22102

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pr18_novgorodtseva_pr22102.ui.theme.PR18_Novgorodtseva_PR22102Theme
const val API_KEY = "AIzaSyA7tY9iy71ccbJlqPHTkc7xrRlu_1tDhvo"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PR18_Novgorodtseva_PR22102Theme {
                val daysList = remember { mutableStateOf(listOf<WeatherModel>()) }
                val dialogState = remember { mutableStateOf(false) }
                val currentDay = remember { mutableStateOf(WeatherModel("", "", "0.0", "", "", "0.0", "0.0", "")) }
                val isShowingHours = remember { mutableStateOf(true) }

                if (dialogState.value) {
                    DialogSearch(dialogState) { city ->
                        getData(city, this, daysList, currentDay)
                    }
                }

                getData("London", this, daysList, currentDay)
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.weatherback),
                        contentDescription = "Фон погоды",
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.5f),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MainCard(currentDay, onClickSearch = { dialogState.value = true }, dialogState = dialogState)
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isShowingHours.value = true },
                                modifier = Modifier.weight(1f),
                                colors = androidx.compose.material.ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBBDEFB))
                            ) {
                                Text("HOURS")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { isShowingHours.value = false }, // Завершите функцию onClick
                                modifier = Modifier.weight(1f),
                                colors = androidx.compose.material.ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBBDEFB))
                            ) {
                                Text("DAYS")
                            }
                        }
                        Button(
                            onClick = { /* Действие при нажатии */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBBDEFB))
                        ) {
                            Text("DAYS")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TabLayout(daysList, currentDay, isShowingHours)

                        private fun getData(
                            city: String,
                            context: Context,
                            daysList: MutableState<List<WeatherModel>>,
                            currentDay: MutableState<WeatherModel>
                        ) {
                            val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=3&aqi=no&alerts=no"
                            val queue = Volley.newRequestQueue(context)

                            val sRequest = StringRequest(Request.Method.GET, url,
                                { response ->
                                    val list = getWeatherByDays(response)
                                    if (list.isNotEmpty()) {
                                        currentDay.value = list[0]
                                        daysList.value = list
                                    }
                                },
                                { error ->
                                    Log.d("Error", "Volley Error: $error")
                                }
                            )
                            queue.add(sRequest)
                        }

                        private fun getWeatherByDays(response: String): List<WeatherModel> {
                            if (response.isEmpty()) return emptyList()

                            val list = ArrayList<WeatherModel>()
                            val mainObject = JSONObject(response)
                            val city = mainObject.getJSONObject("location").getString("name")
                            val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

                            for (i in 0 until days.length()) {
                                val day = days.getJSONObject(i)
                                list.add(
                                    WeatherModel(
                                        city,
                                        day.getString("date"),
                                        "",
                                        day.getJSONObject("day").getJSONObject("condition").getString("text"),
                                        day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                                        day.getJSONObject("day").getString("maxtemp_c"),
                                        day.getJSONObject("day").getString("mintemp_c"),
                                        day.getJSONArray("hour").toString()
                                    )
                                )
                            }

                            val current = mainObject.getJSONObject("current")
                            list[0] = list[0].copy(
                                time = current.getString("last_updated"),
                                currentTemp = current.getString("temp_c")
                            )

                            return list
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PR18_Novgorodtseva_PR22102Theme {
        Greeting("Android")
    }
}