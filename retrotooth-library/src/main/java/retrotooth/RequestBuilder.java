package retrotooth;

import java.util.UUID;

final class RequestBuilder {
    private final UUID service;
    private final UUID characteristic;
    private final BluetoothOperation bluetoothOperation;

    private final Request.Builder requestBuilder;

    private final boolean hasBody;
    private RequestData body;

    RequestBuilder(UUID service, UUID characteristic, BluetoothOperation bluetoothOperation, boolean hasBody) {
        this.service = service;
        this.characteristic = characteristic;
        this.bluetoothOperation = bluetoothOperation;

        Request.Builder requestBuilder = new Request.Builder();

        this.requestBuilder = requestBuilder;
        this.hasBody = hasBody;

//        if (isFormEncoded) {
//        Will be set to 'body' in 'build'.
//            formEncodingBuilder = new FormEncodingBuilder();
//        } else if (isMultipart) {
//                Will be set to 'body' in 'build'.
//            multipartBuilder = new MultipartBuilder();
//        }
    }


    void setData(RequestData body) {
        this.body = body;
    }

    Request build() {
//        HttpUrl url;
//        HttpUrl.Builder urlBuilder = this.urlBuilder;
//        if (urlBuilder != null) {
//            url = urlBuilder.build();
//        } else {
//             No query parameters triggered builder creation, just combine the relative URL and base URL.
//            url = baseUrl.resolve(relativeUrl);
//        }

        RequestData body = this.body;
        if (body == null) {
            // Try to pull from one of the builders.
//            if (formEncodingBuilder != null) {
//                body = formEncodingBuilder.build();
//            } else if (multipartBuilder != null) {
//                body = multipartBuilder.build();
//            } else if (hasBody) {
//                 Body is absent, make an empty body.
//                body = RequestBody.create(null, new byte[0]);
//            }
        }


        return requestBuilder
                .characteristic(characteristic)
                .service(service)
                .bluetoothOperation(bluetoothOperation)
                .build();
    }
}
