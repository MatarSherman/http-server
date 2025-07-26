package dev.matar.httpserver.exception;

public class ReadLimitException extends Exception {
  public ReadLimitException() {}

  public ReadLimitException(String message) {
    super(message);
  }

  public ReadLimitException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReadLimitException(Throwable cause) {
    super(cause);
  }
}
