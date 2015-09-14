package retrotooth;

import java.io.Closeable;
import java.io.IOException;

public class Response<T> implements Closeable {

    private final byte[] data;
    private final T content;

    public Response(byte[] data, T content, Object o) {
        this.data = data;
        this.content = content;
    }

    public Response(byte[] data) {
        this.data = data;
        this.content = null; // TODO parse data
    }

    public static <T> Response<T> success(T content, byte[] data) {
        return new Response<>(data, content, null);
    }

    @Override
    public void close() throws IOException {

    }
}
