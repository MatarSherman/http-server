package dev.matar.httpserver.server;

import dev.matar.httpserver.model.http.HttpMethod;
import dev.matar.httpserver.model.server.Route;
import dev.matar.httpserver.model.server.Routes;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
  private static final int DEFAULT_TIMEOUT_DURATION_MS = 30000;

  private final Routes routes;
  private final ServerSocket socket;
  private final Executor threadPool;

  private final StaticResourcesHandler staticResourcesHandler;

  public Server(int port) throws IOException {
    routes = new Routes();
    threadPool = Executors.newVirtualThreadPerTaskExecutor();
    socket = new ServerSocket(port);
    staticResourcesHandler = new StaticResourcesHandler();
    System.out.println("Initiated server with port " + port);
  }

  public void start() {
    System.out.println("INFO: server listening for requests...");
    ClientConnectionHandler connectionHandler =
        new ClientConnectionHandler(routes, staticResourcesHandler);
    try {
      while (true) {
        Socket clientSocket = socket.accept();
        clientSocket.setSoTimeout(DEFAULT_TIMEOUT_DURATION_MS);
        threadPool.execute(() -> connectionHandler.handleSocket(clientSocket));
      }
    } catch (IOException e) {
      System.out.println("ERROR: connection failed. " + e);
    }
  }

  public void addRoute(HttpMethod method, String path, Route route) {
    routes.addRoute(method, path, route);
  }

  public void setStaticResources(String endpoint, String path) {
    this.staticResourcesHandler.setPath(endpoint, path);
  }
}
