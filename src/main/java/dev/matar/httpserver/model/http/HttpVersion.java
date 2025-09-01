package dev.matar.httpserver.model.http;

public enum HttpVersion {
  V1("HTTP/1");

  private final String value;

  HttpVersion(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}
