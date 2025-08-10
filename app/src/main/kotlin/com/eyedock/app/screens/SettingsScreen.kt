package com.eyedock.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onOpenStoragePicker: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoUploadEnabled by remember { mutableStateOf(false) }
    var retentionDays by remember { mutableIntStateOf(30) }
    var recordingQuality by remember { mutableStateOf("1080p") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Storage Section
            SettingsSection(
                title = "Storage",
                modifier = Modifier.testTag("storage_settings")
            ) {
                SettingsItem(
                    title = "Storage Location",
                    subtitle = "Choose where recordings are saved",
                    icon = Icons.Default.Storage,
                    onClick = onOpenStoragePicker
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                CurrentStorageInfo()

                SettingsItem(
                    title = "Retention Policy",
                    subtitle = "Automatically delete old recordings",
                    icon = Icons.Default.Schedule
                ) {
                    DropdownMenuBox(
                        selectedValue = "$retentionDays days",
                        options = listOf("7 days", "30 days", "90 days", "Never"),
                        onSelectionChange = { selection ->
                            retentionDays = when (selection) {
                                "7 days" -> 7
                                "30 days" -> 30
                                "90 days" -> 90
                                else -> 0
                            }
                        }
                    )
                }
            }

            Divider()

            // Recording Section
            SettingsSection(title = "Recording") {
                SettingsItem(
                    title = "Default Quality",
                    subtitle = "Video resolution for new cameras",
                    icon = Icons.Default.VideoSettings
                ) {
                    DropdownMenuBox(
                        selectedValue = recordingQuality,
                        options = listOf("720p", "1080p", "4K"),
                        onSelectionChange = { recordingQuality = it }
                    )
                }

                SettingsItem(
                    title = "Auto Upload",
                    subtitle = "Upload recordings to cloud storage",
                    icon = Icons.Default.CloudUpload
                ) {
                    Switch(
                        checked = autoUploadEnabled,
                        onCheckedChange = { autoUploadEnabled = it }
                    )
                }
            }

            Divider()

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    title = "Motion Alerts",
                    subtitle = "Get notified when motion is detected",
                    icon = Icons.Default.NotificationsActive
                ) {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                if (notificationsEnabled) {
                    SettingsItem(
                        title = "Alert Sound",
                        subtitle = "Play sound for notifications",
                        icon = Icons.Default.VolumeUp,
                        indent = true
                    ) {
                        Switch(
                            checked = true,
                            onCheckedChange = { /* TODO */ }
                        )
                    }

                    SettingsItem(
                        title = "Vibration",
                        subtitle = "Vibrate for notifications",
                        icon = Icons.Default.Vibration,
                        indent = true
                    ) {
                        Switch(
                            checked = true,
                            onCheckedChange = { /* TODO */ }
                        )
                    }
                }
            }

            Divider()

            // Privacy Section
            SettingsSection(title = "Privacy & Security") {
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    icon = Icons.Default.Policy,
                    onClick = { /* TODO: Open privacy policy */ }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                SettingsItem(
                    title = "Data Export",
                    subtitle = "Export your data",
                    icon = Icons.Default.FileDownload,
                    onClick = { /* TODO: Export data */ }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                SettingsItem(
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    icon = Icons.Default.CleaningServices,
                    onClick = { /* TODO: Clear cache */ }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    subtitle = "EyeDock 1.0.0",
                    icon = Icons.Default.Info
                )

                SettingsItem(
                    title = "Diagnostics",
                    subtitle = "View app diagnostics and logs",
                    icon = Icons.Default.BugReport,
                    onClick = { /* TODO: Open diagnostics */ }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                SettingsItem(
                    title = "Help & Support",
                    subtitle = "Get help or contact support",
                    icon = Icons.Default.HelpOutline,
                    onClick = { /* TODO: Open help */ }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Add some bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    indent: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val itemModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .padding(
                start = if (indent) 32.dp else 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        content?.invoke()
    }
}

@Composable
fun CurrentStorageInfo() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("storage_picker"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Storage",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Internal Storage/EyeDock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Free Space",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2.4 GB / 32 GB",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = 0.85f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    selectedValue: String,
    options: List<String>,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .widthIn(min = 120.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
