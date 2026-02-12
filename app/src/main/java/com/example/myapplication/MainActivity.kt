package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val busTime = remember { mutableStateOf("未取得") }
    val trainTimes = remember { mutableStateOf(listOf("未取得", "未取得")) }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun getTodayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Unknown"
        }
    }

    fun getDayType(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY, Calendar.SUNDAY -> "weekend"
            else -> "weekday"
        }
    }

    fun getNextTwoTimesFromCsv(fileName: String, dayKey: String, direction: String): List<String> {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val now = LocalTime.now()
            val nextTimes = mutableListOf<String>()

            reader.readLine() // skip header
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split(",")
                if (parts.size < 3) continue
                val day = parts[0].trim()
                val time = parts[1].trim()
                val dir = parts[2].trim()

                if (day == dayKey && dir == direction) {
                    val parsed = LocalTime.parse(time, timeFormatter)
                    if (parsed.isAfter(now)) {
                        nextTimes.add(parsed.toString())
                        if (nextTimes.size == 2) break
                    }
                }
            }
            reader.close()
            when (nextTimes.size) {
                2 -> nextTimes
                1 -> listOf(nextTimes[0], "もう無い（帰れ）")
                else -> listOf("もう無い（帰れ）", "もっと無い（マジで帰れ）")
            }
        } catch (e: Exception) {
            listOf("読み込み失敗", "読み込み失敗")
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Button(onClick = {
            busTime.value = getNextTwoTimesFromCsv("bus_time.csv", getTodayName(), "from_university")[0]
            trainTimes.value = getNextTwoTimesFromCsv("niken_time.csv", getDayType(), "n_to_d")
        }) {
            Text("今すぐ調べる")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("🚌 バス: ${busTime.value}")
        Text("🚃 電車: ${trainTimes.value[0]}")
        Text("🚃 次の電車: ${trainTimes.value[1]}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        HomeScreen()
    }
}
