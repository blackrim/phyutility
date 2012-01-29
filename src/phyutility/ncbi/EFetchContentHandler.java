package phyutility.ncbi;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class EFetchContentHandler extends DefaultHandler{
	private Stack stack;
	private boolean isStackReadyForText;
	public String webenv = "";
	public String querykey = "";
	public String gi = "";
	public String taxid = "";
	public String orgname = "";
	public String length = "";
	public String defline = "";
	public String sequence ="";
	int lengthlim = 10000;
	boolean dontread = false;
	private String outfor = "31";
	private String sep = "_";
	private String outfile;
	private FileWriter fw;
	private ArrayList<String> allfors;

	public EFetchContentHandler(Integer lengthlim, String outfile, String outfor, String sep){
		stack = new Stack();
		isStackReadyForText = false;
		if(lengthlim != null)
			this.lengthlim = lengthlim;
		if(outfor != null)
			this.outfor = outfor;
		if(sep != null)
			this.sep = sep;
		if(outfile != null){
			this.outfile = outfile;
			try {
				fw = new FileWriter(outfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		if (localName.equals("TSeq_gi")) {
			stack.push( new StringBuffer() );
			isStackReadyForText = true;
		}else if(localName.equals("TSeq_taxid")){
			stack.push( new StringBuffer() );
			isStackReadyForText = true;
		}else if(localName.equals("TSeq_orgname")){
			stack.push( new StringBuffer() );
			isStackReadyForText = true;
		}else if(localName.equals("TSeq_defline")){
			stack.push( new StringBuffer() );
			isStackReadyForText = true;
		}else if(localName.equals("TSeq_length")){
			stack.push( new StringBuffer() );
			isStackReadyForText = true;
		}else if(localName.equals("TSeq_sequence")&& dontread == false){
			stack.push( new StringBuffer() );
			isStackReadyForText = true;
		}else{
			stack.push(null);
		}
	}

	public void endElement( String uri, String localName, String qName ) {

		// recognized text is always content of an element
		// when the element closes, no more text should be expected
		isStackReadyForText = false;

		// pop stack and add to 'parent' element, which is next on the stack
		// important to pop stack first, then peek at top element!
		Object tmp = stack.pop();

		if( localName.equals( "TSeq_gi" ) ) {
			gi = tmp.toString();
		}else if(localName.equals("TSeq_taxid")){
			taxid = tmp.toString();
		}else if(localName.equals("TSeq_orgname")){
			orgname = tmp.toString();
		}else if(localName.equals("TSeq_defline")){
			defline = tmp.toString();
		}else if(localName.equals("TSeq_length")){
			length = tmp.toString();
			if(Integer.valueOf(length) > lengthlim)
				dontread = true;
			else
				dontread = false;
			//System.out.println(length);
		}else if(localName.equals("TSeq_sequence")&& dontread == false){
			if(dontread == false){
				sequence = tmp.toString();
				printall();
			}
		}
		else{
			stack.push( tmp );
		}
	}

	private void printall(){
		if(dontread == false){
			allfors = new ArrayList<String>();
			allfors.add(gi);allfors.add(taxid);allfors.add(orgname);allfors.add(defline);
			allfors.add(length);allfors.add(sequence);
			String pr = ">";
			for(int i=0;i<outfor.length();i++){
				String ac = allfors.get(Integer.valueOf(outfor.substring(i, i+1))-1);
				pr = pr.concat(ac.replaceAll(" ", sep));
				if(i+1 < outfor.length())
					pr = pr.concat(sep);
			}
			pr = pr.concat("\n");
			if(outfile == null){
				System.out.print(pr);
				System.out.println(sequence);
			}else{
				try {
					fw.write(pr);
					fw.write(sequence+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}
		}
	}

	public void close(){
		if(outfile!=null){
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	// -----

	public void characters( char[] data, int start, int length ) {

		// if stack is not ready, data is not content of recognized element
		if( isStackReadyForText == true ) {
			((StringBuffer)stack.peek()).append( data, start, length );
		}else{
			// read data which is not part of recognized element
		}
	}


	private String resolveAttrib( String uri, String localName, 
			Attributes attribs, String defaultValue ) {

		String tmp = attribs.getValue( uri, localName );
		return (tmp!=null)?(tmp):(defaultValue);
	}
}
