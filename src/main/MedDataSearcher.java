package main;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

/**
 * @author johniv
 * @version 03.04.2014
 */
public class MedDataSearcher {

	private MedDataSearcher() {}

	public static void main(String[] args) throws Exception {
		String usage = "Usage:\tSearchFiles [-index dir] [-fields f[,f]] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String index = "G:\\Documents\\DHBW\\6Semester\\Data_Warehouse\\index\\";
		String[] field = { "abstract" };
		String queries = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;

		for (int i = 0;i < args.length;i++) {
			switch (args[i]) {
				case "-index":
					index = args[i + 1];
					i++;
					break;

				case "-field":
					field = args[i + 1].split(",");
					i++;
					break;

				case "-queries":
					queries = args[i + 1];
					i++;
					break;

				case "-query":
					queryString = args[i + 1];
					i++;
					break;

				case "-repeat":
					repeat = Integer.parseInt(args[i + 1]);
					i++;
					break;

				case "-raw":
					raw = true;
					break;

				case "-paging":
					hitsPerPage = Integer.parseInt(args[i + 1]);
					if (hitsPerPage <= 0) {
						System.err.println("There must be at least 1 hit per page.");
						System.exit(1);
					}
					i++;
					break;
			}
		}

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

		BufferedReader in;
		if (queries != null) {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
		} else {
			in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		}
		QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_47, field, analyzer);
		while (true) {
			if (queries == null && queryString == null) {
				// prompt the user
				System.out.println("Enter query: ");
			}

			String line = queryString != null ? queryString : in.readLine();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}

			Query query = parser.parse(line);
			System.out.println("Searching for: " + query.toString(Arrays.toString(field)));

			if (repeat > 0) {
				// repeat & time as benchmark
				Date start = new Date();
				for (int i = 0; i < repeat; i++) {
					searcher.search(query, null, 100);
				}
				Date end = new Date();
				System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
			}

			doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

			if (queryString != null) {
				break;
			}
		}
		reader.close();
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search engine presents
	 * pages of size n to the user. The user can then go to the next page if interested in
	 * the next hits.
	 *
	 * When the query is executed for the first time, then only enough results are collected
	 * to fill 5 result pages. If the user wants to page beyond this limit, then the query
	 * is executed another time and all hits are collected.
	 *
	 */
	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw, boolean interactive) throws IOException {
		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (true) {
			if (end > hits.length) {
				System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
				System.out.println("Collect more (y/n)?");
				String line = in.readLine();
				if (line.length() == 0 || line.charAt(0) == 'n') {
					break;
				}

				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				if (raw) {
					// output raw format
					System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
					continue;
				}

				Document doc = searcher.doc(hits[i].doc);

				String firstline = doc.getField("firstline").stringValue();
				String author = doc.getField("author").stringValue();
				String title = doc.getField("title").stringValue();
				String institute = doc.getField("institute").stringValue();
				String abstracta = doc.getField("abstract").stringValue();
				int pmid = doc.getField("pmid").numericValue().intValue();
//				int pmcid = doc.getField("pmcid").numericValue().intValue();

				if (firstline != null)
					System.out.println("   First line: " + firstline);
				if (author != null)
					System.out.println("   Author: " + author);
				if (title != null)
					System.out.println("   Title: " + title);
				if (institute != null)
					System.out.println("   Institute: " + institute);
				if (abstracta != null)
					System.out.println("   Abstract: " + abstracta);

				System.out.println("   PMID: " + pmid);
//				System.out.println("   PMCID: " + pmcid);

			}

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0)=='q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start+=hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit) break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}
}