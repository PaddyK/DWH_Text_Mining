package data;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;

public class MyDocument {
	
	/*
	 * Document Format:
	 * ===============================
	 * 1
	 * 2	[0-9]+. ([a-Z]' ')+
	 * 3
	 * 4	title
	 * 5
	 * 6	author1,author2,...
	 * 7
	 * 8	institute
	 * 9	(institute)
	 * 10
	 * 11	abstract
	 * 12	abstract
	 * ...
	 * N
	 * N+1	PMID: [0-9]\{8}
	 * N+2
	 * ========================================
	 */
	private String pmid;
	private String pmcid;
	private String firstline; // 93421. J. Exp Med. 1940 May 31;71(6):839-56 I do not know what this is
	private String title;
	private String author;
	private String institutes;
	private String content;
	private String date;
	
	
	public MyDocument() {
		pmid = null;
		pmcid = null;
		firstline = null;
		date = null;
		title = "";
		author = "";
		institutes = "";
		content = "";
	}

	public void setPmid(String pmid) {
		Pattern pattern = Pattern.compile("([^0-9]+)([0-9]{8})(.*)");
		Matcher matcher = pattern.matcher(pmid);
		if(matcher.matches())
			this.pmid = matcher.group(matcher.groupCount()-1);
		else
			System.err.println("No PMID matched");
	}
	
	public void setPmcid(String pmcid) {
		Pattern pattern = Pattern.compile("([^0-9]+)([0-9]{7})(.*)");
		Matcher matcher = pattern.matcher(pmcid);
		if(matcher.matches())
			this.pmcid = matcher.group(matcher.groupCount()-1);
		else
			System.err.println("No PMCID matched");
	}
	
	public void setFirstline(String firstLine) {
		this.firstline = firstLine;
	}
	
	public void addToTitle(String title) {
		this.title += title;
	}
	
	public void addToAuthor(String author) {
		this.author += author;
	}
	
	public void addToInstitute(String institute) {
		this.institutes += institute;
	}
	
	public void addToContent(String content) {
		this.content += content;
	}
	
	public Document getDocument() {
		Document doc = new Document();
		doc.add(new StringField("firstline", firstline, Field.Store.YES));
		doc.add(new StringField("author", author, Field.Store.YES));
		doc.add(new StringField("title", title, Field.Store.YES));
		doc.add(new StringField("institute", institutes, Field.Store.YES));
		doc.add(new StringField("abstract", content, Field.Store.YES));
		doc.add(new IntField("pmid", Integer.parseInt(pmid), Field.Store.YES));
		if(pmcid != null)
			doc.add(new IntField("pmcid", Integer.parseInt(pmcid), Field.Store.YES));
		
		return doc;
	}
	
	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		return firstline + nl + nl +
				title + nl + nl +
				author + nl + nl +
				institutes + nl + nl +
				content + nl + nl +
				"PMCID: " + pmcid + nl +
				"PMID: " + pmid;
	}
	
	public boolean isInstituteEmpty() {
		return institutes.isEmpty();
	}
	
	public boolean isContentEmpty() {
		return content.isEmpty();
	}
	
	public String getContent() {
		return content;
	}
	
	public String getInstitutes() {
		return institutes;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setInstitutes(String institutes) {
		this.institutes = institutes;
	}
}
