package com.cl.common_base.util.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */

public class BleUtil {
    public static boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    public static boolean isCharacteristicWritable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    public static boolean isCharacteristicNoRspWritable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }

    public static boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    public static boolean isCharacteristicIndicatable(BluetoothGattCharacteristic characteristic) {
        return characteristic != null && (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;
    }

    public static boolean clearGattCache(BluetoothGatt gatt) {
        boolean result = false;
        try {
            if (gatt != null) {
                Method refresh = BluetoothGatt.class.getMethod("refresh");
                if (refresh != null) {
                    refresh.setAccessible(true);
                    result = (boolean) refresh.invoke(gatt, new Object[0]);
                }
            }
        } catch (Exception e) {
        }

        return result;
    }

    public static boolean isBleEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public static int getBluetoothState() {
        return BluetoothAdapter.getDefaultAdapter().getState();
    }

    public static void restartBle() {
        if (BleUtil.isBleEnabled()) {
            BleUtil.closeBle();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BleUtil.openBle();
                }
            }, 1000);
        } else {
            BleUtil.openBle();
        }
    }

    public static void openBle() {
        int state = getBluetoothState();

        switch (state) {
            case BluetoothAdapter.STATE_ON:
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                break;

            case BluetoothAdapter.STATE_TURNING_OFF:
            case BluetoothAdapter.STATE_OFF:
                if (!isBleEnabled()) {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter != null) {
                        adapter.enable();
                    }
                }
                break;
            default:
                break;
        }
    }

    public static void closeBle() {
        int state = getBluetoothState();

        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
            case BluetoothAdapter.STATE_ON:
                if (isBleEnabled()) {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter != null) {
                        adapter.disable();
                    }
                }
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                break;
            case BluetoothAdapter.STATE_OFF:
                break;
            default:
                break;
        }
    }

    public static List<BluetoothDevice> getSystemBondedDevices() {
        List<BluetoothDevice> bondedDeviceList = new ArrayList<>();
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                bondedDeviceList.addAll(devices);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bondedDeviceList;
    }


    /**
     * 反射来调用BluetoothDevice.removeBond取消设备的配对
     *
     * @param device
     */
    public static void unPairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {

        }
    }


    /**
     * 创建配对
     *
     * @param mac
     */
    public static boolean createBound(String mac) throws Exception{
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);
       return device.createBond();
    }


    /**
     * 解除配对
     *
     * @param mac
     * @throws Exception
     */
    public static void removeBond(String mac) throws Exception {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        Set<BluetoothDevice> bluetoothDeviceList = bluetoothAdapter.getBondedDevices();
        BluetoothDevice bluetoothDevice = null;
        for (BluetoothDevice device : bluetoothDeviceList) {
            if (device.getAddress().equalsIgnoreCase(mac)) {
                bluetoothDevice = device;
                break;
            }
        }

        Method bondDevice = BluetoothDevice.class.getMethod("removeBond");
        bondDevice.invoke(bluetoothDevice);
    }


    /**
     * 是否有配对
     *
     * @param mac
     * @return
     */
    public static boolean hasBond(String mac) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        Set<BluetoothDevice> bluetoothDeviceList = bluetoothAdapter.getBondedDevices();
        boolean hasBond = false;
        for (BluetoothDevice device : bluetoothDeviceList) {
            if (device.getAddress().equalsIgnoreCase(mac)) {
                hasBond = true;
                break;
            }
        }
        return hasBond;
    }


    /**
     * 是否连接BT
     *
     * @param mac
     * @return
     */
    public static boolean isConnectBT(String mac) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        Set<BluetoothDevice> bluetoothDeviceList = bluetoothAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for (BluetoothDevice device : bluetoothDeviceList) {
            if (device.getAddress().equalsIgnoreCase(mac)) {
                targetDevice = device;
                break;
            }
        }

        if (targetDevice != null) {
            try {
                Method isConnectedMethod = BluetoothDevice.class.getMethod("isConnected");
                isConnectedMethod.setAccessible(true);
                return (boolean) isConnectedMethod.invoke(targetDevice);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
        return false;
    }

}