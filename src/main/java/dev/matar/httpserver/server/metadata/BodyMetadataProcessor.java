package dev.matar.httpserver.server.metadata;

import dev.matar.httpserver.model.http.*;
import java.util.List;

public class BodyMetadataProcessor implements HttpMetadataProcessor {
  private static final List<HttpMetadataProcessor> bodyMetadataProcessors = List.of();

  @Override
  public void process(HttpRequest request, HttpResponse<?> response) {
    Object body = response.getBody();
    if (body == null) {
      configureNoBodyResponse(response);
    }
  }

  private static void configureNoBodyResponse(HttpResponse<?> response) {
    response.getHeaders().set(HttpHeaderKey.CONTENT_LENGTH.value(), "" + 0);
  }
}
