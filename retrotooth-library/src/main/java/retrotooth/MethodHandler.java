package retrotooth;

import android.bluetooth.BluetoothGatt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static retrotooth.Utils.methodError;

final class MethodHandler<T> {
    @SuppressWarnings("unchecked")
    static MethodHandler<?> create(Method method, BluetoothGatt client,
                                   List<CallAdapter.Factory> callAdapterFactories, List<Converter.Factory> converterFactories, RetrotoothGattCallback retrotoothGattCallback) {
        CallAdapter<Object> callAdapter =
                (CallAdapter<Object>) createCallAdapter(method, callAdapterFactories);
        Converter<ResponseData, Object> responseConverter =
                (Converter<ResponseData, Object>) createResponseConverter(method, callAdapter.responseType(),
                        converterFactories);
        RequestFactory requestFactory = RequestFactoryParser.parse(method, converterFactories);
        return new MethodHandler<>(client, requestFactory, callAdapter, responseConverter, retrotoothGattCallback);
    }

    private static CallAdapter<?> createCallAdapter(Method method,
                                                    List<CallAdapter.Factory> adapterFactories) {
        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw methodError(method,
                    "Method return type must not include a type variable or wildcard: %s", returnType);
        }

        if (returnType == void.class) {
            throw methodError(method, "Service methods cannot return void.");
        }

        Annotation[] annotations = method.getAnnotations();
        try {
            return Utils.resolveCallAdapter(adapterFactories, returnType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw Utils.methodError(e, method, "Unable to create call adapter for %s", returnType);
        }
    }

    private static Converter<ResponseData, ?> createResponseConverter(Method method,
                                                                      Type responseType, List<Converter.Factory> converterFactories) {
        Annotation[] annotations = method.getAnnotations();
        try {
            return Utils.resolveResponseBodyConverter(converterFactories, responseType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw Utils.methodError(e, method, "Unable to create converter for %s", responseType);
        }
    }

    private final BluetoothGatt client;
    private final RequestFactory requestFactory;
    private final CallAdapter<T> callAdapter;
    private final Converter<ResponseData, T> responseConverter;
    private final RetrotoothGattCallback retrotoothGattCallback;

    private MethodHandler(BluetoothGatt client, RequestFactory requestFactory,
                          CallAdapter<T> callAdapter, Converter<ResponseData, T> responseConverter, RetrotoothGattCallback retrotoothGattCallback) {
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
