package dev.matar.httpserver.server.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.HttpSerializationException;
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
      throws IOException, HttpSerializationException {
    if (!response.getHeaders().containsKey(HttpHeaderKey.CONTENT_TYPE.value())) {
      outputStream.write(
          HttpResponseSerializer.serializeHeader(
                  HttpHeader.contentType(
                      MimeType.JSON.value()
                          + HttpHeader.SUB_PARAM_SEPARATOR
                          + HttpHeader.CHARSET_PARAM_INDICATOR
                          + Constants.DEFAULT_CHARSET.name()))
              .getBytes(Constants.DEFAULT_CHARSET));
    }
    byte[] result;
    try {
      result = objectMapper.writeValueAsBytes(body);
    } catch (JacksonException e) {
      throw new HttpSerializationException("ERROR: failed to serialize body using Jackson, ", e);
    }
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
