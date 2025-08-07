package dev.matar.httpserver.exception;

import dev.matar.httpserver.model.http.HttpStatus;

public class HttpRequestSizeLimitException extends Exception {
  public enum Section {
    BODY,
    FIRST_LINE,
    HEADERS
  }

  private final Section oversizedSection;

  public HttpRequestSizeLimitException(String message, Section oversizedSection) {
    super(message);
    this.oversizedSection = oversizedSection;
  }

  public int getHttpStatus() {
    return switch (oversizedSection) {
      case FIRST_LINE -> HttpStatus.URI_TOO_LONG.code();
      case HEADERS -> HttpStatus.HEADERS_TOO_LARGE.code();
      case BODY -> HttpStatus.CONTENT_TOO_LARGE.code();
    };
  }
}
