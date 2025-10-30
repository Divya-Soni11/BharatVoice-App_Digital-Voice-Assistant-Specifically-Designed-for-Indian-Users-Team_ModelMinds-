package com.runanywhere.startup_hackathon20.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.managers.AppConfigManager
import com.runanywhere.startup_hackathon20.models.AssistanceMode
import com.runanywhere.startup_hackathon20.models.InstalledAppInfo
import com.runanywhere.startup_hackathon20.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for App Selection
class AppSelectionViewModel(
    private val appConfigManager: AppConfigManager
) : ViewModel() {

    private val _popularApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val popularApps: StateFlow<List<InstalledAppInfo>> = _popularApps.asStateFlow()

    private val _allApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val allApps: StateFlow<List<InstalledAppInfo>> = _allApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showAllApps = MutableStateFlow(false)
    val showAllApps: StateFlow<Boolean> = _showAllApps.asStateFlow()

    // Track enabled apps and modes in state for immediate UI updates
    private val _enabledApps = MutableStateFlow<Set<String>>(emptySet())
    val enabledApps: StateFlow<Set<String>> = _enabledApps.asStateFlow()

    private val _appModes = MutableStateFlow<Map<String, AssistanceMode>>(emptyMap())
    val appModes: StateFlow<Map<String, AssistanceMode>> = _appModes.asStateFlow()

    init {
        loadApps()
        loadPreferences()
    }

    private fun loadPreferences() {
        // Load current enabled apps and modes
        _enabledApps.value = appConfigManager.getEnabledApps()

        // Load modes for enabled apps
        val modes = mutableMapOf<String, AssistanceMode>()
        _enabledApps.value.forEach { packageName ->
            modes[packageName] = appConfigManager.getAssistanceMode(packageName)
        }
        _appModes.value = modes
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _popularApps.value = appConfigManager.getPopularInstalledApps()
                _allApps.value = appConfigManager.getInstalledApps(includeSystemApps = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleShowAllApps() {
        _showAllApps.value = !_showAllApps.value
    }

    fun isAppEnabled(packageName: String): Boolean {
        return packageName in _enabledApps.value
    }

    fun toggleApp(packageName: String) {
        val currentEnabled = _enabledApps.value.toMutableSet()
        val isCurrentlyEnabled = packageName in currentEnabled

        if (isCurrentlyEnabled) {
            currentEnabled.remove(packageName)
            // Remove mode when disabling
            val currentModes = _appModes.value.toMutableMap()
            currentModes.remove(packageName)
            _appModes.value = currentModes
        } else {
            currentEnabled.add(packageName)
            // Set default mode when enabling
            val currentModes = _appModes.value.toMutableMap()
            currentModes[packageName] = AssistanceMode.ON_DEMAND
            _appModes.value = currentModes
        }

        _enabledApps.value = currentEnabled
        appConfigManager.setAppEnabled(packageName, !isCurrentlyEnabled)
    }

    fun getAssistanceMode(packageName: String): AssistanceMode {
        return _appModes.value[packageName] ?: AssistanceMode.ON_DEMAND
    }

    fun setAssistanceMode(packageName: String, mode: AssistanceMode) {
        val currentModes = _appModes.value.toMutableMap()
        currentModes[packageName] = mode
        _appModes.value = currentModes
        appConfigManager.setAssistanceMode(packageName, mode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val popularApps by viewModel.popularApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showAllApps by viewModel.showAllApps.collectAsState()
    val enabledApps by viewModel.enabledApps.collectAsState()
    val appModes by viewModel.appModes.collectAsState()

    var selectedApp by remember { mutableStateOf<InstalledAppInfo?>(null) }

    key(enabledApps.size, appModes.size) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VVGradients.SoftGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Select Apps",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "ऐप्स चुनें",
                                style = MaterialTheme.typography.bodySmall,
                                color = VVColors.Gray600
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(VVSpacing.lg)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = VVSpacing.xl),
                        shape = RoundedCornerShape(VVRadius.xl),
                        colors = CardDefaults.cardColors(
                            containerColor = VVColors.Primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(VVSpacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(VVColors.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = VVColors.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(VVSpacing.lg))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Choose Your Apps",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VVColors.White
                                )
                                Text(
                                    "Enable voice assistance for selected apps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VVColors.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Popular Apps Section
                    if (popularApps.isNotEmpty()) {
                        SectionHeader(
                            title = "🌟 Popular Apps",
                            subtitle = "लोकप्रिय ऐप्स"
                        )

                        Spacer(modifier = Modifier.height(VVSpacing.md))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.heightIn(max = 800.dp),
                            horizontalArrangement = Arrangement.spacedBy(VVSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(VVSpacing.md)
                        ) {
                            items(
                                items = popularApps,
                                key = { it.packageName }
                            ) { app ->
                                val isEnabled = app.packageName in enabledApps
                                val mode = appModes[app.packageName] ?: AssistanceMode.ON_DEMAND

                                AppGridItem(
                                    app = app,
                                    isEnabled = isEnabled,
                                    assistanceMode = mode,
                                    onToggle = {
                                        viewModel.toggleApp(app.packageName)
                                    },
                                    onClick = {
                                        selectedApp = app
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(VVSpacing.xl))
                    }

                    // Show All Apps Button
                    OutlinedButton(
                        onClick = { viewModel.toggleShowAllApps() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(VVRadius.lg),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = VVColors.White
                        )
                    ) {
                        Icon(
                            if (showAllApps) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(VVSpacing.sm))
                        Text(
                            if (showAllApps) "Hide All Apps" else "Show All Apps",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // All Apps Section
                    AnimatedVisibility(
                        visible = showAllApps,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(VVSpacing.xl))

                            SectionHeader(
                                title = "📱 All Apps",
                                subtitle = "सभी ऐप्स"
                            )

                            Spacer(modifier = Modifier.height(VVSpacing.md))

                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = VVColors.Primary)
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.heightIn(max = 1200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(VVSpacing.md),
                                    verticalArrangement = Arrangement.spacedBy(VVSpacing.md)
                                ) {
                                    items(
                                        items = allApps,
                                        key = { it.packageName }
                                    ) { app ->
                                        val isEnabled = app.packageName in enabledApps
                                        val mode =
                                            appModes[app.packageName] ?: AssistanceMode.ON_DEMAND

                                        AppGridItem(
                                            app = app,
                                            isEnabled = isEnabled,
                                            assistanceMode = mode,
                                            onToggle = {
                                                viewModel.toggleApp(app.packageName)
                                            },
                                            onClick = {
                                                selectedApp = app
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(VVSpacing.xxxl))
                }
            }

            // App Settings Bottom Sheet
            selectedApp?.let { app ->
                val isEnabled = app.packageName in enabledApps
                val currentMode = appModes[app.packageName] ?: AssistanceMode.ON_DEMAND

                AppSettingsBottomSheet(
                    app = app,
                    isEnabled = isEnabled,
                    currentMode = currentMode,
                    onDismiss = { selectedApp = null },
                    onToggle = {
                        viewModel.toggleApp(app.packageName)
                    },
                    onModeChange = { mode ->
                        viewModel.setAssistanceMode(app.packageName, mode)
                    }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = VVColors.Gray900
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = VVColors.Gray600
        )
    }
}

@Composable
fun AppGridItem(
    app: InstalledAppInfo,
    isEnabled: Boolean,
    assistanceMode: AssistanceMode,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(VVRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) VVColors.PrimaryContainer else VVColors.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEnabled) VVElevation.md else VVElevation.sm
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(VVSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Icon
                app.icon?.let { drawable ->
                    Image(
                        bitmap = drawable.toBitmap(72, 72).asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(VVRadius.md))
                    )
                }

                Spacer(modifier = Modifier.height(VVSpacing.xs))

                // App Name
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = if (isEnabled) VVColors.Primary else VVColors.Gray700
                )

                // Mode Badge
                if (isEnabled) {
                    Spacer(modifier = Modifier.height(VVSpacing.xxs))
                    val badgeText = when (assistanceMode) {
                        AssistanceMode.ALWAYS_ON -> "AUTO"
                        AssistanceMode.ON_DEMAND -> "ON-TAP"
                        else -> ""
                    }
                    if (badgeText.isNotEmpty()) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = VVColors.Primary,
                            modifier = Modifier
                                .background(
                                    VVColors.Primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(VVRadius.sm)
                                )
                                .padding(horizontal = VVSpacing.xs, vertical = 2.dp)
                        )
                    }
                }
            }

            // Checkmark
            if (isEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(VVSpacing.xs)
                        .size(24.dp)
                        .background(VVColors.Success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Enabled",
                        tint = VVColors.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsBottomSheet(
    app: InstalledAppInfo,
    isEnabled: Boolean,
    currentMode: AssistanceMode,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onModeChange: (AssistanceMode) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VVColors.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VVSpacing.xl)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                app.icon?.let { drawable ->
                    Image(
                        bitmap = drawable.toBitmap(64, 64).asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(VVRadius.md))
                    )
                }

                Spacer(modifier = Modifier.width(VVSpacing.lg))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnabled) "✓ Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEnabled) VVColors.Success else VVColors.Gray500
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VVColors.White,
                        checkedTrackColor = VVColors.Success
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(VVSpacing.xl))

                Divider(color = VVColors.Gray200)

                Spacer(modifier = Modifier.height(VVSpacing.xl))

                // Assistance Mode Selection
                Text(
                    "Assistance Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "सहायता मोड",
                    style = MaterialTheme.typography.bodySmall,
                    color = VVColors.Gray600
                )

                Spacer(modifier = Modifier.height(VVSpacing.lg))

                // Always On Option
                ModeOptionCard(
                    title = "Always On",
                    subtitle = "Auto-starts when you open ${app.appName}",
                    hindiSubtitle = "ऐप खुलने पर स्वचालित रूप से शुरू होता है",
                    icon = Icons.Default.Star,
                    isSelected = currentMode == AssistanceMode.ALWAYS_ON,
                    onClick = { onModeChange(AssistanceMode.ALWAYS_ON) }
                )

                Spacer(modifier = Modifier.height(VVSpacing.md))

                // On Demand Option
                ModeOptionCard(
                    title = "On-Demand",
                    subtitle = "Activate with floating button or gesture",
                    hindiSubtitle = "फ्लोटिंग बटन या जेस्चर से सक्रिय करें",
                    icon = Icons.Default.Settings,
                    isSelected = currentMode == AssistanceMode.ON_DEMAND,
                    onClick = { onModeChange(AssistanceMode.ON_DEMAND) }
                )
            }

            Spacer(modifier = Modifier.height(VVSpacing.xl))
        }
    }
}

@Composable
fun ModeOptionCard(
    title: String,
    subtitle: String,
    hindiSubtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(VVRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VVColors.PrimaryContainer else VVColors.Gray50
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, VVColors.Primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VVSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSelected) VVColors.Primary else VVColors.Gray300,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = VVColors.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(VVSpacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) VVColors.Primary else VVColors.Gray900
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VVColors.Gray600
                )
                Text(
                    text = hindiSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = VVColors.Gray500
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = VVColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
