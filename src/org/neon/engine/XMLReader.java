package org.neon.engine;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.neon.model.Condition;
import org.neon.model.Heuristic;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLReader {
  
  
  
  
  public static ArrayList<Heuristic> read(File strategy) {
    
    
	  
	ArrayList<Heuristic> heuristics = new ArrayList<Heuristic>();  
    try {
 
	
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	dBuilder.setErrorHandler(new ErrorHandler() {
		@Override
		public void error(SAXParseException arg0) throws SAXException {
			arg0.printStackTrace();
			System.exit(1);
		}
		@Override
		public void fatalError(SAXParseException arg0) throws SAXException {
			arg0.printStackTrace();
			System.exit(1);
    	}
		@Override
		public void warning(SAXParseException arg0) throws SAXException {
			arg0.printStackTrace();
		}
	});
	
	Document doc = dBuilder.parse(strategy); 
	
	doc.getDocumentElement().normalize();
 
	NodeList nList = doc.getElementsByTagName("NLP_heuristic");
 
	
	for (int temp = 0; temp < nList.getLength(); temp++) {
 
		Node nNode = nList.item(temp);
 
 
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Heuristic heuristic = new Heuristic();
			Element eElement = (Element) nNode;
 
			heuristic.setType(eElement.getElementsByTagName("type").item(0).getTextContent());
			heuristic.setText(eElement.getElementsByTagName("text").item(0).getTextContent());
			
			if (eElement.getElementsByTagName("sentence") != null && eElement.getElementsByTagName("sentence").item(0) != null){
				Element sentenceEl = (Element) eElement.getElementsByTagName("sentence").item(0);
				heuristic.setSentence_type(sentenceEl.getAttribute("type"));
			}else{
				heuristic.setSentence_type("all");
			}
			
			
			
			ArrayList<Condition> conditions = new ArrayList<Condition>();
			if (eElement.getElementsByTagName("conditions").item(0).hasChildNodes()){
				NodeList clist = eElement.getElementsByTagName("conditions").item(0).getChildNodes();
				
				for (int count = 0; count<clist.getLength(); count++){
					
					Node cNode = clist.item(count);
					
					
					if (cNode.getNodeType() == Node.ELEMENT_NODE){
						
						Element cElement = (Element) cNode;
						if (cElement.getNodeName().equals("condition")){
							
							String cond = cElement.getTextContent();
							String[] sts = cond.split("=");
							for (int c =0; c<sts.length; c++){
								sts[c] = sts[c].trim();
							}
							Condition conditio = parseConditionsStrings(sts);
							conditions.add(conditio);
						}
					}
				}
				
			}
			heuristic.setConditions(conditions);

			String category = eElement.getElementsByTagName("sentence_class").item(0).getTextContent();
			
			heuristic.setSentence_class(category);
						
			heuristics.add(heuristic);
		}
	}
    } catch (Exception e) {
    	System.err.println("Unable to read XML file");
    	e.printStackTrace();
    	System.exit(1);
    }
	
	return heuristics;
  }

  public static ArrayList<String> getSentenceClasses(File strategy){
	  ArrayList<String> categoriesList = null;   
	  try {
	    	 
	    	categoriesList = new ArrayList<String>();
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	dBuilder.setErrorHandler(new ErrorHandler() {
	    		@Override
	    		public void error(SAXParseException arg0) throws SAXException {
	    			arg0.printStackTrace();
	    			System.exit(1);
	    		}
	    		@Override
	    		public void fatalError(SAXParseException arg0) throws SAXException {
	    			arg0.printStackTrace();
	    			System.exit(1);
	        	}
	    		@Override
	    		public void warning(SAXParseException arg0) throws SAXException {
	    			arg0.printStackTrace();
	    		}
	    	});
	    	
	    	Document doc = dBuilder.parse(strategy); 
	    	
	    	doc.getDocumentElement().normalize();
	    	
	    	NodeList nlist = doc.getElementsByTagName("sentence_class");
	    	
	    	for (int i = 0; i<nlist.getLength(); i++){
	    		Node n = nlist.item(i);
	    		if (n.getNodeType() == Node.ELEMENT_NODE){
	    			Element element = (Element) n;
	    			String category = n.getTextContent();
	    			if (!categoriesList.contains(category.toUpperCase())){
	    				categoriesList.add(category.toUpperCase());
	    			}
	    			
	    		}
	    		
	    	}
	    	
	    	
	    }catch(Exception ex){
	    	System.err.println("Unable to read XML file");
	    	ex.printStackTrace();

	    }
	    return categoriesList;
  }
  
  
  
  
  
  private static Condition parseConditionsStrings(String[] sts){
	  // Parse first condition element
	  Condition cond = new Condition();
	  
	  
	  cond.setConditionString(sts[0]+"="+sts[1]);
	  
	  String first = sts[0];
	  String last = sts[1];

	  if (first.startsWith("not:")){
		  cond.setNegative(true);
		  first = sts[0].substring(4);
	  }else{
		  cond.setNegative(false);
	  }
	  int index = first.indexOf(".");
	  String tdep = first.substring(0,index);
	  
	  String gd = first.substring(index+1);
	  cond.getFirstTerm().setTd_type(tdep);
	  cond.getFirstTerm().setChild_type(gd);
	  
	  	  
	  String tdep_last = null;
	  String gd_last = null;
	  
	  
	  if (last.contains(".governor") || last.contains(".dependent")){
		  int ind = last.indexOf(".");
		  tdep_last = last.substring(0,ind);
		  gd_last = last.substring(ind+1);
		  cond.getLastTerm().setTd_type(tdep_last);
		  cond.getLastTerm().setChild_type(gd_last);
		  cond.getLastTerm().setTokensType(false);
	  
	  }else{
		  
		  
		  cond.getLastTerm().setTokens(last);
		  
		  cond.getLastTerm().setTokensType(true);
		  
	  
	  }
      return cond;
	  
	  
  }
  
  
  
}


