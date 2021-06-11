package org.neon.pathsFinder.engine;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.neon.model.Condition;
import org.neon.model.Heuristic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLWriter {
	public static void addXMLHeuristics(File destination, ArrayList<Heuristic> heuristics) throws Exception{
    boolean empty = true;
    if (destination.length()!=0){
    	empty = false;
    }
	
	Document doc = null;
	Element location = null;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	if (!empty){
		doc = dBuilder.parse(destination);
		location = (Element) doc.getElementsByTagName("heuristics").item(0);
	}else{
		doc =  dBuilder.newDocument();
		location = doc.createElement("heuristics");
		doc.appendChild(location);
	}
	for (Heuristic heuristic: heuristics){
		Element nlpElement = doc.createElement("NLP_heuristic");
		location.appendChild(nlpElement);
		
		if (heuristic.getSentence_type()!=null && !heuristic.getSentence_type().isEmpty()){
			Element sentence = doc.createElement("sentence");
			sentence.setAttribute("type", heuristic.getSentence_type());
			nlpElement.appendChild(sentence);
		}
		
		
		Element type = doc.createElement("type");
		type.appendChild(doc.createTextNode(heuristic.getType()));
		nlpElement.appendChild(type);
		
		Element heuristicText = doc.createElement("text");
		heuristicText.appendChild(doc.createTextNode((heuristic.getText())));
		nlpElement.appendChild(heuristicText);
		
		Element conditions = doc.createElement("conditions");
		nlpElement.appendChild(conditions);
		
		for (Condition c: heuristic.getConditions()){
			Element condition = doc.createElement("condition");
			condition.appendChild(doc.createTextNode(c.getConditionString()));
			conditions.appendChild(condition);
		}
	
		Element sentenceClass = doc.createElement("sentence_class");
		sentenceClass.appendChild(doc.createTextNode(heuristic.getSentence_class()));
		nlpElement.appendChild(sentenceClass);
	}
	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();
	DOMSource source = new DOMSource(doc);
	StreamResult res = new StreamResult(destination);	
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.transform(source, res);

	}

}
