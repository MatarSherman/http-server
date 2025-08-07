package dev.matar.httpserver.server;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaders;
import dev.matar.httpserver.model.http.HttpResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ParsedHttpResponse {
  private final String firstLine;
  private final HttpHeaders headers;
  private final String body;

  private ParsedHttpResponse(String firstLine, HttpHeaders headers, String body) {
    this.firstLine = firstLine;
    this.headers = headers;
    this.body = body;
  }

  public static ParsedHttpResponse parse(byte[] rawResponse) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawResponse)));

    String firstLine = reader.readLine();
    HttpHeaders headers = new HttpHeaders();

    String headerLine;
    while (!(headerLine = reader.readLine()).isEmpty()) {
      String[] keyValue = headerLine.split(HttpHeader.KEY_VALUE_SEPARATOR);
      headers.add(keyValue[0], keyValue[1]);
    }

    String stringResponse = new String(rawResponse, Constants.DEFAULT_CHARSET);
    String responseBodySeparator = Constants.HTTP_CRLF.repeat(2);
    String body =
        stringResponse.substring(
            stringResponse.indexOf(responseBodySeparator) + responseBodySeparator.length());

    return new ParsedHttpResponse(firstLine, headers, body.isEmpty() ? null : body);
  }

  public static ParsedHttpResponse from(HttpResponse<?> response, String body) {
    String firstLine =
        String.join(
            " ",
            Constants.HTTP_VERSION,
            String.valueOf(response.getStatus()),
            response.getMessage());

    return new ParsedHttpResponse(firstLine, response.getHeaders(), body);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ParsedHttpResponse that = (ParsedHttpResponse) o;
    return Objects.equals(firstLine, that.firstLine)
        && Objects.equals(headers, that.headers)
        && Objects.equals(body, that.body);
  }

  @Override
  public String toString() {
    return "ParsedHttpResponse{"
        + "firstLine='"
        + firstLine
        + '\''
        + ", headers="
        + headers
        + ", body='"
        + body
        + '\''
        + '}';
  }
}
