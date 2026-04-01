package com.rr.aido.ui.circletosearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rr.aido.ui.circletosearch.data.SearchEngine
import com.rr.aido.ui.circletosearch.utils.UIPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiPreferences: UIPreferences,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    

    var useGoogleLensOnly by remember { mutableStateOf(uiPreferences.isUseGoogleLensOnly()) }
    val initialOrderString = uiPreferences.getSearchEngineOrder()
    val allEngines = SearchEngine.values()
    
    // Parse order from preferences or use default
    val engineOrder = remember {
        val preferredNames = initialOrderString?.split(",") ?: emptyList()
        val ordered = mutableListOf<SearchEngine>()
        
        // Add existing engines in preferred order
        preferredNames.forEach { name ->
            allEngines.find { it.name == name }?.let { ordered.add(it) }
        }
        
        // Add any remaining engines
        allEngines.forEach { engine ->
            if (!ordered.contains(engine)) ordered.add(engine)
        }
        mutableStateListOf(*ordered.toTypedArray())
    }



    LaunchedEffect(useGoogleLensOnly) {
        uiPreferences.setUseGoogleLensOnly(useGoogleLensOnly)
    }

    LaunchedEffect(engineOrder.toList()) {
        uiPreferences.setSearchEngineOrder(engineOrder.joinToString(",") { it.name })
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // General Section


            // Search Method Section
            SearchMethodSelector(
                isLensOnly = useGoogleLensOnly,
                onMethodChange = { useGoogleLensOnly = it }
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Spacer(modifier = Modifier.height(24.dp))

            // Search Engines Section
            SettingsSectionHeader(title = "Search Engines")
            Text(
                text = "Tap arrows to change tab sequence",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column {
                    engineOrder.forEachIndexed { index, engine ->
                        EngineOrderItem(
                            engine = engine,
                            isFirst = index == 0,
                            isLast = index == engineOrder.size - 1,
                            onMoveUp = {
                                if (index > 0) {
                                    val temp = engineOrder[index]
                                    engineOrder[index] = engineOrder[index - 1]
                                    engineOrder[index - 1] = temp
                                }
                            },
                            onMoveDown = {
                                if (index < engineOrder.size - 1) {
                                    val temp = engineOrder[index]
                                    engineOrder[index] = engineOrder[index + 1]
                                    engineOrder[index + 1] = temp
                                }
                            }
                        )
                        if (index < engineOrder.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun EngineOrderItem(
    engine: SearchEngine,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    // Get custom icon and color for each engine
    val (icon, iconColor) = when(engine) {
        SearchEngine.Google -> Icons.Default.Search to Color(0xFF1E88E5)
        SearchEngine.Bing -> Icons.Default.TravelExplore to Color(0xFF00ACC1)
        SearchEngine.Yandex -> Icons.Default.Language to Color(0xFF5E35B1)
        SearchEngine.TinEye -> Icons.Default.CameraAlt to Color(0xFF26A69A)
        SearchEngine.ChatGPT -> Icons.Default.Chat to Color(0xFF1E88E5)
        SearchEngine.Perplexity -> Icons.Default.Psychology to Color(0xFF00ACC1)
        else -> Icons.Default.Search to Color(0xFF5E35B1)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colored background
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(10.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = engine.displayName,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )
        
        // Arrow buttons with better styling
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = if (!isFirst) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ) {
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    tint = if (!isFirst) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = if (!isLast) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ) {
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    tint = if (!isLast) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMethodSelector(
    isLensOnly: Boolean,
    onMethodChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "SEARCH METHOD",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                onClick = { onMethodChange(false) },
                selected = !isLensOnly,
                icon = { SegmentedButtonDefaults.Icon(!isLensOnly) },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Multi-Search", style = MaterialTheme.typography.labelLarge)
                }
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                onClick = { onMethodChange(true) },
                selected = isLensOnly,
                icon = { SegmentedButtonDefaults.Icon(isLensOnly) },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Lens", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
