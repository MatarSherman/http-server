package dev.matar.httpserver.model.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HttpResponse<T> {
  private int status;
  private String message;
  private List<HttpHeader> headers;
  private T body;

  public HttpResponse(int status, String message, T body) {
    this.status = status;
    this.message = message;
    this.headers = new ArrayList<>();
    this.body = body;
  }

  public HttpResponse(int status, String message) {
    this.status = status;
    this.message = message;
    this.headers = new ArrayList<>();
  }

  public boolean hasHeader(String key) {
    return this.headers.stream().anyMatch(header -> header.key().equalsIgnoreCase(key));
  }

  public Optional<HttpHeader> getHeader(String key) {
    return this.headers.stream().filter(header -> header.key().equalsIgnoreCase(key)).findFirst();
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

  public List<HttpHeader> getHeaders() {
    return headers;
  }

  public void setHeaders(List<HttpHeader> headers) {
    this.headers = headers;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }
}
