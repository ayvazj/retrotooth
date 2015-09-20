package retrotooth;

import java.io.IOException;

import static retrotooth.Utils.closeQuietly;

/**
 * Created by pwner on 9/19/15.
 */
public class BuiltInResponseDataConverter implements Converter<ResponseData, ResponseData> {

    BuiltInResponseDataConverter() {
    }

    @Override
    public ResponseData convert(ResponseData value) throws IOException {
        // Buffer the entire body to avoid future I/O.
        try {
            return Utils.readBodyToBytesIfNecessary(value);
        } finally {
            closeQuietly(value);
        }
    }
}
