package com.example.meshmessagingapp

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import no.nordicsemi.android.mesh.MeshManagerApi
import no.nordicsemi.android.mesh.MeshManagerCallbacks
import no.nordicsemi.android.mesh.MeshNetwork
import no.nordicsemi.android.mesh.ApplicationKey
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.transport.GenericOnOffSet
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), MeshManagerCallbacks {

    private lateinit var chatTextView: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var meshManagerApi: MeshManagerApi
    private lateinit var appKey: ApplicationKey

    // Use the hardcoded group address 0xC000
    private val destinationAddress: Int = 0xC000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Initialise UI components
        chatTextView = findViewById(R.id.chatTextView)
        chatTextView.setTextColor(android.graphics.Color.BLACK)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        sendButton.isEnabled = false // Disable until network is imported/loaded

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialise MeshManagerApi and set callbacks
        meshManagerApi = MeshManagerApi(this)
        meshManagerApi.setMeshManagerCallbacks(this)

        // Import mesh network configuration from JSON file
        val imported = importMeshNetworkFromAssets("mesh_config.json")
        if (!imported) {
            appendMessage("Falling back to loading existing network...")
            meshManagerApi.loadMeshNetwork()
        }

        // Set up send button listener
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isBlank()) {
                sendTestMessage()
            } else {
                appendMessage("You: $messageText")
                messageEditText.text.clear()
                // Send message using the already configured appKey
                val state = true
                val genericOnOffMessage = GenericOnOffSet(appKey, state, 0)
                meshManagerApi.createMeshPdu(0xC000, genericOnOffMessage)
            }
        }
    }

    // Append messages to the chat area
    private fun appendMessage(message: String) {
        chatTextView.append("\n$message")
        chatTextView.scrollTo(0, chatTextView.bottom)
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    // Copies asset file to app's cache directory and returns a File reference.
    private fun copyAssetToFile(filename: String): File? {
        return try {
            val file = File(cacheDir, filename)
            assets.open(filename).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun importMeshNetworkFromAssets(filename: String): Boolean {
        val file = copyAssetToFile(filename)
        if (file == null) {
            appendMessage("Error copying asset file: $filename")
            return false
        }
        val uri = Uri.fromFile(file)
        try {
            meshManagerApi.importMeshNetwork(uri)
            runOnUiThread {
                appendMessage("Mesh network imported successfully from JSON.")
                configureAppKey()
                sendButton.isEnabled = true
            }
            return true
        } catch (e: Exception) {
            runOnUiThread {
                appendMessage("Error importing mesh network: ${e.message}")
            }
            return false
        }
    }

    private fun configureAppKey() {
        val keyHex = "8090BBC4A6443EB48C0C56FAB14FF036"
        val keyBytes = hexStringToByteArray(keyHex)
        val meshNetwork: MeshNetwork? = meshManagerApi.meshNetwork
        if (meshNetwork != null) {
            val existingKey = meshNetwork.appKeys.firstOrNull { it.key.contentEquals(keyBytes) }
            if (existingKey != null) {
                appKey = existingKey
                appendMessage("Using existing AppKey from imported network.")
            } else {
                val newKey = meshNetwork.createAppKey()
                appKey = newKey
                meshNetwork.addAppKey(appKey)
                appendMessage("New AppKey created and added to the network.")
            }
        } else {
            appendMessage("Error: Mesh network is null during AppKey configuration.")
        }
    }

    private fun sendTestMessage() {
        val meshNetwork: MeshNetwork? = meshManagerApi.meshNetwork
        if (meshNetwork != null) {
            val key = meshNetwork.getAppKey(0)
            if (key != null) {
                val testMsg = GenericOnOffSet(key, true, 0)
                meshManagerApi.createMeshPdu(0xC000, testMsg)
                appendMessage("Sent test message using getAppKey(0).")
            } else {
                appendMessage("Error: getAppKey(0) returned null.")
            }
        } else {
            appendMessage("Error: Mesh network is null.")
        }
    }

    // MeshManagerCallbacks implementations

    override fun onNetworkLoaded(meshNetwork: MeshNetwork?) {
        runOnUiThread {
            if (meshNetwork != null) {
                appendMessage("Mesh network loaded.")
                if (!::appKey.isInitialized) {
                    configureAppKey()
                }
                sendButton.isEnabled = true
            } else {
                appendMessage("Error: Mesh network not loaded")
            }
        }
    }

    override fun onNetworkUpdated(meshNetwork: MeshNetwork?) {
        // Handle network updates if needed
    }

    override fun onNetworkLoadFailed(error: String?) {
        runOnUiThread {
            appendMessage("Mesh network load failed: $error")
        }
    }

    override fun onNetworkImported(meshNetwork: MeshNetwork?) {
        runOnUiThread {
            if (meshNetwork != null) {
                appendMessage("Mesh network imported callback received.")
                configureAppKey()
                sendButton.isEnabled = true
            } else {
                appendMessage("Error: Mesh network is null on import.")
            }
        }
    }

    override fun onNetworkImportFailed(error: String?) {
        runOnUiThread {
            appendMessage("Mesh network import failed: $error")
        }
    }

    override fun sendProvisioningPdu(meshNode: UnprovisionedMeshNode?, pdu: ByteArray?) {
    }

    override fun onMeshPduCreated(pdu: ByteArray?) {
    }

    override fun getMtu(): Int {
        return 23
    }
}
