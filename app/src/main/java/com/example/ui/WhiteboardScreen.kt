package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.canvas.WhiteboardCanvas
import com.example.ui.components.BoardDrawer
import com.example.ui.components.WhiteboardBottomToolbar
import com.example.ui.components.WhiteboardDialogs
import com.example.ui.components.WhiteboardTopBar
import com.example.ui.viewmodel.WhiteboardViewModel

@Composable
fun WhiteboardScreen(
    viewModel: WhiteboardViewModel,
    modifier: Modifier = Modifier
) {
    val isZenMode by viewModel.isZenMode.collectAsState()
    val isDrawerOpen by viewModel.isBoardDrawerOpen.collectAsState()

    // Handle back button smoothly
    BackHandler(enabled = isDrawerOpen || isZenMode) {
        if (isDrawerOpen) {
            viewModel.closeBoardDrawer()
        } else if (isZenMode) {
            viewModel.toggleZenMode()
        }
    }

    BoardDrawer(viewModel = viewModel) {
        Box(modifier = modifier.fillMaxSize()) {
            // Main Infinite Whiteboard Canvas
            WhiteboardCanvas(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Top Bar
            AnimatedVisibility(
                visible = !isZenMode,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                WhiteboardTopBar(viewModel = viewModel)
            }

            // Bottom Toolbar Dock
            AnimatedVisibility(
                visible = !isZenMode,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                WhiteboardBottomToolbar(viewModel = viewModel)
            }

            // Zen Mode exit button
            AnimatedVisibility(
                visible = isZenMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.toggleZenMode() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.testTag("exit_zen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit zen mode"
                    )
                }
            }

            // Dialogs
            WhiteboardDialogs(viewModel = viewModel)
        }
    }
}
