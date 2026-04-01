package com.rr.aido.ui.circletosearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rr.aido.ui.circletosearch.data.SearchEngine

@Composable
fun TopControlBar(
    selectedEngine: SearchEngine,
    desktopModeEngines: Set<SearchEngine>,
    isDarkMode: Boolean,
    showGradientBorder: Boolean,
    onClose: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleGradientBorder: () -> Unit,
    onRefresh: () -> Unit,
    onCopyUrl: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = selectedEngine.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
        
        
            // Premium Metallic Glassmorphic Menu
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(
                    extraSmall = RoundedCornerShape(24.dp)
                )
            ) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = Color.Black.copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2A2A2A).copy(alpha = 0.95f),
                                    Color(0xFF1A1A1A).copy(alpha = 0.98f)
                                )
                            )
                        )
                        .padding(vertical = 8.dp)
                ) {
                    val isDesktop = desktopModeEngines.contains(selectedEngine)
                    if (selectedEngine.supportsBrowserOptions) {
                        DropdownMenuItem(
                            text = { Text(if (isDesktop) "Mobile Mode" else "Desktop Mode", color = Color.White) },
                            leadingIcon = { Icon(if (isDesktop) Icons.Default.Smartphone else Icons.Default.DesktopWindows, null, tint = Color(0xFF1E88E5)) },
                            onClick = { onToggleDesktopMode(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isDarkMode) "Light Mode" else "Dark Mode", color = Color.White) },
                            leadingIcon = { Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = Color(0xFF00ACC1)) },
                            onClick = { onToggleDarkMode(); showMenu = false }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(if (showGradientBorder) "Hide Border" else "Show Border", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.BorderOuter, null, tint = Color(0xFF5E35B1)) },
                        onClick = { onToggleGradientBorder(); showMenu = false }
                    )
                    if (selectedEngine.supportsBrowserOptions) {
                        DropdownMenuItem(
                            text = { Text("Refresh", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Refresh, null, tint = Color(0xFF26A69A)) },
                            onClick = { onRefresh(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy URL", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color(0xFF1E88E5)) },
                            onClick = { onCopyUrl(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Open in Browser", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.OpenInNew, null, tint = Color(0xFF00ACC1)) },
                            onClick = { onOpenInBrowser(); showMenu = false }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Settings", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Settings, null, tint = Color(0xFF5E35B1)) },
                        onClick = { onOpenSettings(); showMenu = false }
                    )
                }
            }
        }
    }
}
