package dev.matar.httpserver.server.serializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.http.MimeType;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.OutputStream;

public class PrimitiveBodySerializer implements HttpBodySerializer<Object> {

  @Override
  public void serialize(Object body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    if (!response.getHeaders().containsKey(HttpHeaderKey.CONTENT_TYPE.value())) {
      outputStream.write(
          HttpResponseSerializer.serializeHeader(
                  HttpHeader.contentType(
                      MimeType.TEXT_PLAIN.value()
                          + HttpHeader.SUB_PARAM_SEPARATOR
                          + HttpHeader.CHARSET_PARAM_INDICATOR
                          + Constants.DEFAULT_CHARSET.name()))
              .getBytes(Constants.DEFAULT_CHARSET));
    }
    byte[] result = String.valueOf(body).getBytes(Constants.DEFAULT_CHARSET);

    outputStream.write(
        HttpResponseSerializer.serializeHeader(HttpHeader.contentLength(result.length))
            .getBytes(Constants.DEFAULT_CHARSET));
    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    outputStream.write(result);
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    return body instanceof Number || body instanceof Boolean || body instanceof Character;
  }
}
