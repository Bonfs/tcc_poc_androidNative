package com.pocnative.bonfim.pocnativeandroid

import android.bluetooth.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_ble.*
import kotlinx.android.synthetic.main.content_ble.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.pocnative.bonfim.pocnativeandroid.utils.CustomBluetoothProfile
import java.util.*
import android.widget.Toast


class BLEActivity : AppCompatActivity() {
    lateinit var bluetoothDevice: BluetoothDevice
    lateinit var bluetoothGatt: BluetoothGatt
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var isListeningHeartRate = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)
        setSupportActionBar(toolbar)

        btnConnect.setOnClickListener {
            this.connect()
        }

        btnHeart.setOnClickListener {
            this.startScanHeartRate()
        }

        btnSteps.setOnClickListener {
            this.getSteps()
        }

        btnBattery.setOnClickListener {
            this.getBatteryStatus()
        }
    }

    fun connect() {
        val address = etAddress.text.toString()
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)

        Log.v("test", "Connecting to ${address}")
        Log.v("test", "Device name ${bluetoothDevice.name}")

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback)
    }

    fun hasConnected() {
        bluetoothGatt.discoverServices()
        tvState.text = "Connected"
    }

    fun disconnect() {
        bluetoothGatt.disconnect()
        tvState.text = "Disconnected"
    }

    fun startScanHeartRate() {
        tvByte.text = "..."
        val bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate().service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate().controlCharacteristic)
        bchar.value = byteArrayOf(21, 2, 1)
        bluetoothGatt.writeCharacteristic(bchar)
    }

    fun listenHeartRate() {
        val bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate().service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate().measurementCharacteristic)
        bluetoothGatt.setCharacteristicNotification(bchar, true)
        val descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate().descriptor)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt.writeDescriptor(descriptor)
        isListeningHeartRate = true
    }

    fun getSteps() {
        tvByte.text = "..."
        val bchar = bluetoothGatt.getService(CustomBluetoothProfile.Basic().service)
                .getCharacteristic(CustomBluetoothProfile.Pedometer().characteristicSteps)
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get pedometer info", Toast.LENGTH_SHORT).show()
        }
    }

    fun getBatteryStatus() {
        tvByte.text = "..."
        val bchar = bluetoothGatt.getService(CustomBluetoothProfile.Basic().service)
                .getCharacteristic(CustomBluetoothProfile.Basic().batteryCharacteristic)
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show()
        }

    }

    private val bluetoothGattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.v("Test", "onConnectionStateChange")

            if(newState == BluetoothProfile.STATE_CONNECTED) {
                hasConnected()
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.v("test", "onServicesDiscovered")
            listenHeartRate()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.v("test", "onCharacteristicRead")
            val data = characteristic.value
            tvByte.text = Arrays.toString(data)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.v("test", "onCharacteristicWrite")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.v("test", "onCharacteristicChanged")
            val data = characteristic.value
            tvByte.text = Arrays.toString(data)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.v("test", "onDescriptorRead")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.v("test", "onDescriptorWrite")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
            Log.v("test", "onReliableWriteCompleted")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            Log.v("test", "onReadRemoteRssi")
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.v("test", "onMtuChanged")
        }
    }
}
