package dev.matar.httpserver.server.metadata;

import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;

public interface HttpMetadataProcessor {
  void process(HttpRequest request, HttpResponse<?> response);
}
