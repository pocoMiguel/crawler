package org.mkm.crawler.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mkm.crawler.model.PageMap;
import org.mkm.crawler.scraper.HTMLScraper;

@Slf4j
@Builder
public final class Crawler implements Runnable{

  private final Client client;

  private final BlockingQueue<URL> urlsToVisit;

  private final Set<URL> synchronizedVisitedURLs;

  private final PageMap pageMap;

  private final URL rootURL;

  private final int idleTime;

  @Override
  public void run() {
    URL currentURL = null;
    do {
      try {
        currentURL = urlsToVisit.poll(idleTime, TimeUnit.SECONDS);

        //stop crawling if there is no url to crawl
        if (currentURL == null) break;

        if (!synchronizedVisitedURLs.contains(currentURL)) {
          synchronizedVisitedURLs.add(currentURL);
          crawl(currentURL);
        }

      } catch (InterruptedException e) {
        if (urlsToVisit.isEmpty()) break;
      }
    } while (currentURL != null);
  }

  private void crawl(URL url) {
    log.info("Crawling {}", url);
    Predicate<URL> isInternalDomain = r -> r.getHost().equalsIgnoreCase(rootURL.getHost());

    try {
      String HTML = client
          .target(url.toString())
          .request(MediaType.TEXT_HTML)
          .get(String.class);

      Elements links = HTMLScraper.scrape(HTML);

      List<URL> validLinksOnPage = links.stream()
          .map(this::toURL)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());

      List<String> internalPageLinks = validLinksOnPage.stream()
          .filter(isInternalDomain)
          .map(URL::getPath)
          .collect(Collectors.toList());

      pageMap.addPage(url, internalPageLinks);

      validLinksOnPage.stream()
          .filter(isInternalDomain)
          .forEach(this::addToQueueIfNotVisited);

    } catch (WebApplicationException e) {
      log.debug("Unable to reach URL={}", url, e);
    }
  }

  private Optional<URL> toURL(Element element) {
    String href = element.attr("href");

    try {
      return Optional.of(new URL(rootURL, href));
    } catch (MalformedURLException e) {
      log.warn("Unable to form URL with rootURL={}, path={}", rootURL, href, e);
    }

    return Optional.empty();
  }

  private void addToQueueIfNotVisited(URL url) {
    boolean isResourceVisited = synchronizedVisitedURLs.contains(url);
    boolean isResourceInQueue = urlsToVisit.contains(url);
    if (!isResourceVisited && !isResourceInQueue) {
      urlsToVisit.add(url);
    }
  }
}
