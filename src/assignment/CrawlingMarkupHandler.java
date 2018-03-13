package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

import com.sun.org.apache.xml.internal.utils.URI;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses
 * the input; responsible for building the actual web index.
 *
 * TODO: Implement this!
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {

	private StringBuilder stringBuilder; // StringBuilder object that stores all
											// the text data found on this page.
	private WebIndex index;
	HashSet<String> visited; // set of all visited URLs
	URL currenturl; // current URL that is being searched
	LinkedList<URL> newURLSonthispage = new LinkedList<URL>();// This holds all
																// URLs on this
																// page

	public CrawlingMarkupHandler() {
		visited = new HashSet<String>();
		index = new WebIndex();
	}

	/**
	 * This method returns the complete index that has been crawled thus far
	 * when called.
	 */
	public Index getIndex() {
		return index;
	}

	/**
	 * This method returns any new URLs found to the Crawler; upon being called,
	 * the set of new URLs should be cleared.
	 */
	public List<URL> newURLs() {
		LinkedList<URL> temp = newURLSonthispage;
		newURLSonthispage = new LinkedList<URL>();
		return temp;
	}

	/**
	 * Called when the parser first starts reading a document.
	 * 
	 * @param startTimeNanos
	 *            the current time (in nanoseconds) when parsing starts
	 * @param line
	 *            the line of the document where parsing starts
	 * @param col
	 *            the column of the document where parsing starts
	 */
	public void handleDocumentStart(long startTimeNanos, int line, int col) {
		stringBuilder = new StringBuilder();
		if (!index.connections.containsKey(currenturl)) {
			index.connections.put(currenturl, 0);
		}
	}

	/**
	 * Called when the parser finishes reading a document.
	 * 
	 * @param endTimeNanos
	 *            the current time (in nanoseconds) when parsing ends
	 * @param totalTimeNanos
	 *            the difference between current times at the start and end of
	 *            parsing
	 * @param line
	 *            the line of the document where parsing ends
	 * @param col
	 *            the column of the document where the parsing ends
	 */
	public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {
		String temp = stringBuilder.toString().trim().toLowerCase();
		index.allURLs.add(currenturl);
		index.inserth1("UNIVERSALSET", currenturl);
		index.inserth3(currenturl, temp);
		String[] words = temp.split("\\W+");
		for (String x : words) {
			// Stem the words before adding them to the hashMap
			// Remove commenting to enable stemming.
			// Stemmer stemmer = new Stemmer();
			// index.inserth1(stemmer.stemword(x), currenturl);
			index.inserth1(x, currenturl);
		}
	}

	/**
	 * Called at the start of any tag.
	 * 
	 * @param elementName
	 *            the element name (such as "div")
	 * @param attributes
	 *            the element attributes map, or null if it has no attributes
	 * @param line
	 *            the line in the document where this elements appears
	 * @param col
	 *            the column in the document where this element appears
	 */
	public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
		if (elementName.toLowerCase().equals("a")) {
			for (String x : attributes.keySet()) {
				if (x.toLowerCase().equals("href")) {
					URL temp = null;
					try {
						String temp2 = attributes.get(x);
						temp = new URL(currenturl, temp2);
						index.addurl(temp);
						if (!visited.contains(temp)) {
							newURLSonthispage.add(temp);
							break;
						}
					} catch (MalformedURLException e) {
						// Didn't add a print statement because it slows down
						// WebCrawler if there are too many errors.
					}
				}
			}
		}
	}

	/**
	 * Called at the end of any tag.
	 * 
	 * @param elementName
	 *            the element name (such as "div").
	 * @param line
	 *            the line in the document where this elements appears.
	 * @param col
	 *            the column in the document where this element appears.
	 */
	public void handleCloseElement(String elementName, int line, int col) {
	}

	/**
	 * Called whenever characters are found inside a tag. Note that the parser
	 * is not required to return all characters in the tag in a single chunk.
	 * Whitespace is also returned as characters.
	 * 
	 * @param ch
	 *            buffer containing characters; do not modify this buffer
	 * @param start
	 *            location of 1st character in ch
	 * @param length
	 *            number of characters in ch
	 */
	public void handleText(char ch[], int start, int length, int line, int col) {
		for (int i = start; i < start + length; i++) {
			if (Character.isWhitespace(ch[i])) {
				stringBuilder.append(" ");
			} else {
				stringBuilder.append(ch[i]);
			}
		}
		// Arbitrary characters appended to the end of each tag.
		stringBuilder.append(" xk7fs ");
	}
}
