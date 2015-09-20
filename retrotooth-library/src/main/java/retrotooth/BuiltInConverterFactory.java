package retrotooth;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class BuiltInConverterFactory extends Converter.Factory {
    @Override
    public Converter<ResponseData, ?> fromResponseBody(Type type, Annotation[] annotations) {
        if (ResponseData.class.equals(type)) {
            return new BuiltInResponseDataConverter();
        }
        if (String.class.equals(type)) {
            return new BuiltInResponseDataConverter();
        }
        if (Void.class.equals(type)) {
            return new VoidConverter();
        }
        return null;
    }

    @Override public Converter<?, RequestData> toRequestBody(Type type, Annotation[] annotations) {
        if (type instanceof Class && RequestData.class.isAssignableFrom((Class<?>) type)) {
            return new BuiltInRequestDataConverter();
        }
        return null;
    }
}
