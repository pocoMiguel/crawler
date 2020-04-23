package org.mkm.crawler.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public interface HTMLScraper {

  static Elements scrape(String HTML) {
    Document document = Jsoup.parse(HTML);

    return document.select("a[href]");
  }
}
