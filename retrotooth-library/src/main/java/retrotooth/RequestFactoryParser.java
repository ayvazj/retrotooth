package retrotooth;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.regex.Pattern;

import retrotooth.annotations.READ;
import retrotooth.util.BleUtils;

import static retrotooth.Utils.methodError;

final class RequestFactoryParser {
    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

    static RequestFactory parse(Method method, Converter.Factory converterFactory) {
        RequestFactoryParser parser = new RequestFactoryParser(method);
        parser.parseMethodAnnotations();
        parser.parseParameters(converterFactory);
        return parser.toRequestFactory();
    }

    private final Method method;

    private BluetoothOperation bluetoothOperation;
    private boolean hasBody;
    private UUID serviceUuid;
    private UUID characteristicUuid;

    private RequestBuilderAction[] requestBuilderActions;

    private RequestFactoryParser(Method method) {
        this.method = method;
    }

    private RequestFactory toRequestFactory() {
        return new RequestFactory(serviceUuid, characteristicUuid, bluetoothOperation, hasBody,
                requestBuilderActions);
    }

    private RuntimeException parameterError(int index, String message, Object... args) {
        return methodError(method, message + " (parameter #" + (index + 1) + ")", args);
    }

    private void parseMethodAnnotations() {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof READ) {
                parseBluetoothOperation(BluetoothOperation.READ, ((READ) annotation), false);
            }
        }
        if (bluetoothOperation == null) {
            throw methodError(method, "BLE method annotation is required (e.g., @READ, @WRITE, etc.).");
        }
    }

    private void parseBluetoothOperation(BluetoothOperation bluetoothOperation, READ read, boolean hasBody) {
        if (this.bluetoothOperation != null) {
            throw methodError(method, "Only one BLE operation method is allowed. Found: %s and %s.",
                    this.bluetoothOperation, bluetoothOperation);
        }
        if (read == null) {
            throw methodError(method, "read == null",
                    this.bluetoothOperation, bluetoothOperation);
        }

        this.bluetoothOperation = bluetoothOperation;
        this.hasBody = hasBody;

        if (read.service() == null || read.service().isEmpty()) {
            throw methodError(method, "\"%s\" must have a service value defined.", this.bluetoothOperation);
        }

        if (read.characteristic() == null || read.characteristic().isEmpty()) {
            throw methodError(method, "\"%s\" must have a characteristic value defined.", this.bluetoothOperation);
        }
        
        this.serviceUuid = BleUtils.getUUID(read.service());
        this.characteristicUuid = BleUtils.getUUID(read.characteristic());
    }

    private void parseParameters(Converter.Factory converterFactory) {
        Type[] methodParameterTypes = method.getGenericParameterTypes();
        Annotation[][] methodParameterAnnotationArrays = method.getParameterAnnotations();

        // TODO add additional param parsing here if we need it
    }
}
