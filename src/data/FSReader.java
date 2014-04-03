package data;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexWriter;

public class FSReader {
	private static final int firstLineRead = 1;
	private static final int title = 2;
	private static final int author = 3;
	private static final int institute = 4;
	private static final int content = 5;
	private static final int pmcid = 6;
	private static final int pmid = 7;
	
	private String path;
	private static final String[] keywords = new String[]{"University",
		"Institut",
		"School",
		"Hospital",
		"Department",
		"Faculty",
		"Section",
		"Group",
		"College",
		"Corporation",
		"Laboratory",
		"Center",
		"Campus",
		"Ward",
		"Branch",
		"Division"};
	
	public FSReader(String path) {
		this.path = path;
	}
	
	public void extractDocuments(IndexWriter iwriter) {
		System.out.println("Start extracting");
		BufferedReader reader = null;
		data.MyDocument document = null;
		String line;
		Matcher matcher;
		
		int state = 0;
		int count = 0;
		int lineCount = 0;
		int emptyLines = 1;
		//Pattern beginPattern = Pattern.compile("[0-9]+\\..+Epub [0-9]{4} [a-zA-Z]+ [0-9]{2}.*");
		Pattern beginPattern = Pattern.compile("[0-9]+\\..*");
		Pattern pmidPattern = Pattern.compile("PMID: [0-9]{8}.*");
		Pattern pmcidPattern = Pattern.compile("PMCID:.*PMC[0.9]{7}.*");
		
		try {
			reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(path))));
			while((line = reader.readLine()) != null && count < 10000) {
				lineCount++;
				
				matcher = beginPattern.matcher(line);
				if(matcher.matches() && emptyLines == 2) {
					state = firstLineRead;
					document = new data.MyDocument();
				}
				
				matcher = pmidPattern.matcher(line);
				if(matcher.matches()) {
					state = pmid;
					count ++;
				}
				
				matcher = pmcidPattern.matcher(line);
				if(matcher.matches()) {
					state = pmcid;
				}


				// content may include empty lines
				if(line.isEmpty() && state != 0 && state < content)
					state++;
				
				if(line.isEmpty())
					emptyLines++;
				else
					switch(state) {
					case firstLineRead: document.setFirstline(line);
						break;
					case title: document.addToTitle(line + " ");
						break;
					case author: document.addToAuthor(line + " ");
						break;
					case institute: document.addToInstitute(line + " ");
						break;
					case content: document.addToContent(line + " ");
						break;
					case pmcid: document.setPmcid(line);
						break;
					case pmid: document.setPmid(line);
						state = 0;
						emptyLines = 0;
						if(document.isContentEmpty() && document.isInstituteEmpty())
							if(!isInstitute(document.getInstitutes(), keywords))
								document.setContent(document.getInstitutes());
						iwriter.addDocument(document.getDocument());
						break;
					}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(Exception e) {
			System.err.println("Error on line "+lineCount);
			System.err.println("===========================");
			e.printStackTrace();
		}
		finally {
			try {reader.close();}
			catch (IOException e) { e.printStackTrace(); }
		}
		System.out.println("Documents: " + count + "\n Lines read: " + lineCount);
	}
	
	private boolean isInstitute(String data, String[] keywords) {
		String[] tokens = data.split("\\w");
		boolean isInstitute = false;
		int length = tokens.length;
		int count = 0;
		for(String t : tokens) {
			for(String k : keywords) {
				if(t.contains(k)) {
					count++;
				}
			}
			if((double)count/(double)length >= 0.1) {
				isInstitute = true;
				break;
			}
		}
			
		return isInstitute;
	}
}
