package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.PoliceRepository
import com.example.ui.PoliceScreen
import com.example.ui.PoliceViewModel
import com.example.ui.theme.PoliceDirectoryTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PoliceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = PoliceRepository(applicationContext)
        val factory = PoliceViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[PoliceViewModel::class.java]

        setContent {
            PoliceDirectoryTheme {
                PoliceScreen(viewModel = viewModel)
            }
        }
    }
}

