package dev.matar.httpserver.server.metadata;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import dev.matar.httpserver.model.http.*;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class HttpMetadataHandler {
  public static void configureResponse(HttpResponse<?> response) {
    setGlobalHeaders(response);
    setHeadersByStatus(response);
    setHeadersByBody(response);
  }

  public static void configureResponse(HttpRequest request, HttpResponse<?> response) {
    configureResponse(response);
  }

  private static void setGlobalHeaders(HttpResponse<?> response) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
    response.getHeaders().set("Date", RFC_1123_DATE_TIME.format(now));
  }

  private static void setHeadersByStatus(HttpResponse<?> response) {
    int status = response.getStatus();

    if (status >= 400 && status <= 599) {
      response.getHeaders().set("Connection", "close");
    }
  }

  private static void setHeadersByBody(HttpResponse<?> response) {
    if (response.getBody() == null) {
      configureNoBodyResponse(response);
    } else if (response.getBody() instanceof InputStream
        && (!response.getHeaders().containsKey(HttpHeaderKey.TRANSFER_ENCODING.value()))) {
      configureInputStreamResponse(response);
    }
  }

  private static void configureNoBodyResponse(HttpResponse<?> response) {
    response.getHeaders().set(HttpHeaderKey.CONTENT_LENGTH.value(), "" + 0);
  }

  private static void configureInputStreamResponse(HttpResponse<?> response) {
    response
        .getHeaders()
        .set(HttpHeaderKey.TRANSFER_ENCODING.value(), HttpHeaderValue.TRANSFER_ENC_CHUNKED.value());
  }
}
