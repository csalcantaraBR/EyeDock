package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "App Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Camera Settings
            item {
                SettingsSection("Camera Settings")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Videocam,
                    title = "Default Stream Quality",
                    subtitle = "Main Stream",
                    onClick = { /* TODO: Stream quality settings */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Timer,
                    title = "Connection Timeout",
                    subtitle = "30 seconds",
                    onClick = { /* TODO: Timeout settings */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Security Settings",
                    subtitle = "Encrypted storage",
                    onClick = { /* TODO: Security settings */ }
                )
            }
            
            // Storage Settings
            item {
                SettingsSection("Storage")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Recording Storage",
                    subtitle = "Internal Storage",
                    onClick = { /* TODO: Storage settings */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Auto Cleanup",
                    subtitle = "Enabled",
                    onClick = { /* TODO: Cleanup settings */ }
                )
            }
            
            // Network Settings
            item {
                SettingsSection("Network")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Wifi,
                    title = "Network Discovery",
                    subtitle = "Enabled",
                    onClick = { /* TODO: Discovery settings */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "Auto Reconnect",
                    subtitle = "Enabled",
                    onClick = { /* TODO: Reconnect settings */ }
                )
            }
            
            // About
            item {
                SettingsSection("About")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = { /* TODO: Version info */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Documentation and FAQ",
                    onClick = { /* TODO: Help screen */ }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
