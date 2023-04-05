package com.example.dcard

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlinx.coroutines.flow.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContent {
            MaterialTheme {
                Column {
                    SearchRepositories()
                }
            }
        }
    }
}

@Composable
fun SearchRepositories() {
    var nameList by remember { mutableStateOf(Array(10) { Array(3) { "" } }) }
    var value by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val searchFlow = remember { MutableStateFlow("") }
    var page = 1
    var perPage = 10
    var flag = true
    var isTypeError by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        searchFlow.emit(value)
    }
    LaunchedEffect(searchFlow) {
        searchFlow
            .debounce(500) // 500 毫秒內的任何輸入都會被忽略，直到 500 毫秒後最後一次輸入發生
            .distinctUntilChanged() // 僅當文本發生更改時才觸發搜索
            .collect {
                isLoading = true
                if (!isAlphaNumeric(value)) {
                    isTypeError = true
                }
                else {
                    val temp = GetRepositories(value, page)
                    val result = temp.resList.toMutableList()
                    perPage = temp.perPage
                    flag = temp.flag
                    isTypeError = false
                    if (flag) {
                        for (i in 0 until perPage) {
                            for (j in 0 .. 2) {
                                nameList[i][j] = result[i][j]
                            }
                        }
                    }
                }
                isLoading = false
            }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = value,
            onValueChange = { text ->
                value = text.replace('0', '*')
                isLoading = true
                println(value)
            },
            label = { Text("Search") },
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        else {
            if (isTypeError) {
                Text(
                    text = "Type Error",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                )
            }
            else {
                if (flag) {
                    if (value != "") {
                        ShowRepositoriesName(nameList, perPage, page, value)
                    }
                }
            }
        }
    }
}

fun isAlphaNumeric(input: String): Boolean {
    val pattern = Regex("[a-zA-Z0-9]+")
    if (input == "")
        return true
    else
        return input.matches(pattern)
}

@Composable
fun ShowRepositoriesName(resList: Array<Array<String>>, perPage: Int, page: Int, value: String) {
    val cardListNow = remember { mutableListOf<MyCard>() }
    var isLoading by remember { mutableStateOf(false) }
    val currentPage = remember { mutableStateOf(page + 1) }
    var isEnd by remember { mutableStateOf(false) }
    var nowPerPage = 11
    val scrollState = rememberLazyListState()

    LaunchedEffect(currentPage.value) {
        isLoading = true
        val temp = GetRepositories(value, currentPage.value)
        val result = temp.resList.toMutableList()
        val flag = temp.flag
        if (perPage < resList.size) {
            resList.copyOf(perPage)
        }
        nowPerPage = temp.perPage
        isEnd = temp.isEnd
        if (flag) {
            for (i in 0 until perPage) {
                for (j in 0 .. 2) {
                    resList[i][j] = result[i][j]
                }
            }
        }
        isLoading = false
    }

    val allEmpty = resList.all { innerArray ->
        innerArray.all { str -> str.isEmpty() }
    }
    var runTime = perPage
    if (!allEmpty) {
        if (nowPerPage < perPage) {
            runTime = nowPerPage
        }
        for (i in 0 until runTime) {
            var createCard = MyCard(resList[i][0], resList[i][1], resList[i][2])
            if (resList[i][0] != "" && resList[i][1] != "" && resList[i][1] != "")
                cardListNow.add(createCard)
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(cardListNow) { card ->
                MyCardView(card = card)
            }
            item{
                if (isEnd) {
                    Text(
                        text = "Down To The Bottom",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 14.sp,
                    )
                }
                else {
                    if (!isLoading) {
                        val visibleItemCount = scrollState.layoutInfo.visibleItemsInfo.size
                        val totalItemCount = scrollState.layoutInfo.totalItemsCount
                        val firstVisibleItemIndex = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

                        if (visibleItemCount + firstVisibleItemIndex >= totalItemCount) {
                            currentPage.value += 1
                        }
                    }
                    else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

data class SearchResult(val resList: Array<Array<String>>, val perPage: Int, val flag: Boolean, val isEnd: Boolean)

suspend fun GetRepositories(search: String, page: Int): SearchResult{
    val singleRes = 3
    val total = 10
    var flag = true
    var perPage = 10
    var isEnd = false
    val resList = Array(total) { Array(singleRes) { "" } }
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    if (search != ""){
        var searchURL = "https://api.github.com/search/repositories?q=" + search
        val getItem = client.get(searchURL) {
            parameter("per_page", perPage)
            parameter("page", page)
        }.body<String>()
        delay(2000)
        val change: JSONObject = JSONObject(getItem)
        val getTotalCount = change.getInt("total_count")
        if (getTotalCount < perPage * page) {
            perPage = getTotalCount - (perPage * (page - 1))
        }
        if (perPage < 0) {
            isEnd = true
            return SearchResult(resList, perPage + 10, flag, isEnd)
        }
        else {
            for (i in 0 until perPage) {
                delay(100)
                if(change.getJSONArray("items").isNull(0)) {
                    flag = false
                    break
                }
                resList[i][0] = change.getJSONArray("items").getJSONObject(i).get("full_name").toString()
                resList[i][1] = change.getJSONArray("items").getJSONObject(i).get("description").toString()
                resList[i][2] = change.getJSONArray("items").getJSONObject(i).get("html_url").toString()
            }
            if (perPage < resList.size) {
                resList.copyOf(perPage)
            }
            return SearchResult(resList, perPage, flag, isEnd)
        }
    }
    else {
        return SearchResult(resList, perPage, flag, isEnd)
    }
}
