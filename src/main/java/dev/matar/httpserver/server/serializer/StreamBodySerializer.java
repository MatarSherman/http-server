package dev.matar.httpserver.server.serializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class StreamBodySerializer implements HttpBodySerializer<InputStream> {
  private static final int CHUNK_SIZE = 4096;

  @Override
  public void serialize(InputStream body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    if (!response.getHeaders().containsKey(HttpHeaderKey.CONTENT_TYPE.value())) {
      outputStream.write(
          HttpResponseSerializer.serializeHeader(HttpHeader.contentType(MimeType.BINARY))
              .getBytes(Constants.DEFAULT_CHARSET));
    }
    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    try (InputStream input = body) {
      transferChunked(input, outputStream);
    }
  }

  private void transferChunked(InputStream input, OutputStream outputStream) throws IOException {
    byte[] chunk = new byte[CHUNK_SIZE];

    int bytesRead;
    while ((bytesRead = input.read(chunk, 0, CHUNK_SIZE)) != -1) {
      outputStream.write(
          (Integer.toHexString(bytesRead) + Constants.HTTP_CRLF)
              .getBytes(StandardCharsets.US_ASCII));
      outputStream.write(chunk, 0, bytesRead);
      outputStream.write(Constants.HTTP_CRLF.getBytes(StandardCharsets.US_ASCII));
    }
    outputStream.write(
        (Integer.toHexString(0) + Constants.HTTP_CRLF).getBytes(StandardCharsets.US_ASCII));
    outputStream.write(Constants.HTTP_CRLF.getBytes(StandardCharsets.US_ASCII));
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    Optional<String> transferEncoding =
        response.getHeaders().getFirst(HttpHeaderKey.TRANSFER_ENCODING.value());

    return body instanceof InputStream
        && transferEncoding.isPresent()
        && transferEncoding.get().equals(HttpHeaderValue.TRANSFER_ENC_CHUNKED.value());
  }
}
