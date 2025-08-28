package dev.matar.httpserver.server.bodySerializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.OutputStream;

public class PrimitiveBodySerializer implements HttpBodySerializer<Object> {

  @Override
  public void serialize(Object body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    byte[] result = String.valueOf(body).getBytes(Constants.DEFAULT_CHARSET);

    HttpHeaders contentHeaders = new HttpHeaders();
    if (!response.getHeaders().containsKey(HttpHeaderKey.CONTENT_TYPE.value())) {
      contentHeaders.set(
          HttpHeaderKey.CONTENT_TYPE.value(),
          MimeType.TEXT_PLAIN.value()
              + HttpHeader.SUB_PARAM_SEPARATOR
              + HttpHeader.CHARSET_PARAM_INDICATOR
              + Constants.DEFAULT_CHARSET.name());
    }
    contentHeaders.set(HttpHeaderKey.CONTENT_LENGTH.value(), result.length + "");

    HttpResponseSerializer.writeHeaders(contentHeaders, outputStream);
    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    outputStream.write(result);
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    return body instanceof Number || body instanceof Boolean || body instanceof Character;
  }
}
