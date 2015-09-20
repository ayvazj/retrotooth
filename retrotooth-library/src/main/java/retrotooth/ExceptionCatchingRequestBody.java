package retrotooth;

import java.io.IOException;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import retrotooth.ok.MediaType;


final class ExceptionCatchingRequestBody extends ResponseData {
    private final ResponseData delegate;
    private IOException thrownException;

    ExceptionCatchingRequestBody(ResponseData delegate) {
        this.delegate = delegate;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            thrownException = e;
            throw e;
        }
    }

    @Override
    public BufferedSource source() throws IOException {
        BufferedSource delegateSource;
        try {
            delegateSource = delegate.source();
        } catch (IOException e) {
            thrownException = e;
            throw e;
        }
        return Okio.buffer(new ForwardingSource(delegateSource) {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                try {
                    return super.read(sink, byteCount);
                } catch (IOException e) {
                    thrownException = e;
                    throw e;
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    void throwIfCaught() throws IOException {
        if (thrownException != null) {
            throw thrownException;
        }
    }
}

