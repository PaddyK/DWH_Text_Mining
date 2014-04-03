package main;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.Version;

import data.FSReader;
import data.MyDocument;

public class Program {
	public Program() {
		Analyzer analyzer;
		Directory dir = null;
		IndexWriterConfig iwConfig;
		IndexWriter iwriter = null;
		FSReader reader;
		
		try {
			analyzer = new StandardAnalyzer(Version.LUCENE_47);
			dir = FSDirectory.open(new File("G:\\Documents\\DHBW\\6Semester\\Data_Warehouse\\index\\"));
			iwConfig = new IndexWriterConfig(Version.LUCENE_47, analyzer);
			iwriter = new IndexWriter(dir, iwConfig);
			reader = new FSReader("G:\\Documents\\DHBW\\6Semester\\Data_Warehouse\\pubmed_result-HumanGene-072012.txt");
			reader.extractDocuments(iwriter);
			System.out.println("Number of docs according to reader : " + iwriter.numDocs());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
			iwriter.close();
			dir.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
			System.out.println("Finished");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Program();

	}

}
