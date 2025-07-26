package dev.matar.httpserver.exception;

public class InvalidHttpRequestException extends Exception {
  public InvalidHttpRequestException(String message) {
    super(message);
  }

  public InvalidHttpRequestException(Throwable cause) {
    super(cause);
  }

  public InvalidHttpRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
