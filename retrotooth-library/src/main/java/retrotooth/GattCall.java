package retrotooth;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class GattCall {
    private final BluetoothGatt client;
    private final RetrotoothGattCallback retrotoothGattCallback;

    // Guarded by this.
    private boolean executed;
    volatile boolean canceled;

    /**
     * The application's original request unadulterated by redirects or auth headers.
     */
    Request originalRequest;

    protected GattCall(BluetoothGatt client, Request originalRequest, RetrotoothGattCallback retrotoothGattCallback) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.retrotoothGattCallback = retrotoothGattCallback;
    }

    Object tag() {
        return originalRequest.tag();
    }

    public byte[] execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        return getResponse(originalRequest);
    }

    /**
     * Schedules the request to be executed at some point in the future.
     * <p/>
     *
     * @throws IllegalStateException when the call has already been executed.
     */
    public void enqueue(GattCallback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }

        boolean signalledCallback = false;
        try {
            byte[] resp = getResponse(originalRequest);
            if (canceled) {
                signalledCallback = true;
                responseCallback.onFailure(originalRequest, new IOException("Canceled"));
            } else {
                signalledCallback = true;
                responseCallback.onResponse(resp);
            }
        } catch (IOException e) {
            if (signalledCallback) {
                // Do not signal the callback twice!
                Internal.logger.log(Level.INFO, "Callback failure for " + toLoggableString(), e);
            } else {
                responseCallback.onFailure(originalRequest, e);
            }
        } finally {
            // TODO
        }
    }


    /**
     * Cancels the request, if possible. Requests that are already complete
     * cannot be canceled.
     */
    public void cancel() {
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns a string that describes this call. Doesn't include a full URL as that might contain
     * sensitive information.
     */
    private String toLoggableString() {
        String string = canceled ? "canceled call" : "call";
        return string + " to " + originalRequest.bluetoothOperation() + " " + originalRequest.characteristic();
    }


    /**
     * Performs the request and returns the response. May return null if this
     * call was canceled.
     */
    byte[] getResponse(final Request request) {
        switch (request.bluetoothOperation()) {
            case READ:
                return doRead(request);
            case WRITE:
                return doWrite(request);
            default:
                break;
        }
        return null;
    }

    private byte[] doRead(final Request request) {
        final RetrotoothFuture future = new RetrotoothFuture();
        retrotoothGattCallback.setPassthruListener(new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (BluetoothGatt.GATT_SUCCESS == status) {
                    BluetoothGattService service = gatt.getService(request.service());
                    if (service == null) {
                        throw new IllegalStateException("service(" + request.service() + ") not found");
                    }
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(request.characteristic());
                    if (characteristic == null) {
                        throw new IllegalStateException("characteristic(" + request.characteristic() + ") not found on service(" + request.service() + ")");
                    }
                    gatt.setCharacteristicNotification(characteristic, true);
                    gatt.readCharacteristic(characteristic);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (BluetoothGatt.GATT_SUCCESS == status && characteristic.getUuid().equals(request.characteristic())) {
                    future.onCharacteristicRead(characteristic);
                }
            }
        });
        BluetoothGattService service = client.getService(request.service());
        if (service == null) {
            if (!client.discoverServices()) {
                throw new IllegalStateException("discover services failed");
            }
        } else {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(request.characteristic());
            if (characteristic == null) {
                throw new IllegalStateException("characteristic(" + request.characteristic() + ") not found on service(" + request.service() + ")");
            }
            client.setCharacteristicNotification(characteristic, true);
            client.readCharacteristic(characteristic);
        }

        try {
            BluetoothGattCharacteristic characteristic = future.get();
            if (characteristic == null) {
                throw new IllegalStateException("characteristic(" + request.characteristic() + ") is null.  Try resetting your bluetooth adapter");
            }
            if (characteristic.getValue() == null || characteristic.getValue().length == 0) {
                // empty response, this can happen if the charactersitic has no value
                return new byte[]{};
            }
            Internal.logger.log(Level.INFO, "characteristic.getValue() " + characteristic.getValue()[0]);
            return characteristic.getValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] doWrite(final Request request) {
        final RetrotoothFuture future = new RetrotoothFuture();
        retrotoothGattCallback.setPassthruListener(new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (BluetoothGatt.GATT_SUCCESS == status) {
                    BluetoothGattService service = gatt.getService(request.service());
                    if (service == null) {
                        throw new IllegalStateException("service(" + request.service() + ") not found");
                    }
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(request.characteristic());
                    if (characteristic == null) {
                        throw new IllegalStateException("characteristic(" + request.characteristic() + ") not found on service(" + request.service() + ")");
                    }
                    gatt.setCharacteristicNotification(characteristic, true);
                    characteristic.setValue(new byte[] { 0x03 });
                    gatt.writeCharacteristic(characteristic);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (BluetoothGatt.GATT_SUCCESS == status && characteristic.getUuid().equals(request.characteristic())) {
                    future.onCharacteristicWrite(characteristic);
                }
            }
        });
        BluetoothGattService service = client.getService(request.service());
        if (service == null) {
            if (!client.discoverServices()) {
                throw new IllegalStateException("discover services failed");
            }
        } else {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(request.characteristic());
            if (characteristic == null) {
                throw new IllegalStateException("characteristic(" + request.characteristic() + ") not found on service(" + request.service() + ")");
            }
            client.setCharacteristicNotification(characteristic, true);
            client.writeCharacteristic(characteristic);
        }

        try {
            BluetoothGattCharacteristic characteristic = future.get();
            if (characteristic == null) {
                throw new IllegalStateException("characteristic(" + request.characteristic() + ") is null.  Try resetting your bluetooth adapter");
            }
            if (characteristic.getValue() == null || characteristic.getValue().length == 0) {
                // empty response, this can happen if the charactersitic has no value
                return new byte[]{};
            }
            Internal.logger.log(Level.INFO, "characteristic.getValue() " + characteristic.getValue()[0]);
            return characteristic.getValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    class RetrotoothFuture implements Future<BluetoothGattCharacteristic> {

        private volatile BluetoothGattCharacteristic characteristic = null;
        private volatile boolean cancelled = false;
        private final CountDownLatch countDownLatch;
        private final int TIMEOUT = 4;

        public RetrotoothFuture() {
            countDownLatch = new CountDownLatch(1);
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            if (isDone()) {
                return false;
            } else {
                countDownLatch.countDown();
                cancelled = true;
                return !isDone();
            }
        }

        @Override
        public BluetoothGattCharacteristic get() throws InterruptedException, ExecutionException {
            countDownLatch.await(TIMEOUT, TimeUnit.SECONDS);
            return characteristic;
        }

        @Override
        public BluetoothGattCharacteristic get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            countDownLatch.await(timeout, unit);
            return characteristic;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return countDownLatch.getCount() == 0;
        }

        public void onCharacteristicRead(final BluetoothGattCharacteristic characteristic) {
            this.characteristic = characteristic;
            countDownLatch.countDown();
        }

        public void onCharacteristicWrite(final BluetoothGattCharacteristic characteristic) {
            this.characteristic = characteristic;
            countDownLatch.countDown();
        }
    }
}

