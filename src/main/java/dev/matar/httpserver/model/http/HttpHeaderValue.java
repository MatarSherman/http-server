package dev.matar.httpserver.model.http;

public enum HttpHeaderValue {
  TRANSFER_ENC_CHUNKED("chunked");

  private final String value;

  HttpHeaderValue(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}
