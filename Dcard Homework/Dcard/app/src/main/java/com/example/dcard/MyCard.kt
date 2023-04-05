package com.example.dcard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.launch

class MyCard(val title: String, val description: String, val url: String) {
    fun start(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardView(card: MyCard) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(20.dp)
    ) {
        Text(
            text = card.title,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            fontSize = 20.sp,
        )
        Text(
            text = card.description,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
        )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column() {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            card.start(context)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "More Detail")
                }
            }
        }
    }
}