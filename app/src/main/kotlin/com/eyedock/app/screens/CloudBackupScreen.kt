package com.eyedock.app.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eyedock.app.domain.interfaces.CloudStorage
import com.eyedock.app.viewmodels.CloudBackupViewModel
import com.eyedock.app.viewmodels.CloudBackupViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CloudBackupViewModel = viewModel(
        factory = CloudBackupViewModelFactory(context)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val cloudFiles by viewModel.cloudFiles.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val success = viewModel.handleGoogleSignInResult(result.data)
        if (!success) {
            // Handle sign-in failure
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Backup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            item {
                StatusCard(uiState = uiState)
            }
            
            // Service Selection
            item {
                ServiceSelectionCard(
                    availableServices = availableServices,
                    onServiceSelected = { serviceType ->
                        viewModel.initializeCloudStorage(serviceType)
                    }
                )
            }
            
            // Authentication Card
            item {
                AuthenticationCard(
                    uiState = uiState,
                    onAuthenticate = {
                        // Launch Google Sign-In
                        val signInIntent = viewModel.getGoogleSignInIntent()
                        if (signInIntent != null) {
                            googleSignInLauncher.launch(signInIntent)
                        } else {
                            // Fallback to regular authentication
                            viewModel.authenticate()
                        }
                    },
                    onSignOut = {
                        viewModel.signOut()
                    }
                )
            }
            
            // Storage Info Card
            item {
                storageInfo?.let { info ->
                    StorageInfoCard(storageInfo = info)
                }
            }
            
            // Cloud Files
            if (cloudFiles.isNotEmpty()) {
                item {
                    Text(
                        text = "Cloud Files",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                items(cloudFiles) { file ->
                    CloudFileCard(
                        file = file,
                        onDelete = { fileId ->
                            viewModel.deleteCloudFile(fileId)
                        },
                        onDownload = { fileId ->
                            viewModel.getDownloadUrl(fileId) { url ->
                                // Handle download URL
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(uiState: CloudBackupViewModel.CloudBackupUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (uiState) {
                is CloudBackupViewModel.CloudBackupUiState.Idle -> {
                    Text("Not connected to any cloud service")
                }
                is CloudBackupViewModel.CloudBackupUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Initializing...")
                    }
                }
                is CloudBackupViewModel.CloudBackupUiState.Authenticating -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authenticating...")
                    }
                }
                is CloudBackupViewModel.CloudBackupUiState.Authenticated -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connected to ${uiState.serviceType.name}")
                    }
                }
                is CloudBackupViewModel.CloudBackupUiState.Uploading -> {
                    Column {
                        Text("Uploading: ${uiState.fileName}")
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = uiState.progress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is CloudBackupViewModel.CloudBackupUiState.UploadSuccess -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload successful")
                    }
                }
                is CloudBackupViewModel.CloudBackupUiState.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceSelectionCard(
    availableServices: List<CloudStorage.ServiceType>,
    onServiceSelected: (CloudStorage.ServiceType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Cloud Service",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            availableServices.forEach { service ->
                Button(
                    onClick = { onServiceSelected(service) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        when (service) {
                            CloudStorage.ServiceType.GOOGLE_DRIVE -> Icons.Default.Cloud
                            CloudStorage.ServiceType.ONEDRIVE -> Icons.Default.Cloud
                            CloudStorage.ServiceType.SHAREPOINT -> Icons.Default.Cloud
                            CloudStorage.ServiceType.DROPBOX -> Icons.Default.Cloud
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(service.name.replace("_", " "))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AuthenticationCard(
    uiState: CloudBackupViewModel.CloudBackupUiState,
    onAuthenticate: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Authentication",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (uiState) {
                is CloudBackupViewModel.CloudBackupUiState.Authenticated -> {
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
                else -> {
                    Button(
                        onClick = onAuthenticate,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is CloudBackupViewModel.CloudBackupUiState.Authenticating
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In")
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageInfoCard(storageInfo: CloudStorage.StorageInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Storage Info",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Total: ${formatBytes(storageInfo.totalSpace)}")
            Text("Used: ${formatBytes(storageInfo.usedSpace)}")
            Text("Available: ${formatBytes(storageInfo.availableSpace)}")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = storageInfo.usedSpace.toFloat() / storageInfo.totalSpace.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CloudFileCard(
    file: CloudStorage.CloudFile,
    onDelete: (String) -> Unit,
    onDownload: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatBytes(file.size),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(file.modifiedTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { onDownload(file.id) }) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
            
            IconButton(onClick = { onDelete(file.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "%.1f %s".format(size, units[unitIndex])
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}
