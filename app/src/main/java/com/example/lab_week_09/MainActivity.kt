package com.example.lab_week_09

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonClass
import com.squareup.moshi.adapter
import kotlin.OptIn
import kotlin.ExperimentalStdlibApi

@JsonClass(generateAdapter = true)
data class Student(val name: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(navController = navController)
                }
            }
        }
    }
}

@Composable
fun App(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home { json ->
                val encodedJson = Uri.encode(json)
                navController.navigate("result/$encodedJson")
            }
        }
        composable(
            "result/{json}",
            arguments = listOf(navArgument("json") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("json") ?: ""
            val json = Uri.decode(encodedJson)
            ResultContent(json)
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun Home(onNavigate: (String) -> Unit) {
    val listData = remember { mutableStateListOf(Student("Tanu"), Student("Tina"), Student("Tono")) }
    val inputValue = remember { mutableStateOf("") }

    LazyColumn {
        item {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.enter_item))
                TextField(
                    value = inputValue.value,
                    onValueChange = { inputValue.value = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Row {
                    Button(onClick = {
                        if (inputValue.value.isNotBlank()) {  // Fix: Cek tidak kosong
                            listData.add(Student(inputValue.value))
                            inputValue.value = ""
                        }
                    }) {
                        Text(text = stringResource(id = R.string.button_click))
                    }
                    Button(onClick = {
                        // Serialize ke JSON dengan Moshi (bonus)
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter<List<Student>>()
                        val json = adapter.toJson(listData)
                        onNavigate(json)
                    }) {
                        Text(text = stringResource(id = R.string.button_navigate))
                    }
                }
            }
        }
        items(listData) { item ->
            Text(text = item.name, modifier = Modifier.padding(8.dp))
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ResultContent(json: String) {
    // Deserialize JSON kembali ke list dengan Moshi (bonus)
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter<List<Student>>()
    val listData: List<Student> = try {
        adapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(listData) { item ->
            Text(text = item.name, modifier = Modifier.padding(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    Home(onNavigate = {})
}