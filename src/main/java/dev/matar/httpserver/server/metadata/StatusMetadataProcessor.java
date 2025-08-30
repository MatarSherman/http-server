package dev.matar.httpserver.server.metadata;

import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;

public class StatusMetadataProcessor implements HttpMetadataProcessor {
  @Override
  public void process(HttpRequest request, HttpResponse<?> response) {
    int status = response.getStatus();

    if (status >= 400 && status <= 599) {
      response.getHeaders().set("Connection", "close");
    }
  }
}
