package com.example.rokidaiassistant.activities.bluetooth

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.rokidaiassistant.activities.aiassistant.AIAssistantActivity
import com.example.rokidaiassistant.ui.theme.RokidAIAssistantTheme

class BluetoothInitActivity : ComponentActivity() {

    private val bluetoothViewModel: BluetoothInitViewModel by viewModels()
    private var pendingPermissionAction: (() -> Unit)? = null
    private var bluetoothPermissionsGranted by mutableStateOf(false)

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        bluetoothPermissionsGranted = hasRequiredBluetoothPermissions()
        val pendingAction = pendingPermissionAction
        pendingPermissionAction = null
        if (bluetoothPermissionsGranted) {
            pendingAction?.invoke()
        } else {
            bluetoothViewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothPermissionsGranted = hasRequiredBluetoothPermissions()
        setContent {
            RokidAIAssistantTheme {
                val uiState by bluetoothViewModel.uiState.collectAsStateWithLifecycle()
                
                // Listen for connection success
                LaunchedEffect(uiState.isConnected) {
                    if (uiState.isConnected) {
                        navigateToAIAssistant()
                    }
                }
                
                BluetoothInitScreen(
                    uiState = uiState,
                    canAccessDeviceDetails = bluetoothPermissionsGranted,
                    onStartScan = {
                        runWithBluetoothPermissions {
                            bluetoothViewModel.startScan(applicationContext)
                        }
                    },
                    onStopScan = {
                        runWithBluetoothPermissions {
                            bluetoothViewModel.stopScan(applicationContext)
                        }
                    },
                    onDeviceClick = { device ->
                        runWithBluetoothPermissions {
                            bluetoothViewModel.connectToDevice(applicationContext, device)
                        }
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
    
    private fun navigateToAIAssistant() {
        startActivity(Intent(this, AIAssistantActivity::class.java))
        finish()
    }

    private fun runWithBluetoothPermissions(action: () -> Unit) {
        if (hasRequiredBluetoothPermissions()) {
            bluetoothPermissionsGranted = true
            action()
            return
        }

        bluetoothPermissionsGranted = false
        pendingPermissionAction = action
        bluetoothPermissionLauncher.launch(requiredBluetoothPermissions())
    }

    private fun hasRequiredBluetoothPermissions(): Boolean {
        return requiredBluetoothPermissions().all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredBluetoothPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothInitScreen(
    uiState: BluetoothUiState,
    canAccessDeviceDetails: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Glasses") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isScanning) {
                        IconButton(onClick = onStopScan) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop Scan")
                        }
                    } else {
                        IconButton(onClick = onStartScan) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status display
            when {
                uiState.isConnecting -> {
                    ConnectingState(deviceName = uiState.connectedDeviceName ?: "Glasses")
                }
                uiState.isScanning -> {
                    ScanningState()
                }
                uiState.error != null -> {
                    ErrorState(message = uiState.error, onRetry = onStartScan)
                }
            }
            
            // Device list
            if (!uiState.isConnecting) {
                if (
                    uiState.devices.isEmpty() &&
                    !uiState.isScanning &&
                    uiState.error == null
                ) {
                    EmptyState(onStartScan = onStartScan)
                } else {
                    DeviceList(
                        devices = uiState.devices,
                        canAccessDeviceDetails = canAccessDeviceDetails,
                        onDeviceClick = onDeviceClick
                    )
                }
            }
        }
    }
    
    // Auto-start scan on first entry
    LaunchedEffect(Unit) {
        onStartScan()
    }
}

@Composable
fun ScanningState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("Scanning for nearby Rokid glasses...")
    }
}

@Composable
fun ConnectingState(deviceName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connecting to $deviceName...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please wait",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyState(onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Rokid glasses found",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please ensure glasses are powered on and Bluetooth is enabled",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStartScan) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rescan")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceList(
    devices: List<BluetoothDevice>,
    canAccessDeviceDetails: Boolean,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Discovered Devices",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(devices) { device ->
            DeviceItem(
                device = device,
                canAccessDeviceDetails = canAccessDeviceDetails,
                onClick = { onDeviceClick(device) }
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    canAccessDeviceDetails: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (canAccessDeviceDetails) {
                        device.name ?: "Unknown Device"
                    } else {
                        "Bluetooth permission required"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (canAccessDeviceDetails) device.address else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
