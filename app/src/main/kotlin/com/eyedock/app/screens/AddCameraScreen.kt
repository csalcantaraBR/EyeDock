package com.eyedock.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eyedock.app.domain.model.Auth
import com.eyedock.app.viewmodels.CamerasViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCameraScreen(
    onNavigateBack: () -> Unit,
    viewModel: CamerasViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("") }
    var rtspUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var showQRScanner by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Camera") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QR Code Scanner Button
            OutlinedButton(
                onClick = { showQRScanner = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan QR Code")
            }

            // Camera Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Camera Name") },
                placeholder = { Text("e.g., Living Room Camera") },
                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // IP Address
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address") },
                placeholder = { Text("192.168.1.100") },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            // RTSP URL
            OutlinedTextField(
                value = rtspUrl,
                onValueChange = { rtspUrl = it },
                label = { Text("RTSP URL") },
                placeholder = { Text("rtsp://192.168.1.100:554/stream1") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username (Optional)") },
                placeholder = { Text("admin") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (Optional)") },
                placeholder = { Text("Enter password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Test Connection Button
            OutlinedButton(
                onClick = {
                    isTesting = true
                    scope.launch {
                        kotlinx.coroutines.delay(2000) // Simulate test
                        isTesting = false
                    }
                },
                enabled = ipAddress.isNotBlank() && rtspUrl.isNotBlank() && !isTesting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.WifiTethering, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTesting) "Testing..." else "Test Connection")
            }

            // Add Camera Button
            Button(
                onClick = {
                    if (name.isNotBlank() && ipAddress.isNotBlank() && rtspUrl.isNotBlank()) {
                        val auth = if (username.isNotBlank() && password.isNotBlank()) {
                            Auth(username, password)
                        } else null
                        viewModel.addCamera(name, ipAddress, rtspUrl, auth)
                        onNavigateBack()
                    }
                },
                enabled = name.isNotBlank() && ipAddress.isNotBlank() && rtspUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Camera")
            }

            // Help Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to find your camera details:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Check the camera's manual or sticker\n" +
                               "• Look for QR code on the camera\n" +
                               "• Use the camera's web interface\n" +
                               "• Common RTSP URLs:\n" +
                               "  - rtsp://IP:554/stream1\n" +
                               "  - rtsp://IP:554/live\n" +
                               "  - rtsp://IP:554/h264Preview_01_main",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // QR Scanner Dialog (placeholder)
    if (showQRScanner) {
        AlertDialog(
            onDismissRequest = { showQRScanner = false },
            title = { Text("QR Code Scanner") },
            text = { Text("QR code scanning functionality will be implemented here.") },
            confirmButton = {
                TextButton(onClick = { showQRScanner = false }) {
                    Text("OK")
                }
            }
        )
    }
}
