package dev.matar.httpserver.exception;

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
      case FIRST_LINE -> 414;
      case HEADERS -> 431;
      case BODY -> 413;
    };
  }
}
