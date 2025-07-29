package dev.matar.httpserver.model.http;

public class HttpResponse<T> {
  private int status;
  private String message;
  private final HttpHeaders headers;
  private T body;

  public HttpResponse(int status, String message) {
    this.status = status;
    this.message = message;
    this.headers = new HttpHeaders();
  }

  public HttpResponse(int status, String message, T body) {
    this(status, message);
    this.body = body;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }
}
