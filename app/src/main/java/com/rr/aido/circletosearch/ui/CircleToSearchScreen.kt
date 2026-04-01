package com.rr.aido.ui.circletosearch.ui

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rr.aido.ui.circletosearch.data.SearchEngine
import com.rr.aido.ui.circletosearch.data.isDirectUpload
import com.rr.aido.ui.circletosearch.data.TextNode
import com.rr.aido.ui.circletosearch.data.TextRepository

import com.rr.aido.ui.circletosearch.ui.components.searchWithGoogleLens
import com.rr.aido.ui.circletosearch.ui.theme.OverlayGradientColors
import com.rr.aido.ui.circletosearch.utils.FriendlyMessageManager
import com.rr.aido.ui.circletosearch.utils.ImageSearchUploader
import com.rr.aido.ui.circletosearch.utils.ImageUtils
import com.rr.aido.ui.circletosearch.utils.UIPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import com.rr.aido.data.DataStoreManager
import com.rr.aido.data.repository.GeminiRepository
import com.rr.aido.data.repository.Result
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleToSearchScreen(
    screenshot: Bitmap?,
    dataStoreManager: DataStoreManager,
    geminiRepository: GeminiRepository,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val uiPreferences = remember { UIPreferences(context) }

    var isTextSelectionMode by remember { mutableStateOf(false) }
    val textNodes = remember { mutableStateListOf<TextNode>() }
    val clipboardManager = LocalClipboardManager.current
    var showTextDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val nodes = TextRepository.getTextNodes()
        textNodes.addAll(nodes)
    }

    LaunchedEffect(screenshot) {
        if (screenshot != null) {
            try {
                val image = InputImage.fromBitmap(screenshot, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val ocrNodes = visionText.textBlocks.mapNotNull { block ->
                            block.boundingBox?.let { rect ->
                                TextNode(block.text, rect)
                            }
                        }
                        textNodes.addAll(ocrNodes)
                    }
                    .addOnFailureListener { e ->
                        Log.e("CircleToSearch", "OCR Failed", e)
                    }
            } catch (e: Exception) {
                Log.e("CircleToSearch", "Error identifying text", e)
            }
        }
    }
    
    val preferredOrder = remember(uiPreferences.getSearchEngineOrder()) {
        val allEngines = SearchEngine.values()
        val orderString = uiPreferences.getSearchEngineOrder()
        if (orderString == null) allEngines
        else {
            val preferredNames = orderString.split(",")
            val ordered = mutableListOf<SearchEngine>()
            preferredNames.forEach { name ->
                allEngines.find { it.name == name }?.let { ordered.add(it) }
            }
            allEngines.forEach { if (!ordered.contains(it)) ordered.add(it) }
            ordered
        }
    }
    val searchEngines = preferredOrder

    var showSettingsScreen by remember { mutableStateOf(false) }
    // Friendly Message Logic Removed
    
    var selectedEngine by remember(searchEngines) { mutableStateOf<SearchEngine>(searchEngines.first()) }
    var searchUrl by remember { mutableStateOf<String?>(null) }
    var hostedImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val initialDesktopMode = uiPreferences.isDesktopMode()
    var desktopModeEngines by remember { mutableStateOf<Set<SearchEngine>>(if(initialDesktopMode) searchEngines.toSet() else emptySet()) }
    
    var isDarkMode by remember { mutableStateOf(uiPreferences.isDarkMode()) }
    var showGradientBorder by remember { mutableStateOf(uiPreferences.isShowGradientBorder()) }
    val initializedEngines = remember { mutableStateListOf<SearchEngine>() }
    
    fun isDesktop(engine: SearchEngine) = desktopModeEngines.contains(engine)
    
    LaunchedEffect(isDarkMode) { uiPreferences.setDarkMode(isDarkMode) }
    LaunchedEffect(showGradientBorder) { uiPreferences.setShowGradientBorder(showGradientBorder) }
    
    val preloadedUrls = remember { mutableMapOf<SearchEngine, String>() }
    val webViews = remember { mutableMapOf<SearchEngine, android.webkit.WebView>() }
    
    LaunchedEffect(desktopModeEngines) {
        webViews.forEach { (engine, wv) ->
             val isDesktop = desktopModeEngines.contains(engine)
             val newUserAgent = if (isDesktop) {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else {
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            }
            if (wv.settings.userAgentString != newUserAgent) {
                wv.settings.userAgentString = newUserAgent
                wv.reload()
            }
        }
    }
    
    LaunchedEffect(isDarkMode) {
        webViews.values.forEach { wv ->
            try {
                if (isDarkMode) {
                    wv.webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            val darkModeCSS = "javascript:(function() { var style = document.createElement('style'); style.innerHTML = 'html { filter: invert(1) hue-rotate(180deg) !important; background: #000 !important; } img, video, [style*=\"background-image\"] { filter: invert(1) hue-rotate(180deg) !important; }'; document.head.appendChild(style); })()"
                            view?.loadUrl(darkModeCSS)
                        }
                    }
                } else {
                    wv.webViewClient = android.webkit.WebViewClient()
                }
                wv.reload()
            } catch (e: Exception) {
                Log.e("CircleToSearch", "Error updating dark mode", e)
            }
        }
    }
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Hidden, skipHiddenState = false)
    )

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    
    fun createWebView(ctx: android.content.Context, engine: SearchEngine): android.webkit.WebView {
        return android.webkit.WebView(ctx).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                userAgentString = if (isDesktop(engine)) {
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                } else {
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = android.webkit.WebViewClient()
            isNestedScrollingEnabled = true
            setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }

    BackHandler(enabled = true) {
        val currentWebView = webViews[selectedEngine]
        if (currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack()
        } else if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
             scope.launch { scaffoldState.bottomSheetState.partialExpand() }
        } else if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
             scope.launch { scaffoldState.bottomSheetState.hide() }
        } else {
            onClose()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * 0.55f),
        sheetContainerColor = Color(0xFF1F1F1F),
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetDragHandle = { 
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f), width = 32.dp, height = 3.dp)
        },
        sheetSwipeEnabled = true,
        sheetContent = {
            SearchResultsSheet(
                searchEngines = searchEngines,
                selectedEngine = selectedEngine,
                initializedEngines = initializedEngines,
                preloadedUrls = preloadedUrls,
                webViews = webViews,
                isLoading = isLoading,
                isDesktop = ::isDesktop,
                isDarkMode = isDarkMode,
                onEngineSelected = { engine ->
                    selectedEngine = engine
                    if (!initializedEngines.contains(engine)) {
                        initializedEngines.add(engine)
                    }
                },
                createWebView = ::createWebView
            )
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            if (screenshot != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = screenshot.asImageBitmap(),
                        contentDescription = "Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = OverlayGradientColors.map { it.copy(alpha = 0.1f) })))
                }
            }

            if (showGradientBorder) {
                Box(modifier = Modifier.fillMaxSize().border(width = 8.dp, brush = Brush.verticalGradient(colors = OverlayGradientColors), shape = RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)))
            }

            SearchOverlay(
                isTextSelectionMode = isTextSelectionMode,
                textNodes = textNodes,
                onTextSelected = { text -> showTextDialog = text },
                onSelectionComplete = { rect ->
                    selectedBitmap = ImageUtils.cropBitmap(screenshot!!, rect)
                    isSearching = true
                },
                onResetSelection = {
                    selectedBitmap = null
                    isSearching = false
                }
            )

            TopControlBar(
                selectedEngine = selectedEngine,
                desktopModeEngines = desktopModeEngines,
                isDarkMode = isDarkMode,
                showGradientBorder = showGradientBorder,
                onClose = onClose,
                onToggleDesktopMode = {
                    val newSet = desktopModeEngines.toMutableSet()
                    if (newSet.contains(selectedEngine)) newSet.remove(selectedEngine) else newSet.add(selectedEngine)
                    desktopModeEngines = newSet
                },
                onToggleDarkMode = { isDarkMode = !isDarkMode },
                onToggleGradientBorder = { showGradientBorder = !showGradientBorder },
                onRefresh = { webViews[selectedEngine]?.reload() },
                onCopyUrl = {
                    val currentUrl = webViews[selectedEngine]?.url ?: searchUrl
                    currentUrl?.let { 
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenInBrowser = {
                    val url = webViews[selectedEngine]?.url ?: searchUrl
                    url?.let {
                        try {
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(it)))
                        } catch (e: Exception) {
                            Log.e("CircleToSearch", "Failed to open browser", e)
                        }
                    }
                },
                onOpenSettings = { showSettingsScreen = true }
            )

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomControlBar(
                    selectedBitmap = selectedBitmap,
                    isTextSelectionMode = isTextSelectionMode,
                    isLensOnlyMode = uiPreferences.isUseGoogleLensOnly(),
                    onExpandSheet = { scope.launch { scaffoldState.bottomSheetState.expand() } },
                    onToggleTextSelection = {
                        isTextSelectionMode = !isTextSelectionMode
                    },
                    onGoogleLensClick = {
                        if (screenshot != null) {
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val path = ImageUtils.saveBitmap(context, screenshot)
                                searchWithGoogleLens(android.net.Uri.fromFile(File(path)), context)
                            }
                            onClose()
                        }
                    }
                )
            }

            LaunchedEffect(selectedBitmap) {
                hostedImageUrl = null
                searchUrl = null
                preloadedUrls.clear()
                initializedEngines.clear()
                webViews.values.forEach { it.destroy() }
                webViews.clear()
            }

            LaunchedEffect(selectedBitmap, hostedImageUrl) {
                if (selectedBitmap != null) {
                    isLoading = true
                    if (uiPreferences.isUseGoogleLensOnly()) {
                        val path = ImageUtils.saveBitmap(context, selectedBitmap!!)
                        if (searchWithGoogleLens(android.net.Uri.fromFile(File(path)), context)) {
                            onClose()
                            return@LaunchedEffect
                        }
                    }
                    scope.launch { scaffoldState.bottomSheetState.expand() }
                    if (hostedImageUrl == null) {
                        ImageSearchUploader.uploadToImageHost(selectedBitmap!!)?.let { hostedImageUrl = it } ?: run { isLoading = false; return@LaunchedEffect }
                    }
                    searchEngines.forEach { engine ->
                        if (!preloadedUrls.containsKey(engine)) {
                            val url = if (engine.isDirectUpload) {
                                 when (engine) {
                                    SearchEngine.Perplexity -> ImageSearchUploader.getPerplexityUrl(hostedImageUrl!!)
                                    SearchEngine.ChatGPT -> ImageSearchUploader.getChatGPTUrl(hostedImageUrl!!)
                                    else -> null
                                }
                            } else {
                                 when (engine) {
                                    SearchEngine.Google -> ImageSearchUploader.getGoogleLensUrl(hostedImageUrl!!)
                                    SearchEngine.Bing -> ImageSearchUploader.getBingUrl(hostedImageUrl!!)
                                    SearchEngine.Yandex -> ImageSearchUploader.getYandexUrl(hostedImageUrl!!)
                                    SearchEngine.TinEye -> ImageSearchUploader.getTinEyeUrl(hostedImageUrl!!)
                                    else -> null
                                }
                            }
                            url?.let { preloadedUrls[engine] = it }
                        }
                    }
                    preloadedUrls[selectedEngine]?.let { searchUrl = it }
                    if (!initializedEngines.contains(selectedEngine)) initializedEngines.add(selectedEngine)
                    isLoading = false
                }
            }
            
            LaunchedEffect(selectedEngine, preloadedUrls) {
                preloadedUrls[selectedEngine]?.let { searchUrl = it }
            }

            if (showSettingsScreen) {
                SettingsScreen(uiPreferences = uiPreferences, onDismissRequest = { showSettingsScreen = false })
            }
            
            if (showTextDialog != null) {
                var showTriggerSelection by remember { mutableStateOf(false) }
                var isProcessingTrigger by remember { mutableStateOf(false) }
                var triggerResult by remember { mutableStateOf<String?>(null) }
                
                if (triggerResult != null) {
                    AlertDialog(
                        onDismissRequest = { triggerResult = null },
                        title = { Text("AI Result") },
                        text = { SelectionContainer { Text(triggerResult!!) } },
                        confirmButton = {
                            TextButton(onClick = {
                                clipboardManager.setText(AnnotatedString(triggerResult!!))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                triggerResult = null
                            }) { Text("Copy") }
                        },
                        dismissButton = {
                            TextButton(onClick = { triggerResult = null }) { Text("Close") }
                        }
                    )
                } else if (showTriggerSelection) {
                    // Fetch triggers
                    val triggers = produceState(initialValue = emptyList<String>()) {
                        val settings = dataStoreManager.settingsFlow.first()
                        val preprompts = dataStoreManager.prepromptsFlow.first()
                        val list = mutableListOf<String>()
                        if (settings.isSmartReplyEnabled) list.add(settings.smartReplyTrigger)
                        if (settings.isToneRewriteEnabled) list.add(settings.toneRewriteTrigger)
                        list.addAll(preprompts.map { it.trigger })
                        value = list
                    }

                    AlertDialog(
                        onDismissRequest = { showTriggerSelection = false },
                        title = { Text("Select Trigger") },
                        text = {
                            if (triggers.value.isEmpty()) {
                                Text("No triggers found. Add them in settings.")
                            } else {
                                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                                    androidx.compose.foundation.lazy.LazyColumn {
                                        items(triggers.value.size) { index ->
                                            val trigger = triggers.value[index]
                                            ListItem(
                                                headlineContent = { Text(trigger) },
                                                modifier = Modifier.clickable {
                                                    showTriggerSelection = false
                                                    isProcessingTrigger = true
                                                    scope.launch {
                                                        try {
                                                            val settings = dataStoreManager.settingsFlow.first()
                                                            val preprompts = dataStoreManager.prepromptsFlow.first()
                                                            val text = showTextDialog!!
                                                            
                                                            val prompt = when(trigger) {
                                                                settings.smartReplyTrigger -> "Generate a smart reply for: $text" 
                                                                settings.toneRewriteTrigger -> "Rewrite this text to be more polite: $text"
                                                                else -> {
                                                                    val p = preprompts.find { it.trigger == trigger }
                                                                    p?.instruction?.replace("{text}", text) ?: "Analyze this: $text"
                                                                }
                                                            }
                                                            
                                                            // Check response language
                                                            val finalPrompt = if (settings.responseLanguage != "English" && settings.responseLanguage != "None") {
                                                                "(Please reply in ${settings.responseLanguage}) $prompt"
                                                            } else {
                                                                prompt
                                                            }

                                                            val result = geminiRepository.sendPrompt(
                                                                provider = settings.provider,
                                                                apiKey = settings.apiKey,
                                                                model = settings.selectedModel,
                                                                prompt = finalPrompt,
                                                                builtInProviderUrl = settings.builtInProvider?.apiUrl ?: ""
                                                            )
                                                            
                                                            triggerResult = if (result is Result.Success<*>) result.data.toString() else "Error: ${result}"
                                                        } catch (e: Exception) {
                                                            triggerResult = "Error: ${e.message}"
                                                        } finally {
                                                            isProcessingTrigger = false
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showTriggerSelection = false }) { Text("Cancel") }
                        }
                    )
                } else if (isProcessingTrigger) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Processing...") },
                        text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
                        confirmButton = {}
                    )
                } else {
                    // Main Text Selection Dialog
                    AlertDialog(
                        onDismissRequest = { showTextDialog = null },
                        icon = { Icon(Icons.Default.TextFormat, null) },
                        title = { Text("Selected Text") },
                        text = { SelectionContainer { Text(showTextDialog!!) } },
                        confirmButton = {
                            Row {
                                TextButton(onClick = { showTriggerSelection = true }) { 
                                    Text("Trigger") 
                                }
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(showTextDialog!!))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    showTextDialog = null
                                }) { Text("Copy") }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTextDialog = null }) { Text("Close") }
                        }
                    )
                }
            }
        }
    }
}
