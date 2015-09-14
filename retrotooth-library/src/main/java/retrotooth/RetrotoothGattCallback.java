package retrotooth;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

class RetrotoothGattCallback extends BluetoothGattCallback {
    private BluetoothGattCallback passthru;

    public RetrotoothGattCallback() {
        super();
    }

    public void setPassthruListener(BluetoothGattCallback callback) {
        this.passthru = callback;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
            gatt.discoverServices();
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
            gatt.disconnect();
        }

        if (this.passthru != null) {
            this.passthru.onConnectionStateChange(gatt, status, newState);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (this.passthru != null) {
            this.passthru.onServicesDiscovered(gatt, status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (this.passthru != null) {
            this.passthru.onCharacteristicRead(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (this.passthru != null) {
            this.passthru.onCharacteristicWrite(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (this.passthru != null) {
            this.passthru.onCharacteristicChanged(gatt, characteristic);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (this.passthru != null) {
            this.passthru.onDescriptorRead(gatt, descriptor, status);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (this.passthru != null) {
            this.passthru.onDescriptorWrite(gatt, descriptor, status);
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        if (this.passthru != null) {
            this.passthru.onReliableWriteCompleted(gatt, status);
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (this.passthru != null) {
            this.passthru.onReadRemoteRssi(gatt, rssi, status);
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        if (this.passthru != null) {
            this.passthru.onMtuChanged(gatt, mtu, status);
        }
    }
}
