package org.neon.engine;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.neon.model.Result;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLWriter {
	public static void addXMLSentences(File destination, ArrayList<Result> sentencesToExport) throws Exception{
	
	Document doc = null;
	Element location = null;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	doc =  dBuilder.newDocument();
	location = doc.createElement("sentences");
	doc.appendChild(location);

	for (Result r: sentencesToExport){
	
		if (r.getSentenceClass() != null && r.getSentence() != null){
			Element result = doc.createElement("categorized_sentence");
			location.appendChild(result);
			
			Element sentence = doc.createElement("sentence");
			sentence.appendChild(doc.createTextNode(r.getSentence()));
			result.appendChild(sentence);
			
			Element category = doc.createElement("category");
			category.appendChild(doc.createTextNode(r.getSentenceClass()));
			result.appendChild(category);
			

			
		}
		
		
	}
	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();
	DOMSource source = new DOMSource(doc);
	StreamResult res = new StreamResult(destination);	
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.transform(source, res);

	}

}
