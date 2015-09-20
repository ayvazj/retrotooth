package retrotooth;


import java.io.IOException;

final class VoidConverter implements Converter<ResponseData, Void> {
    @Override
    public Void convert(ResponseData value) throws IOException {
        value.close();
        return null;
    }
}