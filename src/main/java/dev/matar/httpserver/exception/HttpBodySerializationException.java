package dev.matar.httpserver.exception;

public class HttpBodySerializationException extends Exception {
  public HttpBodySerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public HttpBodySerializationException(String message) {
    super(message);
  }
}
