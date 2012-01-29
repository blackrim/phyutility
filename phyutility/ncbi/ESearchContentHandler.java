package phyutility.ncbi;

import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class ESearchContentHandler extends DefaultHandler {
	private Stack stack;
    private boolean isStackReadyForText;
    public String webenv = "";
    public String querykey = "";
	public ArrayList<String> ids = new ArrayList<String>();
	public ESearchContentHandler(){
		stack = new Stack();
		isStackReadyForText = false;
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		if (localName.equals("Id")) {
			stack.push( new StringBuffer() );
		    isStackReadyForText = true;
		}else if(localName.equals("WebEnv")){
			stack.push( new StringBuffer() );
		    isStackReadyForText = true;
		}else if(localName.equals("QueryKey")){
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

		if( localName.equals( "Id" ) ) {
			ids.add(tmp.toString() );
		}else if(localName.equals("WebEnv")){
			webenv = tmp.toString();
		}else if(localName.equals("QueryKey")){
			querykey = tmp.toString();
		}
		else{
			stack.push( tmp );
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
