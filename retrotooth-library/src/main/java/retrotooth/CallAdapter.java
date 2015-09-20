package retrotooth;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface CallAdapter<T> {
    /**
     * Returns the value type that this adapter uses when converting the response body to a Java
     * object. For example, the response type for {@code Call<Repo>} is {@code Repo}. This type
     * is used to prepare the {@code call} passed to {@code #adapt}.
     * <p/>
     * <p>Note that this is typically not the same type as the {@code returnType} provided to
     * this call adapter's factory.
     */
    Type responseType();

    /** Returns an instance of the {@code T} which adapts the execution of {@code call}. */
    <R> T adapt(Call<R> call);

    interface Factory {

        /**
         * Returns a call adapter for calls that return {@code returnType}, or null if this factory
         * doesn't adapt that type.
         */
        CallAdapter<?> get(Type returnType, Annotation[] annotations);
    }
}