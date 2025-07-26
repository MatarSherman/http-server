package dev.matar.httpserver.model.http;

public enum MimeType {
  TEXT_PLAIN("text/plain"),
  TEXT_HTML("text/html"),
  TEXT_CSS("text/css"),
  TEXT_JS("text/javascript"),
  TEXT_CSV("text/csv"),
  TEXT_XML("text/xml"),
  APP_JS("application/javascript"),
  APP_XML("application/xml"),
  BINARY("application/octet-stream"),
  JSON("application/json");

  private final String value;

  MimeType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
