package dev.matar.httpserver.server.metadata;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.model.http.HttpResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class GlobalMetadataProcessor implements HttpMetadataProcessor {
  @Override
  public void process(HttpRequest request, HttpResponse<?> response) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
    response.getHeaders().set("Date", RFC_1123_DATE_TIME.format(now));
  }
}
