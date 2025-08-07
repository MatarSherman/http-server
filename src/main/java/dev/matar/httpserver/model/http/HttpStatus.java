package dev.matar.httpserver.model.http;

public enum HttpStatus {
  NOT_MODIFIED(304),
  BAD_REQUEST(400),
  NOT_FOUND(404),
  CONTENT_TOO_LARGE(413),
  URI_TOO_LONG(414),
  HEADERS_TOO_LARGE(431),
  INTERNAL_ERROR(500);

  private final int code;

  HttpStatus(int code) {
    this.code = code;
  }

  public int code() {
    return this.code;
  }
}
