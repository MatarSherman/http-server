package dev.matar.httpserver.exception;

import dev.matar.httpserver.model.http.HttpStatus;

public class HttpConnectionHandlingException extends Exception {
  private final int status;
  private final String statusMessage;

  public HttpConnectionHandlingException(int status, String statusMessage) {
    this.status = status;
    this.statusMessage = statusMessage;
  }

  public HttpConnectionHandlingException(HttpStatus status, String statusMessage) {
    this(status.code(), statusMessage);
  }

  public HttpConnectionHandlingException(int status, String statusMessage, Throwable cause) {
    super(cause);
    this.status = status;
    this.statusMessage = statusMessage;
  }

  public HttpConnectionHandlingException(HttpStatus status, String statusMessage, Throwable cause) {
    this(status.code(), statusMessage, cause);
  }

  public int getStatus() {
    return status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }
}
