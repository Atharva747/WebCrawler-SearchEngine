package assignment;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A web-index which efficiently stores information about pages. Serialization
 * is done automatically via the superclass "Index" and Java's Serializable
 * interface.
 *
 * TODO: Implement this!
 */
public class WebIndex extends Index {
	/**
	 * Needed for Serialization (provided by Index) - don't remove this!
	 */
	private static final long serialVersionUID = 1L;

	HashMap<String, HashMap<URL, Integer>> h1;
	HashMap<URL, String> h3;
	HashSet<URL> allURLs;
	HashMap<URL, Integer> connections;

	// TODO: Implement all of this! You may choose your own data structures an
	// internal APIs.
	// You should not need to worry about serialization (just make any other
	// data structures you use
	// here also serializable - the Java standard library data structures
	// already are, for example).

	public WebIndex() {
		h1 = new HashMap<String, HashMap<URL, Integer>>();
		h3 = new HashMap<>();
		allURLs = new HashSet<>();
		connections = new HashMap<>();
	}

	public void inserth1(String s, URL u) {
		if (!h1.containsKey(s)) {
			HashMap<URL, Integer> pages = new HashMap<URL, Integer>();
			pages.put(u, 1);
			h1.put(s, pages);
		} else {
			HashMap<URL, Integer> pages = h1.get(s);
			if (pages.containsKey(u)) {
				int ttt = pages.get(u);
				ttt++;
				pages.put(u, ttt);
			} else {
				pages.put(u, 1);
			}
			h1.put(s, pages);
		}
	}

	public void addurl(URL u) {
		if (connections.containsKey(u)) {
			int temp = connections.get(u);
			temp++;
			connections.put(u, temp);
		} else {
			connections.put(u, 1);
		}
	}

	public void inserth3(URL u, String words) {
		h3.put(u, words);
	}
}
