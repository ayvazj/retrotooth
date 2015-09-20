package retrotooth;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;
import retrotooth.ok.MediaType;

public abstract class ResponseData implements Closeable {
    private Reader reader;

    public ResponseData() {
    }

    public abstract MediaType contentType();

    public abstract long contentLength() throws IOException;

    public final InputStream byteStream() throws IOException {
        return this.source().inputStream();
    }

    public abstract BufferedSource source() throws IOException;

    public final byte[] bytes() throws IOException {
        long contentLength = this.contentLength();
        if (contentLength > 2147483647L) {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
        } else {
            BufferedSource source = this.source();

            byte[] bytes;
            try {
                bytes = source.readByteArray();
            } finally {
                Utils.closeQuietly(source);
            }

            if (contentLength != -1L && contentLength != (long) bytes.length) {
                throw new IOException("Content-Length and stream length disagree");
            } else {
                return bytes;
            }
        }
    }

    public final Reader charStream() throws IOException {
        Reader r = this.reader;
        return r != null ? r : (this.reader = new InputStreamReader(this.byteStream(), this.charset()));
    }

    public final String string() throws IOException {
        return new String(this.bytes(), this.charset().name());
    }

    private Charset charset() {
        MediaType contentType = this.contentType();
        return contentType != null ? contentType.charset(Utils.UTF_8) : Utils.UTF_8;
    }

    public static ResponseData create(MediaType contentType, String content) {
        Charset charset = Utils.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Utils.UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }

        Buffer buffer = (new Buffer()).writeString(content, charset);
        return create(contentType, buffer.size(), buffer);
    }

    public static ResponseData create(MediaType contentType, byte[] content) {
        Buffer buffer = (new Buffer()).write(content);
        return create(contentType, (long) content.length, buffer);
    }

    public static ResponseData create(final MediaType contentType, final long contentLength, final BufferedSource content) {
        if (content == null) {
            throw new NullPointerException("source == null");
        } else {
            return new ResponseData() {
                public MediaType contentType() {
                    return contentType;
                }

                public long contentLength() {
                    return contentLength;
                }

                public BufferedSource source() {
                    return content;
                }
            };
        }
    }

    @Override
    public void close() throws IOException {

    }
}
