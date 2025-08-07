package dev.matar.httpserver.model.http;

import java.util.Arrays;

public class HttpRequest {
  private HttpMethod method;
  private String path;
  private String version;
  private final HttpHeaders headers;
  private final HttpHeaders trailingHeaders;
  private byte[] body;

  public HttpRequest(HttpMethod method, String path, String version, byte[] body) {
    this.method = method;
    this.path = path;
    this.version = version;
    this.headers = new HttpHeaders();
    this.trailingHeaders = new HttpHeaders();
    this.body = body;
  }

  private HttpRequest(Builder builder) {
    this.method = builder.method;
    this.path = builder.path;
    this.version = builder.version;
    this.headers = new HttpHeaders(builder.headers);
    this.body = builder.body != null ? Arrays.copyOf(builder.body, builder.body.length) : null;
    this.trailingHeaders = new HttpHeaders(builder.trailingHeaders);
  }

  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  public byte[] getBody() {
    return body;
  }

  public void setBody(byte[] body) {
    this.body = body;
  }

  public HttpHeaders getTrailingHeaders() {
    return trailingHeaders;
  }

  @Override
  public String toString() {
    return "HttpRequest{"
        + "method="
        + method
        + ", path='"
        + path
        + '\''
        + ", version='"
        + version
        + '\''
        + ", headers="
        + headers
        + ", body="
        + Arrays.toString(body)
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private HttpMethod method;
    private String path;
    private String version;
    private HttpHeaders headers;
    private HttpHeaders trailingHeaders;
    private byte[] body;

    private Builder() {
      this.headers = new HttpHeaders();
      this.trailingHeaders = new HttpHeaders();
    }

    public Builder method(HttpMethod method) {
      this.method = method;
      return this;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder addHeader(String key, String value) {
      this.headers.add(key, value);
      return this;
    }

    public Builder addTrailingHeader(String key, String value) {
      this.trailingHeaders.add(key, value);
      return this;
    }

    public Builder body(byte[] body) {
      this.body = body;
      return this;
    }

    public HttpRequest build() {
      return new HttpRequest(this);
    }
  }
}
