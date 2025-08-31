package dev.matar.httpserver;

import dev.matar.httpserver.model.http.HttpMethod;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.server.serializer.body.StreamResource;
import dev.matar.httpserver.server.Server;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/** This code is an example for usage of the server */
public class Main {
  record User(String name, String password, int age, boolean isPremium) {}

  public static void main(String[] args) {
    final int PORT = 8765;
    try {
      Server server = new Server(PORT);

      server.addRoute(
          HttpMethod.GET,
          "/",
          _ -> new HttpResponse<>(200, "", new User("Matar", "matar123", 23, true)));
      server.addRoute(
          HttpMethod.GET, "/num", _ -> new HttpResponse<>(200, "random", Math.random()));
      server.addRoute(
          HttpMethod.GET,
          "/messageStream",
          _ -> {
            InputStream stream = new ByteArrayInputStream("Hello World!".getBytes());
            return new HttpResponse<>(200, "my-message", new StreamResource(stream));
          });
      server.setStaticResources("/resource/", "src/main/resources");
      server.start();
    } catch (IOException e) {
      System.out.println("ERROR: could not start server" + e);
    }
  }
}
