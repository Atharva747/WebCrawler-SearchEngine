package assignment;

import java.net.URL;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual
 * queries with a collection of relevant pages.
 *
 * TODO: Implement this!
 */
public class WebQueryEngine {

	public WebIndex myIndex;
	// this HashMap is used for storing operator precedence.
	public HashMap<String, Integer> mapping = new HashMap<String, Integer>();

	/**
	 * Returns a WebQueryEngine that uses the given Index to construct answers
	 * to queries.
	 *
	 * @param index
	 *            The WebIndex this WebQueryEngine should use.
	 * @return A WebQueryEngine ready to be queried.
	 */
	public static WebQueryEngine fromIndex(WebIndex index) {
		WebQueryEngine w = new WebQueryEngine();
		w.myIndex = index;
		w.mapping.put("(", 0);
		w.mapping.put("|", 1);
		w.mapping.put("&", 2);
		w.mapping.put("!", 3);
		return w;
	}

	/**
	 * Returns a Collection of URLs (as Strings) of web pages satisfying the
	 * query expression.
	 *
	 * @param query
	 *            A query expression.
	 * @return A collection of web pages satisfying the query.
	 */

	public Collection<Page> query(String query) {
		query = query.trim().toLowerCase();
		LinkedList<String> queries = initialparse(query);
		HashSet<URL> alltheseURLs = new HashSet<>(myIndex.allURLs);
		for (String q : queries) {
			HashSet<URL> temp = getURLs(q);
			alltheseURLs = and(alltheseURLs, temp);
		}
		LinkedList<Page> pages = new LinkedList<Page>();
		for (URL u : alltheseURLs) {
			Page page = new Page(u, myIndex.connections.get(u));
			pages.add(page);
		}
		pages.sort(new compareByConnections());
		return pages;
	}

	public LinkedList<String> getToken(String query) {
		// This method parses the query to get a separate list of tokens used
		// for shunting yard.
		LinkedList<String> currentquery = new LinkedList<String>();
		int i = 0;
		while (i < query.length()) {
			if (query.charAt(i) == '(') {
				currentquery.add("(");
				i++;
			} else if (query.charAt(i) == '&') {
				currentquery.add("&");
				i++;
			} else if (query.charAt(i) == '|') {
				currentquery.add("|");
				i++;
			} else if (query.charAt(i) == ')') {
				currentquery.add(")");
				i++;
			} else if (query.charAt(i) == '!') {
				currentquery.add("UNIVERSALSET");
				currentquery.add("!");
				i++;
			} else if (query.charAt(i) == '"') {
				StringBuilder temp = new StringBuilder("");
				while (query.charAt(i + 1) != '"') {
					temp.append(query.charAt(i + 1));
					i++;
				}
				i += 2;
				String temp2 = temp.toString().trim();
				handlephrase(temp2);
				currentquery.add(temp2);
			} else {
				StringBuilder temp = new StringBuilder("");
				while (i != query.length() && !(query.charAt(i) == '&' || query.charAt(i) == '|'
						|| query.charAt(i) == '(' || query.charAt(i) == ')' || query.charAt(i) == '!')) {
					temp = temp.append(query.charAt(i));
					i++;
				}
				String temp2 = temp.toString().trim();
				// handleword(temp2);

				// To enable stemming, remove commenting for the following 2
				// statements.
				// Stemmer stemmer = new Stemmer();
				// temp2 = stemmer.stemword(temp2);
				currentquery.add(temp2);
			}
		}
		return currentquery;
	}

	public LinkedList<String> shunting(String query) {
		// This method transforms the list of tokens to postfix notation.
		LinkedList<String> currentquery = getToken(query);
		LinkedList<String> output = new LinkedList<String>();
		Stack<String> opq = new Stack<String>();
		try {
			while (!currentquery.isEmpty()) {
				String currenttoken = currentquery.poll();
				if (currenttoken.equals("|") || currenttoken.equals("&") || currenttoken.equals("!")) {
					if (opq.isEmpty()) {
						opq.push(currenttoken);
					} else {
						while (opq.size() > 0 && mapping.get(currenttoken) < mapping.get(opq.peek())) {
							output.add(opq.pop());
						}
						opq.push(currenttoken);
					}
				} else if (currenttoken.equals("(")) {
					opq.push(currenttoken);
				} else if (currenttoken.equals(")")) {
					while (!opq.isEmpty() && !opq.peek().equals("(")) {
						output.add(opq.pop());
					}
					if (!opq.isEmpty() && opq.peek().equals("(")) {
						opq.pop();
					}
				} else {
					output.add(currenttoken);
				}
			}

			while (!opq.isEmpty()) {
				output.add(opq.pop());
			}
		} catch (EmptyStackException e) {
			// Empty stack exception indicates something is wrong with the query
			System.err.println("INVALID QUERY");
		}
		return output;
	}

	public HashSet<URL> getURLs(String query) {
		// This method uses a custom class called "custom" that stores the
		// hashsets and strings forms of tokens
		LinkedList<String> polishquery = shunting(query);
		LinkedList<custom> customList = new LinkedList<>();
		Stack<custom> lol = new Stack<>();
		for (String t : polishquery) {
			custom temp = new custom(t);
			customList.add(temp);
		}

		try {
			for (custom k : customList) {
				if (k.tp.equals("&")) {
					// Pop the previous 2 tokens and perform the AND operation
					custom a = lol.pop();
					custom b = lol.pop();
					custom c = new custom(and(a.hSet, b.hSet));
					lol.push(c);
				} else if (k.tp.equals("|")) {
					// Pop the previous 2 tokens and perform the OR operation
					custom a = lol.pop();
					custom b = lol.pop();
					custom c = new custom(or(a.hSet, b.hSet));
					lol.push(c);
				} else if (k.tp.equals("!")) {
					// Pop the previous 2 tokens and perform the NOT operation
					custom a = lol.pop();
					custom b = lol.pop();
					custom c = new custom(not(a.hSet, b.hSet));
					lol.push(c);
				} else {
					lol.push(k);
				}
			}

			if (lol.size() > 1) {
				System.err.println("INVALID QUERY");
			} else {
				HashSet<URL> alltheseURLs = new HashSet<>(lol.pop().hSet);
				return alltheseURLs;
			}
		} catch (EmptyStackException e) {
			System.err.println("INVALID QUERY");
		}
		return new HashSet<URL>();
	}

	public HashSet<URL> and(HashSet<URL> firstset, HashSet<URL> secondset) {
		// This method uses the retainAll method for AND operation
		HashSet<URL> hSet = new HashSet<URL>(firstset);
		hSet.retainAll(secondset);
		return hSet;
	}

	public HashSet<URL> or(HashSet<URL> firstset, HashSet<URL> secondset) {
		// This method uses the addAll method for the OR operation
		HashSet<URL> hSet = new HashSet<URL>(firstset);
		hSet.addAll(secondset);
		return hSet;
	}

	public HashSet<URL> not(HashSet<URL> firstset, HashSet<URL> secondset) {
		// This method uses the RemoveAll method to perform the NOT operation.
		if (firstset.size() > secondset.size()) {
			HashSet<URL> hSet = new HashSet<URL>(firstset);
			hSet.removeAll(secondset);
			return hSet;
		} else {
			HashSet<URL> hSet = new HashSet<URL>(secondset);
			hSet.removeAll(firstset);
			return hSet;
		}
	}

	public void handlephrase(String phrase) {
		// This method is called whenever a phrase in encountered in the initial
		// parse
		String words[] = phrase.split("\\W+");
		if (words.length == 0) {
			System.err.println("Invalid query: Empty phrase");
			return;
		}

		HashSet<URL> tempset = new HashSet<>(myIndex.h3.keySet());
		for (String s : words) {
			tempset.retainAll(myIndex.h1.get(s).keySet());
		}
		HashMap<URL, Integer> temp = new HashMap<>();
		List<String> temp3 = Arrays.asList(words);
		for (URL u : tempset) {
			if (Collections.indexOfSubList(Arrays.asList(myIndex.h3.get(u).split("\\W+")), temp3) != -1) {
				temp.put(u, 1);
			}
		}
		myIndex.h1.put(phrase, temp);
	}

	public void handleword(String word) {
		String[] words = word.split("\\W+");
		if (words.length == 0) {
			System.err.println("Invalid query: Empty word");
		} else if (words.length > 1) {
			HashSet<URL> tempset = new HashSet<>(myIndex.h3.keySet());
			for (String s : words) {
				tempset.retainAll(myIndex.h1.get(s).keySet());
			}
			HashMap<URL, Integer> temp = new HashMap<>();
			List<String> temp3 = Arrays.asList(words);
			for (URL u : tempset) {
				if (Collections.indexOfSubList(Arrays.asList(myIndex.h3.get(u).split("\\W+")), temp3) != -1) {
					temp.put(u, 1);
				}
			}
			myIndex.h1.put(word, temp);
		}
	}

	public LinkedList<String> initialparse(String query) {
		// This method separates the initial query into sub-queries, based on
		// the implicit ANDs
		int i = 0;
		int j = 0;
		int k = 0;
		LinkedList<String> queries = new LinkedList<>();
		while (i < query.length()) {
			if (query.charAt(i) == '"') {
				k++;
			} else if (query.charAt(i) == ' ' && k % 2 == 0) {
				if ((query.charAt(i - 1) != '(' && isletter(query.charAt(i - 1)))
						&& (query.charAt(i + 1) != ')' && query.charAt(i + 1) != '&' && query.charAt(i + 1) != '|')) {
					queries.add(query.substring(j, i));
					j = i + 1;
				}
			}
			i++;
		}
		queries.add(query.substring(j, i));
		return queries;
	}

	public Boolean isletter(char c) {
		if (c == '&' || c == '|' || c == '!')
			return false;
		return true;
	}

	class custom {
		// This custom type is used in the Shunting Yard algorithm.
		HashSet<URL> hSet;
		String tp;

		public custom(String a) {
			hSet = new HashSet<>();
			tp = a;
			if (!(a.equals("|") || a.equals("&") || a.equals("!"))) {
				if (myIndex.h1.containsKey(a)) {
					hSet = new HashSet<>(myIndex.h1.get(a).keySet());
				}
			}
		}

		public custom(HashSet<URL> h) {
			hSet = h;
			tp = "";
		}
	}

	// Comparator class
	class compareByConnections implements Comparator<Page> {
		// Comparator is used for sorting pages based on their number of
		// references.
		@Override
		public int compare(Page o1, Page o2) {
			return o2.connections - o1.connections;
		}
	}
}
