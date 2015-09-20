package retrotooth;


import static retrotooth.Utils.checkNotNull;

public final class Response<T> {

    /**
     * TODO
     */
    public static <T> Response<T> success(T body, byte[] rawResponse) {
        return new Response<>(rawResponse, body, null, true);
    }

    /**
     * TODO
     */
    public static <T> Response<T> error(ResponseData body, byte[] rawResponse) {
        return new Response<>(rawResponse, null, body, false);
    }

    private final byte[] rawResponse;
    private final T data;
    private final ResponseData errorBody;
    private final boolean isSuccess;

    private Response(byte[] rawResponse, T data, ResponseData errorBody, boolean isSuccess) {
        this.rawResponse = checkNotNull(rawResponse, "rawResponse == null");
        this.data = data;
        this.errorBody = errorBody;
        this.isSuccess = isSuccess;
    }

    public byte[] raw() {
        return rawResponse;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * The deserialized response data of a {@linkplain #isSuccess() successful} response.
     */
    public T data() {
        return data;
    }

    /**
     * The raw response data of an {@linkplain #isSuccess() unsuccessful} response.
     */
    public ResponseData errorBody() {
        return errorBody;
    }
}
