package dev.matar.httpserver.model.server;

import dev.matar.httpserver.model.http.HttpMethod;
import dev.matar.httpserver.model.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Routes {
  private final Map<String, Route> routeMap;

  public Routes() {
    routeMap = new HashMap<>();
  }

  private String routeKey(HttpMethod method, String path) {
    return method.name() + "-" + path;
  }

  private String routeKey(HttpRequest request) {
    return routeKey(request.getMethod(), request.getPath());
  }

  public Optional<Route> getRoute(HttpRequest req) {
    Route route = routeMap.get(routeKey(req));
    if (route == null) {
      return Optional.empty();
    }
    return Optional.of(route);
  }

  public void addRoute(HttpMethod method, String path, Route routeHandler) {
    routeMap.put(routeKey(method, path), routeHandler);
  }
}
