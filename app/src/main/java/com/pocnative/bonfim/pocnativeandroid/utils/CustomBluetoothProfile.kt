package com.pocnative.bonfim.pocnativeandroid.utils

import java.util.UUID;

class CustomBluetoothProfile {
    class Basic {
        val service: UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val batteryCharacteristic: UUID = UUID.fromString("00000006-0000-3512-2118-0009af100700")
    }

    class AlertNotification {
        val service: UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
        val alertCharacteristic: UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
    }

    class HeartRate {
        var service: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        var measurementCharacteristic: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        var descriptor: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        var controlCharacteristic: UUID = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")
    }

    class Pedometer {
        var characteristicSteps: UUID = UUID.fromString("00000007-0000-3512-2118-0009af100700")
    }
}