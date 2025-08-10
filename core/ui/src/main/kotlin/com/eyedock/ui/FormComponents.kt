package com.eyedock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * GREEN PHASE - Implementação mínima de AddCameraForm
 */
@Composable
fun AddCameraForm(
    name: String,
    ip: String,
    port: String,
    user: String,
    password: String,
    onFieldChange: (field: String, value: String) -> Unit,
    onSave: () -> Unit,
    showValidationErrors: Boolean = false,
    modifier: Modifier = Modifier,
    testTag: String = "add_camera_form"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Camera",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { 
                contentDescription = "Add camera form" 
            }
        )
        
        // Camera Name
        OutlinedTextField(
            value = name,
            onValueChange = { onFieldChange("name", it) },
            label = { Text("Camera Name") },
            isError = showValidationErrors && name.isEmpty(),
            supportingText = if (showValidationErrors && name.isEmpty()) {
                { Text("Camera name is required") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // IP Address
        OutlinedTextField(
            value = ip,
            onValueChange = { onFieldChange("ip", it) },
            label = { Text("IP Address") },
            placeholder = { Text("192.168.1.100") },
            isError = showValidationErrors && ip.isEmpty(),
            supportingText = if (showValidationErrors && ip.isEmpty()) {
                { Text("IP address is required") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Port
        OutlinedTextField(
            value = port,
            onValueChange = { onFieldChange("port", it) },
            label = { Text("RTSP Port") },
            placeholder = { Text("554") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Username
        OutlinedTextField(
            value = user,
            onValueChange = { onFieldChange("user", it) },
            label = { Text("Username") },
            placeholder = { Text("admin") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { onFieldChange("password", it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* Test connection */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test")
            }
            
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

/**
 * GREEN PHASE - Implementação mínima de StoragePicker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoragePicker(
    options: List<StorageOption>,
    selectedOption: StorageOption?,
    onOptionSelected: (StorageOption) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "storage_picker"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Select Storage Location",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { 
                contentDescription = "Storage location picker" 
            }
        )
        
        Text(
            text = "Choose where to save your camera recordings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        options.forEach { option ->
            StorageOptionCard(
                option = option,
                isSelected = option == selectedOption,
                onSelect = { onOptionSelected(option) }
            )
        }
    }
}

/**
 * Card para opção de storage
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorageOptionCard(
    option: StorageOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_save),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * GREEN PHASE - Implementação mínima de QrScannerView
 */
@Composable
fun QrScannerView(
    onQrDetected: (String) -> Unit,
    onTorchToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "qr_scanner"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(testTag)
            .semantics { 
                contentDescription = "QR code scanner view" 
            }
    ) {
        // Placeholder para camera preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera Preview",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Overlay controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Align QR code from camera box or app",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Torch toggle
                IconButton(
                    onClick = onTorchToggle,
                    modifier = Modifier.semantics { 
                        contentDescription = "Toggle flashlight" 
                    }
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_view),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
                
                // Gallery import
                IconButton(
                    onClick = { /* Import from gallery */ },
                    modifier = Modifier.semantics { 
                        contentDescription = "Import QR from gallery" 
                    }
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_gallery),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}

/**
 * Opção de storage
 */
data class StorageOption(
    val name: String,
    val description: String,
    val type: String = "unknown"
)

@Preview
@Composable
private fun AddCameraFormPreview() {
    MaterialTheme {
        AddCameraForm(
            name = "",
            ip = "",
            port = "554",
            user = "",
            password = "",
            onFieldChange = { _, _ -> },
            onSave = { },
            showValidationErrors = true
        )
    }
}

@Preview
@Composable
private fun StoragePickerPreview() {
    MaterialTheme {
        StoragePicker(
            options = listOf(
                StorageOption("Internal Storage", "32 GB available"),
                StorageOption("SD Card", "128 GB available"),
                StorageOption("USB Drive", "64 GB available")
            ),
            selectedOption = null,
            onOptionSelected = { }
        )
    }
}
