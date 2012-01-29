package phyutility.ncbi;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class Einfo {
	private static String esearchbase = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?";
	private static String efetchbase = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?";
	private String retmax = "10000000";
	String webenv = "";
	String querykey = "";

	public Einfo(){

	}

	public ArrayList<String> search(String db, String term){
		try{
			URL yh = new URL(esearchbase+"db="+db+"&usehistory=y&retmax="+retmax+"&term="+term);
			HttpURLConnection ut =(HttpURLConnection) yh.openConnection();
			ut.setDoOutput(true);
			BufferedReader in = new BufferedReader(new InputStreamReader(ut.getInputStream()));
			//String inputLine;
			//while ((inputLine = in.readLine()) != null)
			//	System.out.println(inputLine);

			ESearchContentHandler handler = new ESearchContentHandler();
			XMLReader reader = XMLReaderFactory.createXMLReader(  );
			InputSource inputSource = new InputSource(ut.getInputStream());
			reader.setContentHandler(handler);
			reader.parse(inputSource);
			webenv = handler.webenv;
			querykey = handler.querykey;
			ArrayList<String> ids = handler.ids;
			//System.out.println("Count: "+ids.size());	
			in.close();
			return ids;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public void efetch(String db, String term, int lengthlim, String outfile, String outfor, String sep){
		search(db, term);
		try{
			URL yh = new URL(efetchbase+"rettype=fasta&retmode=xml&retmax="+retmax+"&db="+db+"&query_key="+querykey+"&WebEnv="+webenv);
			HttpURLConnection ut =(HttpURLConnection) yh.openConnection();
			ut.setDoOutput(true);
			BufferedReader in = new BufferedReader(new InputStreamReader(ut.getInputStream()));
			//String inputLine;
			//while ((inputLine = in.readLine()) != null)
			//	System.out.println(inputLine);
			EFetchContentHandler handler = new EFetchContentHandler(lengthlim, outfile, outfor, sep);
			XMLReader reader = XMLReaderFactory.createXMLReader(  );
			InputSource inputSource = new InputSource(ut.getInputStream());
			reader.setContentHandler(handler);
			reader.parse(inputSource);
			String gi = handler.gi;
			String taxid = handler.taxid;
			String orgname = handler.orgname;
			String defline = handler.defline;
			int length = Integer.valueOf(handler.length);
			String seq = handler.sequence;
			handler.close();
			in.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Einfo ei = new Einfo();
		ei.efetch("nucleotide", "lonicera+internal", 2000, null, null, null);
	}
}
