package assignment;

import java.io.*;
import java.net.*;
import java.util.*;

import org.attoparser.simple.*;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;

/**
 * The entry-point for WebCrawler; takes in a list of URLs to start crawling
 * from and saves an index to index.db.
 */
public class WebCrawler {

	/**
	 * The WebCrawler's main method starts crawling a set of pages. You can
	 * change this method as you see fit, as long as it takes URLs as inputs and
	 * saves an Index at "index.db".
	 */
	public static void main(String[] args) {

		// Basic usage information
		if (args.length == 0) {
			System.err.println("Error: No URLs specified.");
			System.exit(1);
		}

		// We'll throw all of the args into a queue for processing.
		Queue<URL> remaining = new LinkedList<>();
		for (String url : args) {
			try {
				remaining.add(new URL(url));
			} catch (MalformedURLException e) {
				// Throw this one out!
				System.err.printf("Error: URL '%s' was malformed and will be ignored!%n", url);
			}
		}

		// Create a parser from the attoparser library, and our handler for
		// markup.
		ISimpleMarkupParser parser = new SimpleMarkupParser(ParseConfiguration.htmlConfiguration());
		CrawlingMarkupHandler handler = new CrawlingMarkupHandler();

		// Try to start crawling, adding new URLS as we see them.
		try {
			while (!remaining.isEmpty()) {
				// Parse the next URL's page
				URL currenturl = remaining.poll();
				handler.currenturl = currenturl;
				String temp1 = currenturl.toString().split("#")[0];
				if (temp1.contains("?")) {
					int y = temp1.lastIndexOf('?');
					temp1 = temp1.substring(0, y);
				}
				if (!handler.visited.contains(temp1)) {
					try {
						handler.visited.add(temp1);
						parser.parse(new InputStreamReader(currenturl.openStream()), handler);
						// Add any new URLs
						remaining.addAll(handler.newURLs());
					} catch (ParseException e) {
						System.err.println("Parse Exception for " + currenturl.toString());
					} catch (FileNotFoundException e) {
						System.err.println("The URL " + currenturl.toString() + " doesn't exist.");
					} catch (UnknownServiceException e) {
					}
				}
			}
			WebIndex w = (WebIndex) handler.getIndex();
			handler.getIndex().save("index.db");
			System.out.println("Pages crawled:"+w.allURLs.size());
		} catch (Exception e) {
			// Bad exception handling :(
			System.err.println("Error: Index generation failed!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
