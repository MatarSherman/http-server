package dev.matar.httpserver.server;

import static dev.matar.httpserver.server.HttpRequestDeserializer.deserialize;

import dev.matar.httpserver.exception.HttpConnectionHandlingException;
import dev.matar.httpserver.exception.HttpDeserializationException;
import dev.matar.httpserver.exception.HttpRequestSizeLimitException;
import dev.matar.httpserver.exception.HttpSerializationException;
import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.http.HttpStatus;
import dev.matar.httpserver.model.server.Route;
import dev.matar.httpserver.model.server.Routes;
import dev.matar.httpserver.server.metadata.HttpMetadataHandler;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class ClientConnectionHandler {
  private final Routes routes;

  public ClientConnectionHandler(Routes routes) {
    this.routes = routes;
  }

  public void handleSocket(Socket clientSocket) {
    try (clientSocket) {
      HttpResponse<?> response;
      HttpRequest request = null;
      try {
        Optional<HttpRequest> optionalRequest = deserializeRequest(clientSocket);
        if (optionalRequest.isEmpty()) {
          System.out.println("DEBUG: received an empty HTTP request, closing socket.");
          return;
        }
        request = optionalRequest.get();
        System.out.println(
            "INFO: received request " + request.getMethod() + " " + request.getPath());

        response = runRouteForRequest(request);
      } catch (HttpConnectionHandlingException e) {
        response = new HttpResponse<>(e.getStatus(), e.getStatusMessage());
      }
      sendResponseToClient(response, clientSocket, request);
    } catch (IOException e) {
      System.out.println("INFO: connection failed for client socket, " + e);
    }
  }

  private Optional<HttpRequest> deserializeRequest(Socket socket)
      throws IOException, HttpConnectionHandlingException {
    try {
      return deserialize(socket.getInputStream());
    } catch (HttpRequestSizeLimitException e) {
      System.out.println("INFO: deserialization aborted due to exceeded request size limit, " + e);
      throw new HttpConnectionHandlingException(e.getHttpStatus(), "Request Too Large", e);
    } catch (HttpDeserializationException e) {
      System.out.println("INFO: deserialization aborted due to invalid format of request, " + e);
      throw new HttpConnectionHandlingException(HttpStatus.BAD_REQUEST, "Bad Request", e);
    }
  }

  private HttpResponse<?> runRouteForRequest(HttpRequest request)
      throws IOException, HttpConnectionHandlingException {
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

  private void sendResponseToClient(
      HttpResponse<?> response, Socket clientSocket, HttpRequest request) throws IOException {
    HttpMetadataHandler.configureResponse(request, response);
    try {
      HttpResponseSerializer.serialize(response, clientSocket.getOutputStream());
    } catch (HttpSerializationException e) {
      System.out.println("INFO: could not serialize response. returning status 500.");
      handleBadResponse(clientSocket);
    }
  }

  private void handleBadResponse(Socket socket) throws IOException {
    HttpResponse<?> response = new HttpResponse<>(HttpStatus.INTERNAL_ERROR, "Internal Error");
    HttpMetadataHandler.configureResponse(response);
    HttpResponseSerializer.serializeWithoutBody(response, socket.getOutputStream());
  }
}
