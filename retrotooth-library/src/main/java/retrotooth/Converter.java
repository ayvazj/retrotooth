package retrotooth;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


public interface Converter<F, T> {
    T convert(F value) throws IOException;

    abstract class Factory {
        /**
         * Create a {@link Converter} for converting an HTTP response body to {@code type} or null if it
         * cannot be handled by this factory.
         */
        public Converter<ResponseData, ?> fromResponseBody(Type type, Annotation[] annotations) {
            return null;
        }

        /**
         * Create a {@link Converter} for converting {@code type} to an HTTP request body or null if it
         * cannot be handled by this factory.
         */
        public Converter<?, RequestData> toRequestBody(Type type, Annotation[] annotations) {
            return null;
        }
    }
}
