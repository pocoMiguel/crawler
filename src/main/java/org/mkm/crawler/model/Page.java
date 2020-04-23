package org.mkm.crawler.model;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public final class Page {

  private final URL url;

  private final Set<String> pageLinks = ConcurrentHashMap.newKeySet();

  public Page(URL url, List<String> links) {
    this.url = url;
    pageLinks.addAll(links);
  }
}
