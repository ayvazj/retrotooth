package retrotooth;

import java.io.IOException;

import static retrotooth.Utils.closeQuietly;

final class BleResponseConverter implements Converter<Response> {
    private final boolean isStreaming;

    BleResponseConverter(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    @Override
    public Response fromResponse(Response<Response> data) throws IOException {
        if (isStreaming) {
            return data;
        }

        // Buffer the entire body to avoid future I/O.
        try {
            return Utils.readBodyToBytesIfNecessary(data);
        } finally {
            closeQuietly(data);
        }
    }

    @Override
    public RequestData toData(Response value) {
        throw new UnsupportedOperationException();
    }
}
