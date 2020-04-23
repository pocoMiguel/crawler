package org.mkm.crawler.model;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

public final class PageMap {

  @Getter
  private final Map<String, Page> pages = new ConcurrentHashMap<>();

  public void addPage(URL url, List<String> links) {
    pages.put(url.getPath(), new Page(url, links));
  }
}
