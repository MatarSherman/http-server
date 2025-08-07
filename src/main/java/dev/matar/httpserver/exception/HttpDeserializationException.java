package dev.matar.httpserver.exception;

public class HttpDeserializationException extends Exception {
  public HttpDeserializationException(String message) {
    super(message);
  }

  public HttpDeserializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
