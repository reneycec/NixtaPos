package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.viewmodels.LoginViewModel
import com.example.ui.theme.NixtaTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()
  private val loginViewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkMode by viewModel.isDarkMode.collectAsState()
      NixtaTheme(darkTheme = isDarkMode) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainScreen(
            viewModel = viewModel,
            loginViewModel = loginViewModel,
            onExitApp = { finish() },
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}

