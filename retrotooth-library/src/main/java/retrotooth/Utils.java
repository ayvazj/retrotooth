package retrotooth;


import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

final public class Utils {
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0L || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Returns true if {@code annotations} contains an instance of {@code cls}.
     */
    static boolean isAnnotationPresent(Annotation[] annotations,
                                       Class<? extends Annotation> cls) {
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

    static CallAdapter<?> resolveCallAdapter(List<CallAdapter.Factory> adapterFactories, Type type,
                                             Annotation[] annotations) {
        for (int i = 0, count = adapterFactories.size(); i < count; i++) {
            CallAdapter<?> adapter = adapterFactories.get(i).get(type, annotations);
            if (adapter != null) {
                return adapter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(type)
                .append(". Tried:");
        for (CallAdapter.Factory adapterFactory : adapterFactories) {
            builder.append("\n * ").append(adapterFactory.getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    static Converter<ResponseData, ?> resolveResponseBodyConverter(
            List<Converter.Factory> converterFactories, Type type, Annotation[] annotations) {
        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            Converter<ResponseData, ?> converter =
                    converterFactories.get(i).fromResponseBody(type, annotations);
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder =
                new StringBuilder("Could not locate ResponseBody converter for ").append(type)
                        .append(". Tried:");
        for (Converter.Factory converterFactory : converterFactories) {
            builder.append("\n * ").append(converterFactory.getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Replace a {@link ResponseData} with an identical copy whose body is backed by a
     * {@link Buffer} rather than a {@link Source}.
     */
    static ResponseData readBodyToBytesIfNecessary(final ResponseData body) throws IOException {
        if (body == null) {
            return null;
        }
        return ResponseData.create(body.contentType(), body.bytes());
    }

    static <T> void validateServiceClass(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("Only interface definitions are supported.");
        }
        // Prevent API interfaces from extending other interfaces. This not only avoids a bug in
        // Android (http://b.android.com/58753) but it forces composition of API declarations which is
        // the recommended pattern.
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("Interface definitions must not extend other interfaces.");
        }
    }


    /**
     * Return the originally proxied object
     *
     * @param proxiedObject
     * @return
     */
    static Class<?> getProxiedClass(Object proxiedObject) {
        Class<?> proxiedClass = proxiedObject.getClass();
        if (proxiedObject instanceof Proxy) {
            Class<?>[] ifaces = proxiedClass.getInterfaces();
            if (ifaces.length == 1) {
                proxiedClass = ifaces[0];
            } else {
                // TODO this only support one proxy which should suffice for now
                proxiedClass = ifaces[ifaces.length - 1];
            }
        }
        return proxiedClass;
    }

    public static Type getSingleParameterUpperBound(ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException(
                    "Expected one type argument but got: " + Arrays.toString(types));
        }
        Type paramType = types[0];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    public static boolean hasUnresolvableType(Type type) {
        if (type instanceof Class<?>) {
            return false;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                if (hasUnresolvableType(typeArgument)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType) {
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            return true;
        }
        if (type instanceof WildcardType) {
            return true;
        }
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + className);
    }

    // This method is copyright 2008 Google Inc. and is taken from Gson under the Apache 2.0 license.
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;

        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);

        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }
    }

    static RuntimeException methodError(Method method, String message, Object... args) {
        return methodError(null, method, message, args);
    }

    static RuntimeException methodError(Throwable cause, Method method, String message,
                                        Object... args) {
        message = String.format(message, args);
        IllegalArgumentException e = new IllegalArgumentException(message
                + "\n    for method "
                + method.getDeclaringClass().getSimpleName()
                + "."
                + method.getName());
        e.initCause(cause);
        return e;

    }

    static Type getCallResponseType(Type returnType) {
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
        final Type responseType = getSingleParameterUpperBound((ParameterizedType) returnType);

        // Ensure the Call response type is not Response, we automatically deliver the Response object.
        if (getRawType(responseType) == Response.class) {
            throw new IllegalArgumentException(
                    "Call<T> cannot use Response as its generic parameter. "
                            + "Specify the response body type only (e.g., Call<TweetResponse>).");
        }
        return responseType;
    }

    private Utils() {
        // No instances.
    }
}
