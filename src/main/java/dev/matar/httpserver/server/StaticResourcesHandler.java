package dev.matar.httpserver.server;

import dev.matar.httpserver.model.http.HttpMethod;
import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.http.HttpStatus;
import dev.matar.httpserver.model.server.serializer.body.FileResource;
import dev.matar.httpserver.model.server.serializer.body.ResourceBody;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticResourcesHandler {
  private Path path;
  private String endpoint;

  public void setPath(String endpoint, String path) {
    Path staticPath = Paths.get(path);

    if (!Files.exists(staticPath) || !Files.isDirectory(staticPath)) {
      throw new IllegalArgumentException("ERROR: Static resources path must represent a directory");
    }
    this.path = staticPath;
    this.endpoint = endpoint;
  }

  public boolean checkIsStaticResource(HttpRequest request) {
    return this.endpoint != null
        && request.getMethod().equals(HttpMethod.GET)
        && request.getPath().startsWith(this.endpoint);
  }

  public HttpResponse<ResourceBody> getResourceResponse(HttpRequest request) {
    Path resourceRelativePath = Path.of(request.getPath().replaceFirst(this.endpoint, ""));

    Path resource = path.resolve(resourceRelativePath).normalize();

    if (!Files.exists(resource)
        || !Files.isRegularFile(resource)
        || !resource.startsWith(this.path)) {
      return new HttpResponse<>(HttpStatus.NOT_FOUND.code(), "Not Found");
    }
    try {
      return new HttpResponse<>(HttpStatus.OK.code(), "OK", new FileResource(resource));
    } catch (IOException e) {
      return new HttpResponse<>(HttpStatus.INTERNAL_ERROR, "Internal error");
    }
  }
}
