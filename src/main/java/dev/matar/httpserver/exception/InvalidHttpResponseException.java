package dev.matar.httpserver.exception;

public class InvalidHttpResponseException extends Exception {
  public InvalidHttpResponseException(String message) {
    super(message);
  }

  public InvalidHttpResponseException(Throwable cause) {
    super(cause);
  }

  public InvalidHttpResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
