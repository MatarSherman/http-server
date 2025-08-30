package dev.matar.httpserver.server.metadata;

import dev.matar.httpserver.model.http.*;
import java.util.List;

public class HttpMetadataHandler {
  private static final List<HttpMetadataProcessor> metadataProcessors =
      List.of(
          new GlobalMetadataProcessor(),
          new BodyMetadataProcessor(),
          new StatusMetadataProcessor());

  private static final List<HttpMetadataProcessor> staticResourcesProcessors =
      List.of(new RangeMetadataProcessor());

  public static void configureResponse(HttpRequest request, HttpResponse<?> response) {
    metadataProcessors.forEach(
        httpMetadataProcessor -> httpMetadataProcessor.process(request, response));
  }

  public static void configureStaticResource(HttpRequest request, HttpResponse<?> response) {
    staticResourcesProcessors.forEach(
        httpMetadataProcessor -> httpMetadataProcessor.process(request, response));
    metadataProcessors.forEach(
        httpMetadataProcessor -> httpMetadataProcessor.process(request, response));
  }
}
