package retrotooth;

import java.io.IOException;

/**
 * Created by pwner on 8/11/15.
 */
public interface GattCallback {
    void onFailure(Request req, IOException var2);

    void onResponse(byte[] resp) throws IOException;
}
