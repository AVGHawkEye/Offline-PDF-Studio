package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.PdfStudioViewModel
import com.example.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {

    private val viewModel: PdfStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDarkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PdfStudioApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PdfStudioApp(viewModel: PdfStudioViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val screen = uiState.currentScreen) {
        is CurrentScreen.Dashboard -> {
            DashboardScreen(
                uiState = uiState,
                onToolSelected = { tool -> viewModel.openTool(tool) },
                onCategorySelected = { cat -> viewModel.selectCategory(cat) },
                onSearchQueryChanged = { query -> viewModel.setSearchQuery(query) },
                onThemeToggle = { viewModel.toggleTheme() },
                onClearCache = {
                    viewModel.clearTemporaryCache()
                }
            )
        }

        is CurrentScreen.Workspace -> {
            BackHandler {
                viewModel.navigateBack()
            }
            WorkspaceScreen(
                tool = screen.tool,
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}
