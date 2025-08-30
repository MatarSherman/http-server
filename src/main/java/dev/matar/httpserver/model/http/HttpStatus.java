package dev.matar.httpserver.model.http;

public enum HttpStatus {
  OK(200),
  PARTIAL_CONTENT(206),
  NOT_MODIFIED(304),
  BAD_REQUEST(400),
  NOT_FOUND(404),
  CONTENT_TOO_LARGE(413),
  URI_TOO_LONG(414),
  RANGE_NOT_SATISFIABLE(416),
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
