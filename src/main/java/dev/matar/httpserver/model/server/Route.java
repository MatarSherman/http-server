package dev.matar.httpserver.model.server;

import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;

public interface Route {
  HttpResponse<?> run(HttpRequest request) throws Exception;
}
