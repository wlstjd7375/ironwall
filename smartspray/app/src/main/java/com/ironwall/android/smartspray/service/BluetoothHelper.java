package com.ironwall.android.smartspray.service;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by KimJS on 2016-08-11.
 */
public class BluetoothHelper {
    public static String shortUuidFormat = "0000%04X-0000-1000-8000-00805F9B34FB";

    public static UUID sixteenBitUuid(long shortUuid) {
        assert shortUuid >= 0 && shortUuid <= 0xFFFF;
        return UUID.fromString(String.format(shortUuidFormat, shortUuid & 0xFFFF));
    }

    public static String getDeviceInfoText(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return "Name: " + device.getName() + "\nMAC: " + device.getAddress() + "\nRSSI: " + rssi + "\nScan Record:" + parseScanRecord(scanRecord);
    }

    // Bluetooth Spec V4.0 - Vol 3, Part C, section 8
    private static String parseScanRecord(byte[] scanRecord) {
        StringBuilder output = new StringBuilder();
        int i = 0;
        while (i < scanRecord.length) {
            int len = scanRecord[i++] & 0xFF;
            if (len == 0) break;
            switch (scanRecord[i] & 0xFF) {
                // https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
                case 0x0A: // Tx Power
                    output.append("\n  Tx Power: ").append(scanRecord[i+1]);
                    break;
                case 0xFF: // Manufacturer Specific data (RFduinoBLE.advertisementData)
                    output.append("\n  Advertisement Data: ")
                            .append(HexAsciiHelper.bytesToHex(scanRecord, i + 3, len));

                    String ascii = HexAsciiHelper.bytesToAsciiMaybe(scanRecord, i + 3, len);
                    if (ascii != null) {
                        output.append(" (\"").append(ascii).append("\")");
                    }
                    break;
            }
            i += len;
        }
        return output.toString();
    }
}

