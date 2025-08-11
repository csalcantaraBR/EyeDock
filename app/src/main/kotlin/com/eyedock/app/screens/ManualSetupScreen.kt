package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eyedock.app.viewmodels.AddCameraViewModel
import com.eyedock.app.viewmodels.AddCameraUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSetupScreen(
    onNavigateBack: () -> Unit,
    onCameraAdded: () -> Unit,
    prefillIp: String? = null,
    viewModel: AddCameraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formData by viewModel.formData.collectAsState()
    
    // Pre-fill form with IP if provided
    LaunchedEffect(prefillIp) {
        prefillIp?.let { ip ->
            viewModel.prefillForm(ip)
        }
    }
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is AddCameraUiState.SaveSuccess -> {
                onCameraAdded()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual Setup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Add Camera Manually",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter your camera details to connect",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Form
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Camera Name
                    OutlinedTextField(
                        value = formData.name,
                        onValueChange = { viewModel.updateField("name", it) },
                        label = { Text("Camera Name") },
                        placeholder = { Text("e.g., Front Door Camera") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // IP Address
                    OutlinedTextField(
                        value = formData.ip,
                        onValueChange = { viewModel.updateField("ip", it) },
                        label = { Text("IP Address") },
                        placeholder = { Text("192.168.1.100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Port
                    OutlinedTextField(
                        value = formData.port,
                        onValueChange = { viewModel.updateField("port", it) },
                        label = { Text("RTSP Port") },
                        placeholder = { Text("554") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Username
                    OutlinedTextField(
                        value = formData.user,
                        onValueChange = { viewModel.updateField("user", it) },
                        label = { Text("Username (Optional)") },
                        placeholder = { Text("admin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Password
                    OutlinedTextField(
                        value = formData.password,
                        onValueChange = { viewModel.updateField("password", it) },
                        label = { Text("Password (Optional)") },
                        placeholder = { Text("Enter password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Status messages
            when (val currentState = uiState) {
                is AddCameraUiState.ConnectionSuccess -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                is AddCameraUiState.ConnectionError -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                is AddCameraUiState.SaveError -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                else -> {}
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddCameraUiState.TestingConnection
                ) {
                    if (uiState is AddCameraUiState.TestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Test Connection")
                }
                
                Button(
                    onClick = { viewModel.saveCamera() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddCameraUiState.Saving
                ) {
                    if (uiState is AddCameraUiState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save Camera")
                }
            }
        }
    }
}
