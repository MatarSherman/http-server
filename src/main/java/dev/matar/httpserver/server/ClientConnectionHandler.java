package dev.matar.httpserver.server;

import static dev.matar.httpserver.server.HttpRequestDeserializer.deserialize;

import dev.matar.httpserver.exception.HttpBodySerializationException;
import dev.matar.httpserver.exception.HttpConnectionHandlingException;
import dev.matar.httpserver.exception.HttpDeserializationException;
import dev.matar.httpserver.exception.HttpRequestSizeLimitException;
import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.model.server.Route;
import dev.matar.httpserver.model.server.Routes;
import dev.matar.httpserver.server.metadata.HttpMetadataHandler;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class ClientConnectionHandler {
  private final Routes routes;
  private final StaticResourcesHandler staticResourcesHandler;

  public ClientConnectionHandler(Routes routes, StaticResourcesHandler staticResourcesHandler) {
    this.routes = routes;
    this.staticResourcesHandler = staticResourcesHandler;
  }

  public void handleSocket(Socket clientSocket) {
    try (clientSocket) {
      HttpStreamReader httpStreamReader = new HttpStreamReader(clientSocket.getInputStream());
      HttpResponse<?> response;
      HttpRequest request = null;
      do {
        try {
          Optional<HttpRequest> optionalRequest = deserializeRequest(httpStreamReader);
          if (optionalRequest.isEmpty()) {
            System.out.println("DEBUG: received an empty HTTP request, closing socket.");
            return;
          }
          request = optionalRequest.get();
          System.out.println(
              "INFO: received request " + request.getMethod() + " " + request.getPath());
          response = generateResponse(request);
        } catch (HttpConnectionHandlingException e) {
          response = generateResponse(request, e);
        }
        sendResponseToClient(response, clientSocket);
      } while (checkIsKeepAlive(response));
    } catch (IOException e) {
      System.out.println("INFO: connection failed for client socket, " + e);
    } catch (HttpBodySerializationException e) {
      System.out.println("ERROR: body serialization failed, aborting client socket connection");
    }
  }

  private Optional<HttpRequest> deserializeRequest(HttpStreamReader httpStreamReader)
      throws IOException, HttpConnectionHandlingException {
    try {
      return deserialize(httpStreamReader);
    } catch (HttpRequestSizeLimitException e) {
      System.out.println("INFO: deserialization aborted due to exceeded request size limit, " + e);
      throw new HttpConnectionHandlingException(e.getHttpStatus(), "Request Too Large", e);
    } catch (HttpDeserializationException e) {
      System.out.println("INFO: deserialization aborted due to invalid format of request, " + e);
      throw new HttpConnectionHandlingException(HttpStatus.BAD_REQUEST, "Bad Request", e);
    }
  }

  private HttpResponse<?> generateResponse(HttpRequest request)
      throws HttpConnectionHandlingException, IOException {
    HttpResponse<?> response;
    if (staticResourcesHandler.checkIsStaticResource(request)) {
      response = staticResourcesHandler.getResourceResponse(request);
      HttpMetadataHandler.configureStaticResource(request, response);
    } else {
      response = runRouteForRequest(request);
      HttpMetadataHandler.configureResponse(request, response);
    }
    return response;
  }

  private HttpResponse<?> generateResponse(HttpRequest request, HttpConnectionHandlingException e) {
    HttpResponse<?> response = new HttpResponse<>(e.getStatus(), e.getStatusMessage());
    if (request != null) {
      HttpMetadataHandler.configureResponse(request, response);
    }
    return response;
  }

  private HttpResponse<?> runRouteForRequest(HttpRequest request)
      throws HttpConnectionHandlingException {
    Optional<Route> route = this.routes.getRoute(request);
    if (route.isEmpty()) {
      throw new HttpConnectionHandlingException(HttpStatus.NOT_FOUND, "Not Found");
    }
    try {
      return route.get().run(request);
    } catch (Exception e) {
      System.out.println(
          "INFO: uncaught error by route: "
              + request.getMethod()
              + " "
              + request.getPath()
              + ". returning status 500");
      throw new HttpConnectionHandlingException(HttpStatus.INTERNAL_ERROR, "Internal Error", e);
    }
  }

  private void sendResponseToClient(HttpResponse<?> response, Socket clientSocket)
      throws IOException, HttpBodySerializationException {
    HttpResponseSerializer.serialize(response, clientSocket.getOutputStream());
  }

  private boolean checkIsKeepAlive(HttpResponse<?> response) {
    String keepAlive = response.getHeaders().getFirst(HttpHeaderKey.CONNECTION.value()).orElse("");

    return keepAlive.equalsIgnoreCase(HttpHeaderValue.KEEP_ALIVE.value());
  }
}
