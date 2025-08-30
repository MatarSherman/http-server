package dev.matar.httpserver.server.bodySerializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.model.server.serializer.body.ResourceBody;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceBodySerializer implements HttpBodySerializer<ResourceBody> {
  @Override
  public void serialize(ResourceBody body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    HttpHeaders contentHeaders = new HttpHeaders();
    if (!response.getHeaders().containsKey(HttpHeaderKey.CONTENT_TYPE.value())) {
      contentHeaders.set(HttpHeaderKey.CONTENT_TYPE.value(), getContentType(body));
    }
    long contentLength = body.getContentLength();
    if (contentLength == ResourceBody.UNKNOWN_CONTENT_LENGTH) {
      contentHeaders.set(
          HttpHeaderKey.TRANSFER_ENCODING.value(), HttpHeaderValue.TRANSFER_ENC_CHUNKED.value());

      HttpResponseSerializer.writeEndOfHeaders(contentHeaders, outputStream);
      serializeChunked(body, outputStream);
    } else {
      contentHeaders.set(HttpHeaderKey.CONTENT_LENGTH.value(), body.getContentLength() + "");

      HttpResponseSerializer.writeEndOfHeaders(contentHeaders, outputStream);
      body.getInputStream().transferTo(outputStream);
    }
  }

  public void serializeChunked(ResourceBody body, OutputStream outputStream) throws IOException {
    InputStream inputStream = body.getInputStream();
    int CHUNK_SIZE = 4096;
    byte[] chunk = new byte[CHUNK_SIZE];

    int bytesRead;
    while ((bytesRead = inputStream.read(chunk)) != -1) {
      outputStream.write(
          (Integer.toHexString(bytesRead) + Constants.HTTP_CRLF)
              .getBytes(StandardCharsets.US_ASCII));

      outputStream.write(chunk, 0, bytesRead);
      HttpResponseSerializer.writeHttpCRLF(outputStream);
    }
    outputStream.write(
        (Integer.toHexString(0) + Constants.HTTP_CRLF).getBytes(StandardCharsets.US_ASCII));
    HttpResponseSerializer.writeHttpCRLF(outputStream);
  }

  public String getContentType(ResourceBody body) {
    try {
      Path filename = Paths.get(body.getFileName());
      String mime = Files.probeContentType(filename);

      return mime == null ? MimeType.BINARY.value() : mime;
    } catch (Exception e) {
      return MimeType.BINARY.value();
    }
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    return body instanceof ResourceBody;
  }
}
