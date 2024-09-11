package com.norm.myuploadfilektor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.norm.myuploadfilektor.ui.theme.MyUploadFileKtorTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyUploadFileKtorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel {
                        UploadFileViewModel(
                            repository = FileRepository(
                                httpClient = HttpClient.client,
                                fileReader = FileReader(
                                    context = applicationContext,
                                )
                            )
                        )
                    }
                    val state = viewModel.state

                    LaunchedEffect(
                        state.errorMessage
                    ) {
                        state.errorMessage?.let {
                            Toast.makeText(
                                applicationContext,
                                it,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }

                    LaunchedEffect(
                        state.isUploadComplete
                    ) {
                        if (state.isUploadComplete) {
                            Toast.makeText(
                                applicationContext,
                                "Upload complete",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }


                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { contentUri ->
                        contentUri?.let {
                            viewModel.uploadFile(contentUri)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            !state.isUploading -> {
                                Button(
                                    onClick = {
                                        filePickerLauncher.launch("*/*")
                                    }
                                ) {
                                    Text(
                                        text = "Pick file"
                                    )
                                }
                            }

                            else -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    LinearProgressIndicator(
                                        progress = {
                                            state.progress
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(16.dp)
                                            .clip(
                                                RoundedCornerShape(32.dp)
                                            ),
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .height(8.dp)
                                    )
                                    Text(
                                        text = "${(state.progress * 100).roundToInt()}%"
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.cancelUpload()
                                        }) {
                                        Text(
                                            text = "Cancel upload"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}