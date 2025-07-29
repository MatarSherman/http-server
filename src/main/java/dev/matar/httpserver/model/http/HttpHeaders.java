package dev.matar.httpserver.model.http;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class HttpHeaders implements Iterable<Map.Entry<String, List<String>>> {
  private final Map<String, List<String>> map;

  public HttpHeaders() {
    this.map = new HashMap<>();
  }

  public HttpHeaders(Map<String, List<String>> headers) {
    this();
    headers.forEach((key, values) -> this.map.put(key.toLowerCase(), new ArrayList<>(values)));
  }

  public HttpHeaders(HttpHeaders headers) {
    this();
    headers
        .getInternalMap()
        .forEach((key, values) -> this.map.put(key.toLowerCase(), new ArrayList<>(values)));
  }

  private Map<String, List<String>> getInternalMap() {
    return this.map;
  }

  public Optional<List<String>> get(String key) {
    List<String> values = this.map.get(key.toLowerCase());
    if (values == null) {
      return Optional.empty();
    }
    return Optional.of(new ArrayList<>(values));
  }

  public Optional<String> getFirst(String key) {
    Optional<List<String>> values = get(key.toLowerCase());
    return values.map(List::getFirst);
  }

  public void add(String key, String value) {
    this.map.computeIfAbsent(key.toLowerCase(), _ -> new ArrayList<>()).add(value);
  }

  public boolean containsKey(String key) {
    return this.map.containsKey(key.toLowerCase());
  }

  public void remove(String key) {
    this.map.remove(key.toLowerCase());
  }

  public Stream<Map.Entry<String, List<String>>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  @Override
  public String toString() {
    return "HttpHeaders{" + map + '}';
  }

  @Override
  public Iterator<Map.Entry<String, List<String>>> iterator() {
    return this.map.entrySet().iterator();
  }
}
