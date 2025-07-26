package dev.matar.httpserver.model.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.http.MimeType;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.OutputStream;

public class JSONBodySerializer implements HttpBodySerializer<Object> {
  private final ObjectMapper objectMapper;

  public JSONBodySerializer() {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void serialize(Object body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    if (!response.hasHeader(HttpHeaderKey.CONTENT_TYPE.value())) {
      outputStream.write(
          HttpResponseSerializer.serializeHeader(HttpHeader.contentType(MimeType.JSON))
              .getBytes(Constants.DEFAULT_CHARSET));
    }
    byte[] result = objectMapper.writeValueAsBytes(body);

    outputStream.write(
        HttpResponseSerializer.serializeHeader(HttpHeader.contentLength(result.length))
            .getBytes(Constants.DEFAULT_CHARSET));
    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    outputStream.write(result);
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    return true;
  }
}
