package dev.matar.httpserver.model.http;

public record HttpHeader(String key, String value) {
  public static final String KEY_VALUE_SEPARATOR = ": ";
  public static final String MULTI_VALUE_SEPARATOR = ", ";
  public static final String SUB_PARAM_SEPARATOR = "; ";

  public String toRaw() {
    return key + KEY_VALUE_SEPARATOR + value;
  }

  public static HttpHeader contentType(MimeType mimeType) {
    return new HttpHeader(HttpHeaderKey.CONTENT_TYPE.value(), String.valueOf(mimeType.value()));
  }

  public static HttpHeader contentType(String mimeType) {
    return new HttpHeader(HttpHeaderKey.CONTENT_TYPE.value(), String.valueOf(mimeType));
  }

  public static HttpHeader contentLength(int length) {
    return new HttpHeader(HttpHeaderKey.CONTENT_LENGTH.value(), String.valueOf(length));
  }
}
