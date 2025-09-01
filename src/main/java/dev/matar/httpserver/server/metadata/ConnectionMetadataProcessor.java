package dev.matar.httpserver.server.metadata;

import dev.matar.httpserver.model.http.*;
import java.util.Optional;

/**
 * {@link HttpMetadataProcessor} Implementation for setting connection headers.
 *
 * <p>It must be used with "positive" status codes (1xx - 3xx), to comply with HTTP protocol
 * standards
 */
public class ConnectionMetadataProcessor implements HttpMetadataProcessor {
  @Override
  public void process(HttpRequest request, HttpResponse<?> response) {
    String connection;
    Optional<String> requestConnection =
        request.getHeaders().getFirst(HttpHeaderKey.CONNECTION.value());

    if (requestConnection.isPresent()) {
      connection = getConnectionByReqHeader(requestConnection.get());
    } else {
      connection = getDefaultConnection(request.getVersion());
    }
    response.getHeaders().set(HttpHeaderKey.CONNECTION.value(), connection);
  }

  public String getDefaultConnection(String httpVersion) {
    if (httpVersion.equals(HttpVersion.V1.value())) {
      return HttpHeaderValue.CONNECTION_CLOSE.value();
    }
    return HttpHeaderValue.KEEP_ALIVE.value();
  }

  public String getConnectionByReqHeader(String connectionHeader) {
    if (connectionHeader.equalsIgnoreCase(HttpHeaderValue.KEEP_ALIVE.value())) {
      return HttpHeaderValue.KEEP_ALIVE.value();
    }
    return HttpHeaderValue.CONNECTION_CLOSE.value();
  }
}
