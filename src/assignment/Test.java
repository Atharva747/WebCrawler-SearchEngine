package assignment;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/*
The testing web used for this test is given below. It consists of an index and 5 files.
Each line contains the name of a separate HTML file, followed by a set of words and a phrase in that file.
This is followed by a set of numbers indicating that the page is linked to those page numbers.


Index: paint bucket shampoo dishes "this is the first paragraph" 1, 2, 3, 4, 5
1.html electrical engineering test custom "this is the first paragraph" 2
2.html Bane been bone boon "this is the first paragraph" 3, 4
3.html bone boon dishes maybe "this is the first paragraph" 1, 4, 5
4.html And if not they bucket "this is the first paragraph" 2, 3
5.html If paint this bucket then I say "this is the first paragraph" 4, 4, 3, 5, 5, 5, 5
*/
public class Test {

	WebIndex w;
	WebQueryEngine wqe;

	@org.junit.Test
	public void test() throws Exception {
		w = (WebIndex) Index.load("index.db");
		getClass();
		testIndex();

		// Construct a webQueryEngine using that WebIndex
		wqe = WebQueryEngine.fromIndex(w);
		testTokens();
		testPostfix();
		testResults();
	}

	public void testIndex() throws MalformedURLException {
		// This method checks if the index was formed as expected.
		// It checks the correctness of the WebCrawler and WebQueryEngine.

		// check if all URLs have been crawled.
		assertEquals(6, w.allURLs.size());

		HashSet<URL> hSet = new HashSet<>();
		URL url = new URL("file://localhost/Users/atharva/Downloads/prog7/testwebsite/testfiles/1.html");
		hSet.add(url);

		// only 1.html contains the word "electrical" and the word "custom"
		assertTrue(w.h1.get("electrical").keySet().equals(hSet));
		assertTrue(w.h1.get("custom").keySet().equals(hSet));
		// negative test
		assertFalse(w.h1.get("paint").keySet().equals(hSet));

		// Checking if the number of connections are stored properly.
		HashMap<URL, Integer> connections = new HashMap<>();
		connections.put(url, 2);
		url = new URL("file://localhost/Users/atharva/Downloads/prog7/testwebsite/testfiles/2.html");
		connections.put(url, 3);
		url = new URL("file://localhost/Users/atharva/Downloads/prog7/testwebsite/testfiles/3.html");
		connections.put(url, 4);
		url = new URL("file://localhost/Users/atharva/Downloads/prog7/testwebsite/testfiles/4.html");
		connections.put(url, 5);
		url = new URL("file://localhost/Users/atharva/Downloads/prog7/testwebsite/testfiles/5.html");
		connections.put(url, 6);
		url = new URL("file://localhost/Users/atharva/Downloads/prog7/testwebsite/index.html");
		connections.put(url, 0);

		assertTrue(connections.equals(w.connections));

		// Check if the strings are stored correctly for phrase queries
		assertTrue(w.h3.get(url).contains("hello"));
		assertTrue(w.h3.get(url).contains("this is the first paragraph"));
		// negative test
		assertFalse(w.h3.get(url).contains("electrical engineering"));
	}

	public void testTokens() {
		// Check if tokens are outputted in the correct order.
		LinkedList<String> currentquery = new LinkedList<String>();
		currentquery.add("(");
		currentquery.add("gear");
		currentquery.add(")");
		currentquery.add("|");
		currentquery.add("top");
		assertEquals(currentquery, wqe.getToken("(gear)|   top   "));

		currentquery = new LinkedList<String>();
		currentquery.add("ping");
		currentquery.add("|");
		currentquery.add("(");
		currentquery.add("pong");
		currentquery.add("&");
		currentquery.add("hat");
		currentquery.add(")");
		currentquery.add("|");
		currentquery.add("maybe");
		assertEquals(currentquery, wqe.getToken("ping|(pong & hat)|maybe"));
	}

	public void testPostfix() {
		// This method checks if the post fix notation is found correctly.
		LinkedList<String> currentquery = new LinkedList<String>();
		// Postfix for this query was found manually
		currentquery.add("ping");
		currentquery.add("pong");
		currentquery.add("hat");
		currentquery.add("&");
		currentquery.add("maybe");
		currentquery.add("|");
		currentquery.add("|");
		assertEquals(currentquery, wqe.shunting("ping|(pong & hat)|maybe"));
	}

	public void testResults() {
		Collection<Page> results = wqe.query("dishes");
		assertEquals(2, results.size());

		results = wqe.query("\"this is the first paragraph\"");
		assertEquals(6, results.size());

		// Check if the results are same for different queries that mean the
		// same.
		assertEquals(wqe.query("dishes|paint").size(), wqe.query("paint|dishes").size());

		LinkedList<Page> currentquery = (LinkedList<Page>) wqe.query("dishes|paint");
		HashSet<String> hSet = new HashSet<>();
		for (Page page : currentquery) {
			hSet.add(page.getURL().toString());
		}
		LinkedList<Page> currentquery2 = (LinkedList<Page>) wqe.query("paint|dishes");
		for (Page page : currentquery2) {
			assertTrue(hSet.contains(page.getURL().toString()));
		}

		assertEquals(wqe.query("dishes|paint").size(), wqe.query("paint|dishes").size());

		currentquery = (LinkedList<Page>) wqe.query("  (electrical & engineering)| boon & bane ");
		// System.out.println(currentquery.size());
		hSet = new HashSet<>();
		for (Page page : currentquery) {
			hSet.add(page.getURL().toString());
			// System.out.println(page.getURL().toString());
		}
		currentquery2 = (LinkedList<Page>) wqe.query("(BANe&boOn)|engineering&electrical");
		// System.out.println(currentquery2.size());
		for (Page page : currentquery2) {
			assertTrue(hSet.contains(page.getURL().toString()));
			// System.out.println(page.getURL().toString());
		}

		assertEquals(2, wqe.query("boon \"this is the first paragraph\"").size());
	}

}
