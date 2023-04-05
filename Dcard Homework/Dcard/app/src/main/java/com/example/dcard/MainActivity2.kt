package com.example.dcard
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                ){
                    startMenu(this@MainActivity2)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun startMenu(context: Context) {
    Column {
        Image(
            painter = painterResource(id = R.drawable.logo_pic),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
        )

        OutlinedButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(horizontal = 130.dp).fillMaxWidth().padding(vertical = 170.dp)
        ) {
            Text(text = "Start")
        }
    }

}