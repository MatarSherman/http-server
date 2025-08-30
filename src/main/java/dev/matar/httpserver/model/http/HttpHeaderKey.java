package dev.matar.httpserver.model.http;

public enum HttpHeaderKey {
  TRANSFER_ENCODING("Transfer-Encoding"),
  CONTENT_LENGTH("Content-Length"),
  CONTENT_TYPE("Content-Type"),
  TRAILER("Trailer"),
  RANGE("Range"),
  CONTENT_RANGE("Content-Range"),
  ACCEPT_RANGES("Accept-Ranges");

  private final String value;

  HttpHeaderKey(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
