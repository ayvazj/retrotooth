package retrotooth;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;
import okio.Source;
import retrotooth.ok.MediaType;

public class RequestData {
    public static RequestData create(MediaType contentType, String content) {
        Charset charset = Utils.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Utils.UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }

        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    public static RequestData create(final MediaType contentType, final ByteString content) {
        return new RequestData() {
            public MediaType contentType() {
                return contentType;
            }

            public long contentLength() throws IOException {
                return (long) content.size();
            }

            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    public static RequestData create(MediaType contentType, byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    public static RequestData create(final MediaType contentType, final byte[] content, final int offset, final int byteCount) {
        if (content == null) {
            throw new NullPointerException("content == null");
        } else {
            Utils.checkOffsetAndCount((long) content.length, (long) offset, (long) byteCount);
            return new RequestData() {
                public MediaType contentType() {
                    return contentType;
                }

                public long contentLength() {
                    return (long) byteCount;
                }

                public void writeTo(BufferedSink sink) throws IOException {
                    sink.write(content, offset, byteCount);
                }
            };
        }
    }

    public static RequestData create(final MediaType contentType, final File file) {
        if (file == null) {
            throw new NullPointerException("content == null");
        } else {
            return new RequestData() {
                public MediaType contentType() {
                    return contentType;
                }

                public long contentLength() {
                    return file.length();
                }

                public void writeTo(BufferedSink sink) throws IOException {
                    Source source = null;

                    try {
                        source = Okio.source(file);
                        sink.writeAll(source);
                    } finally {
                        Utils.closeQuietly(source);
                    }

                }
            };
        }
    }
}
