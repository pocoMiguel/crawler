Simple crawler
==============

# Dependencies
- Gradle
- Java 1.8

# Using it with your IDE
This code base is developed using IntelliJ with Lombok plugin installed. Annotation processing has to be activated to work well with the code base. This does not mean that other IDEs can't be used but it is preferred that your IDE can see the generated functions/classes.

# Getting started
- Run gradle build to build the app
- Run gradle run --args='http://www.google.com' to start the crawler
- An output file can be found in output/output.json

## How it works

- A number of workers (crawlers) is created
- A queue is maintained for the crawlers to visit
- Workers act as both producers and consumers (taking and submitting to the queue)
- The workers will crawl and wait for a URL until there's no more URLs to crawl
- The workers will terminate itself once it waits for more than the idle time and there's no more URL to crawl

## Credits

Author: Michal Majewski