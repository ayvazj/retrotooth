package retrotooth;

import android.bluetooth.BluetoothGatt;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static retrotooth.Utils.methodError;

final class MethodHandler<T> {
    @SuppressWarnings("unchecked")
    static MethodHandler<?> create(Method method, BluetoothGatt client,
                                   CallAdapter.Factory callAdapterFactory, Converter.Factory converterFactory, RetrotoothGattCallback retrotoothGattCallback) {
        CallAdapter<Object> callAdapter =
                (CallAdapter<Object>) createCallAdapter(method, callAdapterFactory);
        Converter<Object> responseConverter =
                (Converter<Object>) createResponseConverter(method, callAdapter.responseType(),
                        converterFactory);
        RequestFactory requestFactory = RequestFactoryParser.parse(method, converterFactory);
        return new MethodHandler<>(client, requestFactory, callAdapter, responseConverter, retrotoothGattCallback);
    }

    private static CallAdapter<?> createCallAdapter(Method method,
                                                    CallAdapter.Factory adapterFactory) {
        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw methodError(method,
                    "Method return type must not include a type variable or wildcard: %s", returnType);
        }

        if (returnType == void.class) {
            throw methodError(method, "Service methods cannot return void.");
        }

        CallAdapter<?> adapter = adapterFactory.get(returnType);
        if (adapter == null) {
            throw methodError(method, "Call adapter factory '%s' was unable to handle return type %s",
                    adapterFactory, returnType);
        }
        return adapter;
    }

    private static Converter<?> createResponseConverter(Method method, Type responseType,
                                                        Converter.Factory converterFactory) {
        if (responseType == Response.class) {
            boolean isStreaming = false; // TODO method.isAnnotationPresent(Streaming.class);
            return new BleResponseConverter(isStreaming);
        }

        if (converterFactory == null) {
            throw methodError(method, "Method response type is "
                    + responseType
                    + " but no converter factory registered. "
                    + "Either add a converter factory to the Retrofit instance or use ResponseBody.");
        }

        Converter<?> converter = converterFactory.get(responseType);
        if (converter == null) {
            throw methodError(method, "Converter factory '%s' was unable to handle response type %s",
                    converterFactory, responseType);
        }
        return converter;
    }

    private final BluetoothGatt client;
    private final RequestFactory requestFactory;
    private final CallAdapter<T> callAdapter;
    private final Converter<T> responseConverter;
    private final RetrotoothGattCallback retrotoothGattCallback;

    private MethodHandler(BluetoothGatt client, RequestFactory requestFactory,
                          CallAdapter<T> callAdapter, Converter<T> responseConverter, RetrotoothGattCallback retrotoothGattCallback) {
        this.client = client;
        this.requestFactory = requestFactory;
        this.callAdapter = callAdapter;
        this.responseConverter = responseConverter;
        this.retrotoothGattCallback = retrotoothGattCallback;
    }

    Object invoke(Object... args) {
        return callAdapter.adapt(new BluetoothCall<>(client, requestFactory, responseConverter, retrotoothGattCallback, args));
    }
}
