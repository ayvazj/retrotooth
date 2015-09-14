package retrotooth;

/**
 * Communicates responses from a server or offline requests. One and only one method will be
 * invoked in response to a given request.
 * <p/>
 * Callback methods are executed using the {@link Retrofit} callback executor. When none is
 * specified, the following defaults are used:
 * <ul>
 * <li>Android: Callbacks are executed on the application's main (UI) thread.</li>
 * </ul>
 *
 * @param <T> expected response type
 */
public interface Callback<T> {
    /**
     * Successful response.
     */
    void onResponse(Response<T> response);

    /**
     * Invoked when an unexpected exception occurred during.
     */
    void onFailure(Throwable t);
}
