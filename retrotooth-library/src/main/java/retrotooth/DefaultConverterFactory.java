package retrotooth;


import java.io.IOException;
import java.lang.reflect.Type;

public class DefaultConverterFactory implements Converter.Factory {
    @Override
    public Converter<?> get(Type type) {
        return new DefaultConverter();
    }

    static class DefaultConverter implements Converter<Object> {
        @Override
        public Object fromResponse(Response<Object> data) throws IOException {
            return data;
        }

        @Override
        public RequestData toData(Object value) {
            return new RequestData();
        }
    }
}

