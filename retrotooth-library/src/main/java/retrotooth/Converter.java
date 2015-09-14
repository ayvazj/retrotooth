package retrotooth;

import java.io.IOException;
import java.lang.reflect.Type;

/** Convert objects to and from their representation as HTTP bodies. */
public interface Converter<T> {
    /** Convert a byte array response to a concrete object of the specified type. */
    T fromResponse(Response<T> data) throws IOException;

    /** Convert an object to an appropriate representation for BLE transport. */
    RequestData toData(T value);

    interface Factory {
        Converter<?> get(Type type);
    }
}