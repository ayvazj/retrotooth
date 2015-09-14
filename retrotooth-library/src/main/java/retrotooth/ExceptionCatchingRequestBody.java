package retrotooth;

import java.io.IOException;


public class ExceptionCatchingRequestBody {
    private final byte[] data;
    private IOException thrownException;

    public ExceptionCatchingRequestBody(byte[] data) {
        this.data = data;
    }

    void throwIfCaught() throws IOException {
        if (thrownException != null) {
            throw thrownException;
        }
    }
}
