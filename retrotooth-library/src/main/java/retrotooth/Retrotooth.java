package retrotooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Adapts a Java interface to a Bluetooth API.
 * <p/>
 * Inspired by square's Retrofit
 * <p/>
 *
 * @author James Ayvaz (james.ayvaz@gmail.com)
 */
public final class Retrotooth {
    private final Map<Method, MethodHandler<?>> methodHandlerCache = new LinkedHashMap<>();

    private final Context context;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt client;
    private final BluetoothDevice bluetoothDevice;
    private final Converter.Factory converterFactory;
    private final CallAdapter.Factory adapterFactory;
    private final Executor callbackExecutor;
    private final RetrotoothGattCallback retrotoothGattCallback;

    private Retrotooth(Context context, BluetoothManager bluetoothManager, BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice, Converter.Factory converterFactory,
                       CallAdapter.Factory adapterFactory, Executor callbackExecutor) {
        this.context = context;
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothDevice = bluetoothDevice;
        this.converterFactory = converterFactory;
        this.adapterFactory = adapterFactory;
        this.callbackExecutor = callbackExecutor;
        this.retrotoothGattCallback = new RetrotoothGattCallback();
        this.client = connect();
    }

    //region Inspect interface using reflection

    /**
     * Create an implementation of the API defined by the {@code service} interface.
     */
    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(Class<T> service) {
        Utils.validateServiceClass(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                handler);
    }

    private final InvocationHandler handler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            return loadMethodHandler(method).invoke(args);
        }
    };

    MethodHandler<?> loadMethodHandler(Method method) {
        MethodHandler<?> handler;
        synchronized (methodHandlerCache) {
            handler = methodHandlerCache.get(method);
            if (handler == null) {
                handler = MethodHandler.create(method, client, adapterFactory, converterFactory, retrotoothGattCallback);
                methodHandlerCache.put(method, handler);
            }
        }
        return handler;
    }
    //endregion

    private BluetoothGatt connect() {
        return bluetoothDevice.connectGatt(this.context, false, this.retrotoothGattCallback);
    }


    public void disconnect() {
        if (bluetoothAdapter == null || client == null) {
            throw new RuntimeException("BluetoothAdapter not initialized");
        }
        client.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        disconnect();
        if (client == null) {
            return;
        }
        client.close();
        client = null;
    }

    public BluetoothGatt client() {
        return client;
    }

    public BluetoothDevice bluetoothDevice() {
        return bluetoothDevice;
    }


    /**
     * TODO
     * <p/>
     * May be null.
     */
    public Converter.Factory converterFactory() {
        return converterFactory;
    }

    public CallAdapter.Factory callAdapterFactory() {
        return adapterFactory;
    }

    public Executor callbackExecutor() {
        return callbackExecutor;
    }

    /**
     * Build a new {@link Retrotooth}.
     * <p/>
     * Calling {@link #bluetoothDevice} is required before calling {@link #build()}. All other methods
     * are optional.
     */
    public static final class Builder {
        private BluetoothGatt client;
        private Context context;
        private BluetoothManager bluetoothManager;
        private BluetoothAdapter bluetoothAdapter;
        private String bluetoothDeviceAddress;
        private BluetoothDevice bluetoothDevice;
        private Converter.Factory converterFactory;
        private CallAdapter.Factory adapterFactory;
        private Executor callbackExecutor;

        public Builder device(String address) {
            if (bluetoothAdapter == null) {
                throw new NullPointerException("bluetoothAdapter == null");
            }
            if (address == null) {
                throw new NullPointerException("address == null");
            }

            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                throw new RuntimeException("Device not found.  Unable to connect.");
            }
            return this.device(device);
        }

        public Builder device(BluetoothDevice device) {
            if (bluetoothAdapter == null) {
                throw new NullPointerException("bluetoothAdapter == null");
            }
            this.bluetoothDevice = Utils.checkNotNull(device, "device == null");
            return this;
        }

        public Builder with(Context context) {
            this.context = Utils.checkNotNull(context, "context == null");

            if (bluetoothManager == null) {
                bluetoothManager = Utils.checkNotNull((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE), "bluetoothManager == null");
            }

            bluetoothAdapter = Utils.checkNotNull(bluetoothManager.getAdapter(), "bluetoothAdapter == null");

            return this;
        }

        /**
         * The converter used for serialization and deserialization of objects.
         */
        public Builder converterFactory(Converter.Factory converterFactory) {
            this.converterFactory = Utils.checkNotNull(converterFactory, "converterFactory == null");
            return this;
        }

        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.adapterFactory = Utils.checkNotNull(factory, "factory == null");
            return this;
        }


        /**
         * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
         * your service method.
         */
        public Builder callbackExecutor(Executor callbackExecutor) {
            this.callbackExecutor = Utils.checkNotNull(callbackExecutor, "callbackExecutor == null");
            return this;
        }

        /**
         * Create the {@link Retrotooth} instances.
         */
        public Retrotooth build() {
            if (bluetoothDevice == null) {
                throw new IllegalStateException("Bluetooth device required.");
            }

            if (context == null) {
                throw new IllegalStateException("context required.");
            }

            if (adapterFactory == null) {
                adapterFactory = Platform.get().defaultCallAdapterFactory(callbackExecutor);
            }

            if (converterFactory == null) {
                converterFactory = new DefaultConverterFactory();
            }

            return new Retrotooth(context, bluetoothManager, bluetoothAdapter, bluetoothDevice, converterFactory, adapterFactory, callbackExecutor);
        }
    }
}