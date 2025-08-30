package dev.matar.httpserver.model.http;

public record HttpHeader(String key, String value) {
  public static final String KEY_VALUE_SEPARATOR = ": ";
  public static final String MULTI_VALUE_SEPARATOR = ", ";
  public static final String SUB_PARAM_SEPARATOR = "; ";
  public static final String CHARSET_PARAM_INDICATOR = "charset=";
  public static final String CONTENT_RANGE_BYTES_PARAM = "bytes ";
  public static final String CONTENT_RANGE_TOTAL_SIZE_INDICATOR = "/";
  public static final String RANGE_BYTES_PARAM = "bytes=";
  public static final String RANGE_BOUNDS_SEPARATOR = "-";
  public static final String CONTENT_RANGE_WILDCARD = "*";

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
