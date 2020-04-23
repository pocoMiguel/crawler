package org.mkm.crawler.scraper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

import org.jsoup.select.Elements;
import org.junit.Test;

public class HTMLScraperTest {
  @Test
  public void scrapesCorrectLinks() {
    Elements elements = HTMLScraper.scrape("<a href=\"1\">1</a>  <a href=\"2\">2</a>  <a>3</a>");

    assertThat(elements, notNullValue());
    assertThat(elements.size(), is(2));
  }

  @Test
  public void returnsNothingIfNoLinks() {
    Elements elements = HTMLScraper.scrape("<p>Some text</p>");

    assertThat(elements, notNullValue());
    assertThat(elements.size(), is(0));
  }

  @Test
  public void returnsNothingIfLinkIsNotValid() {
    Elements elements = HTMLScraper.scrape("<a>What is this</a>");

    assertThat(elements, notNullValue());
    assertThat(elements.size(), is(0));
  }
}