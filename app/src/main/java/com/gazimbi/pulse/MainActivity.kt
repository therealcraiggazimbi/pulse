package com.gazimbi.pulse

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gazimbi.pulse.databinding.ActivityMainBinding
import com.gazimbi.pulse.databinding.ItemParameterBinding
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.Executors

data class Parameter(val name: String)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var telephonyManager: TelephonyManager

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val parameters = listOf(
            Parameter("RSRP"),
            Parameter("RSRQ"),
            Parameter("SINR"),
            Parameter("EC/IO"),
            Parameter("Cell ID (CID)"),
            Parameter("Location Area Code (LAC)"),
            Parameter("Timing Advance (TA)"),
            Parameter("Signal Strength"),
            Parameter("Data Rate"),
            Parameter("Latency"),
            Parameter("Packet Loss")
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ParameterAdapter(parameters) { parameter ->
            handleParameterClick(parameter)
        }
        binding.recyclerView.adapter = adapter

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Request necessary permissions
        requestTelephonyPermissions()
    }

    private fun requestTelephonyPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun handleParameterClick(parameter: Parameter) {
        if (!checkTelephonyPermissions()) {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            return
        }

        when (parameter.name) {
            "RSRP" -> measureRSRP()
            "RSRQ" -> measureRSRQ()
            "SINR" -> measureSINR()
            "EC/IO" -> measureECIO()
            "Cell ID (CID)" -> measureCellID()
            "Location Area Code (LAC)" -> measureLAC()
            "Timing Advance (TA)" -> measureTimingAdvance()
            "Signal Strength" -> measureSignalStrength()
            "Data Rate" -> measureDataRate()
            "Latency" -> measureLatency()
            "Packet Loss" -> measurePacketLoss()
            else -> Toast.makeText(this, "Unknown parameter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkTelephonyPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun measureRSRP() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                when (cellInfo) {
                    is CellInfoLte -> {
                        // Use getLte() method for older Android versions
                        val rsrp = cellInfo.cellSignalStrength.rsrp
                        Toast.makeText(this, "RSRP: $rsrp dBm", Toast.LENGTH_SHORT).show()
                        return
                    }
                    is CellInfoNr -> {
                        // For newer 5G networks, use dbm as a fallback
                        val signalStrength = cellInfo.cellSignalStrength.dbm
                        Toast.makeText(this, "RSRP (5G): $signalStrength dBm", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
            Toast.makeText(this, "RSRP: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }


    private fun measureECIO() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                if (cellInfo is CellInfoWcdma) {
                    val ecio = cellInfo.cellSignalStrength.ecNo
                    Toast.makeText(this, "EC/IO: $ecio dB", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Toast.makeText(this, "EC/IO: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }



    private fun measureLAC() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                val lac = when (cellInfo) {
                    is CellInfoLte -> cellInfo.cellIdentity.tac.toString()
                    is CellInfoWcdma -> cellInfo.cellIdentity.lac.toString()
                    is CellInfoGsm -> cellInfo.cellIdentity.lac.toString()
                    else -> null
                }

                if (lac != null) {
                    Toast.makeText(this, "Location Area Code: $lac", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Toast.makeText(this, "LAC: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }
    private fun measureRSRQ() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                when (cellInfo) {
                    is CellInfoLte -> {
                        val rsrq = cellInfo.cellSignalStrength.rsrq
                        Toast.makeText(this, "RSRQ: $rsrq dB", Toast.LENGTH_SHORT).show()
                        return
                    }
                    else -> {
                        // Fallback for other network types
                        val signalStrength = when (cellInfo) {
                            is CellInfoNr -> cellInfo.cellSignalStrength.dbm
                            is CellInfoWcdma -> cellInfo.cellSignalStrength.dbm
                            is CellInfoGsm -> cellInfo.cellSignalStrength.dbm
                            else -> null
                        }
                        if (signalStrength != null) {
                            Toast.makeText(this, "Signal Quality: $signalStrength dB", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
            Toast.makeText(this, "RSRQ: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun measureSINR() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                when (cellInfo) {
                    is CellInfoLte -> {
                        val sinr = cellInfo.cellSignalStrength.rssnr
                        Toast.makeText(this, "SINR: $sinr dB", Toast.LENGTH_SHORT).show()
                        return
                    }
                    else -> {
                        // Fallback for other network types
                        val signalStrength = when (cellInfo) {
                            is CellInfoNr -> cellInfo.cellSignalStrength.dbm
                            is CellInfoWcdma -> cellInfo.cellSignalStrength.dbm
                            is CellInfoGsm -> cellInfo.cellSignalStrength.dbm
                            else -> null
                        }
                        if (signalStrength != null) {
                            Toast.makeText(this, "Signal Quality: $signalStrength dB", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            }
            Toast.makeText(this, "SINR: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun measureCellID() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                val cellId = when (cellInfo) {
                    is CellInfoLte -> cellInfo.cellIdentity.ci.toString()
                    is CellInfoWcdma -> cellInfo.cellIdentity.cid.toString()
                    is CellInfoGsm -> cellInfo.cellIdentity.cid.toString()
                    else -> null
                }

                if (cellId != null) {
                    Toast.makeText(this, "Cell ID: $cellId", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Toast.makeText(this, "Cell ID: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun measureTimingAdvance() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                val ta = when (cellInfo) {
                    is CellInfoLte -> cellInfo.cellSignalStrength.timingAdvance
                    is CellInfoGsm -> cellInfo.cellSignalStrength.timingAdvance
                    else -> null
                }

                if (ta != null) {
                    Toast.makeText(this, "Timing Advance: $ta", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Toast.makeText(this, "Timing Advance: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }



    private fun measureSignalStrength() {
        if (checkTelephonyPermissions()) {
            val cellInfoList = telephonyManager.allCellInfo
            cellInfoList.forEach { cellInfo ->
                val signalStrength = when (cellInfo) {
                    is CellInfoLte -> cellInfo.cellSignalStrength.dbm
                    is CellInfoNr -> cellInfo.cellSignalStrength.dbm
                    is CellInfoWcdma -> cellInfo.cellSignalStrength.dbm
                    is CellInfoGsm -> cellInfo.cellSignalStrength.dbm
                    else -> null
                }

                if (signalStrength != null) {
                    Toast.makeText(this, "Signal Strength: $signalStrength dBm", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Toast.makeText(this, "Signal Strength: Unable to retrieve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun measureDataRate() {
        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket("speedtest.wdc01.softlayer.com", 80)
                socket.close()
                val endTime = System.currentTimeMillis()

                val downloadSpeed = calculateDownloadSpeed()
                val uploadSpeed = calculateUploadSpeed()

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Download: $downloadSpeed Mbps\nUpload: $uploadSpeed Mbps",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Data Rate: Unable to measure", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun calculateDownloadSpeed(): Double {
        // Implement actual download speed test logic
        return 0.0
    }

    private fun calculateUploadSpeed(): Double {
        // Implement actual upload speed test logic
        return 0.0
    }

    private fun measureLatency() {
        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            try {
                val address = InetAddress.getByName("8.8.8.8")
                val startTime = System.currentTimeMillis()
                val reachable = address.isReachable(1000)
                val endTime = System.currentTimeMillis()

                val latency = endTime - startTime

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Latency: $latency ms",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Latency: Unable to measure", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun measurePacketLoss() {
        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            try {
                val address = InetAddress.getByName("8.8.8.8")
                var packetsSent = 0
                var packetsReceived = 0

                repeat(10) {
                    packetsSent++
                    if (address.isReachable(500)) {
                        packetsReceived++
                    }
                }

                val packetLossPercentage = ((packetsSent - packetsReceived) / packetsSent.toDouble()) * 100

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Packet Loss: ${packetLossPercentage.format(2)}%",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Packet Loss: Unable to measure", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Extension function to format double with specified decimal places
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    class ParameterAdapter(
        private val parameters: List<Parameter>,
        private val onItemClick: (Parameter) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ParameterAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemParameterBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemParameterBinding.inflate(
                android.view.LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int = parameters.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val parameter = parameters[position]
            holder.binding.name.text = parameter.name
            holder.binding.root.setOnClickListener { onItemClick(parameter) }
        }
    }
}