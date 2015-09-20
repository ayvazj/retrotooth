package retrotooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static retrotooth.Utils.checkNotNull;

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
    private final List<Converter.Factory> converterFactories;
    private final List<CallAdapter.Factory> adapterFactories;
    private final Executor callbackExecutor;
    private final RetrotoothGattCallback retrotoothGattCallback;

    private Retrotooth(Context context, BluetoothManager bluetoothManager, BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice, List<Converter.Factory> converterFactories,
                       List<CallAdapter.Factory> adapterFactories, Executor callbackExecutor) {
        this.context = context;
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothDevice = bluetoothDevice;
        this.converterFactories = converterFactories;
        this.adapterFactories = adapterFactories;
        this.callbackExecutor = callbackExecutor;
        this.retrotoothGattCallback = new RetrotoothGattCallback();
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
                handler = MethodHandler.create(method, client, adapterFactories, converterFactories, retrotoothGattCallback);
                methodHandlerCache.put(method, handler);
            }
        }
        return handler;
    }
    //endregion

    public BluetoothGatt connect() {
        this.client = bluetoothDevice.connectGatt(this.context, false, this.retrotoothGattCallback);
        return this.client;
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
    public List<Converter.Factory> converterFactories() {
        return Collections.unmodifiableList(converterFactories);
    }

    public List<CallAdapter.Factory> callAdapterFactories() {
        return Collections.unmodifiableList(adapterFactories);
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
        private List<Converter.Factory> converterFactories = new ArrayList<>();
        private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();
        private Executor callbackExecutor;

        public Builder() {
            // Add the built-in converter factory first. This prevents overriding its behavior but also
            // ensures correct behavior when using converters that consume all types.
            converterFactories.add(new BuiltInConverterFactory());
        }

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
            this.bluetoothDevice = checkNotNull(device, "device == null");
            return this;
        }

        public Builder with(Context context) {
            this.context = checkNotNull(context, "context == null");

            if (bluetoothManager == null) {
                bluetoothManager = checkNotNull((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE), "bluetoothManager == null");
            }

            bluetoothAdapter = checkNotNull(bluetoothManager.getAdapter(), "bluetoothAdapter == null");

            return this;
        }

        /** Add converter factory for serialization and deserialization of objects. */
        public Builder addConverterFactory(Converter.Factory converterFactory) {
            converterFactories.add(checkNotNull(converterFactory, "converterFactory == null"));
            return this;
        }

        /**
         * TODO
         */
        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            adapterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }


        /**
         * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
         * your service method.
         */
        public Builder callbackExecutor(Executor callbackExecutor) {
            this.callbackExecutor = checkNotNull(callbackExecutor, "callbackExecutor == null");
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

            // Make a defensive copy of the adapters and add the default Call adapter.
            List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
            adapterFactories.add(Platform.get().defaultCallAdapterFactory(callbackExecutor));

            // Make a defensive copy of the converters.
            List<Converter.Factory> converterFactories = new ArrayList<>(this.converterFactories);

            return new Retrotooth(context, bluetoothManager, bluetoothAdapter, bluetoothDevice, converterFactories, adapterFactories, callbackExecutor);
        }
    }
}