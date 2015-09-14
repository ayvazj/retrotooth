package retrotooth;


import android.bluetooth.BluetoothGatt;

import java.io.IOException;

final class BluetoothCall<T> implements Call<T> {
    private final BluetoothGatt client;
    private final RequestFactory requestFactory;
    private final Converter<T> responseConverter;
    private final RetrotoothGattCallback retrotoothGattCallback;
    private final Object[] args;

    private boolean executed; // Guarded by this.
    private volatile boolean canceled;
    private GattCall rawCall;

    BluetoothCall(BluetoothGatt client, RequestFactory requestFactory, Converter<T> responseConverter,
                  RetrotoothGattCallback retrotoothGattCallback, Object[] args) {
        this.client = client;
        this.requestFactory = requestFactory;
        this.responseConverter = responseConverter;
        this.retrotoothGattCallback = retrotoothGattCallback;
        this.args = args;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    // We are a final type & this saves clearing state.
    @Override
    public BluetoothCall<T> clone() {
        return new BluetoothCall<>(client, requestFactory, responseConverter, retrotoothGattCallback, args);
    }

    @Override
    public Response<T> execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed");
            executed = true;
        }

        GattCall rawCall = createRawCall();
        if (canceled) {
            rawCall.cancel();
        }
        this.rawCall = rawCall;

        return parseResponse(rawCall.execute());
    }

    public void enqueue(final Callback<T> callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed");
            executed = true;
        }

        GattCall rawCall;
        try {
            rawCall = createRawCall();
        } catch (Throwable t) {
            callback.onFailure(t);
            return;
        }
        if (canceled) {
            rawCall.cancel();
        }
        this.rawCall = rawCall;

        rawCall.enqueue(new GattCallback() {
            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private void callSuccess(Response<T> response) {
                try {
                    callback.onResponse(response);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                callFailure(e);
            }

            @Override
            public void onResponse(byte[] rawResponse) {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    callFailure(e);
                    return;
                }
                callSuccess(response);
            }
        });
    }


    private GattCall createRawCall() {
        return new GattCall(client, requestFactory.create(args), this.retrotoothGattCallback);
    }

    private Response<T> parseResponse(byte[] data) throws IOException {
        ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(data);
        try {
            T resp = responseConverter.fromResponse(new Response<T>(data));
            return Response.success(resp, data);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    public void cancel() {
        canceled = true;
        GattCall rawCall = this.rawCall;
        if (rawCall != null) {
            rawCall.cancel();
        }
    }
}

