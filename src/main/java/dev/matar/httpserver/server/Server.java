package dev.matar.httpserver.server;

import static dev.matar.httpserver.server.HttpRequestDeserializer.deserialize;

import dev.matar.httpserver.exception.HttpRequestSizeLimitException;
import dev.matar.httpserver.exception.InvalidHttpRequestException;
import dev.matar.httpserver.exception.InvalidHttpResponseException;
import dev.matar.httpserver.model.http.HttpMethod;
import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.server.Route;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
  private static final int DEFAULT_TIMEOUT_DURATION_MS = 30000;

  private final Map<String, Route> routes;
  private final ServerSocket socket;
  private final Executor threadPool;

  public Server(int port) throws IOException {
    routes = new HashMap<>();
    threadPool = Executors.newVirtualThreadPerTaskExecutor();
    socket = new ServerSocket(port);
    System.out.println("Initiated server with port " + port);
  }

  private String routeKey(HttpMethod method, String path) {
    return method.name() + "-" + path;
  }

  public void addRoute(HttpMethod method, String path, Route routeHandler) {
    routes.put(routeKey(method, path), routeHandler);
  }

  private Optional<Route> getRoute(HttpRequest req) {
    Route route = routes.get(routeKey(req.getMethod(), req.getPath()));
    if (route == null) {
      return Optional.empty();
    }
    return Optional.of(route);
  }

  public void start() {
    System.out.println("INFO: server listening for requests...");
    try {
      while (true) {
        Socket clientSocket = socket.accept();
        clientSocket.setSoTimeout(DEFAULT_TIMEOUT_DURATION_MS);
        threadPool.execute(() -> handleSocket(clientSocket));
      }
    } catch (IOException e) {
      System.out.println("ERROR: connection failed");
    }
  }

  private void handleSocket(Socket socket) {
    try (socket) {
      Optional<HttpRequest> request;
      try {
        request = deserialize(socket.getInputStream());
      } catch (HttpRequestSizeLimitException e) {
        System.out.println(
            "INFO: deserialization aborted due to exceeded request size limit, " + e);
        handleInvalidRequest(socket, e.getHttpStatus(), "Request Too Large");
        return;
      }

      if (request.isEmpty()) {
        System.out.println("INFO: received empty request, closing socket");
        return;
      }
      System.out.println(
          "INFO: received request " + request.get().getMethod() + " " + request.get().getPath());

      Optional<Route> route = getRoute(request.get());
      if (route.isEmpty()) {
        handleInvalidRequest(socket, 404, "Not Found");
        return;
      }
      HttpResponse<?> response = route.get().run(request.get());
      HttpResponseSerializer.serialize(response, socket.getOutputStream());
    } catch (IOException e) {
      System.out.println("ERROR: connection failed for client socket, " + e);
    } catch (InvalidHttpRequestException e) {
      System.out.println("INFO: deserialization failed due to invalid HTTP request, " + e);
    } catch (InvalidHttpResponseException e) {
      System.out.println("INFO: serialization failed due to invalid HTTP response, " + e);
    }
  }

  private void handleInvalidRequest(Socket socket, int status, String message)
      throws IOException, InvalidHttpResponseException {
    HttpResponse<?> response = new HttpResponse<>(status, message);
    HttpResponseSerializer.serialize(response, socket.getOutputStream());
  }
}
