package dev.matar.httpserver.model.serializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.http.MimeType;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.OutputStream;

public class BytesBodySerializer implements HttpBodySerializer<byte[]> {
  @Override
  public void serialize(byte[] body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    if (response.getHeaders().stream()
        .noneMatch(header -> header.key().equalsIgnoreCase(HttpHeaderKey.CONTENT_TYPE.value()))) {
      outputStream.write(
          HttpResponseSerializer.serializeHeader(HttpHeader.contentType(MimeType.BINARY))
              .getBytes(Constants.DEFAULT_CHARSET));
    }
    outputStream.write(
        HttpResponseSerializer.serializeHeader(HttpHeader.contentLength(body.length))
            .getBytes(Constants.DEFAULT_CHARSET));

    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    outputStream.write(body);
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    return body instanceof byte[];
  }
}
