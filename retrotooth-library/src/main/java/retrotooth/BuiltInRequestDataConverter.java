package retrotooth;

import java.io.IOException;

/**
 * Created by pwner on 9/19/15.
 */
public class BuiltInRequestDataConverter implements Converter<RequestData, RequestData> {
    @Override
    public RequestData convert(RequestData value) throws IOException {
        return value;
    }
}