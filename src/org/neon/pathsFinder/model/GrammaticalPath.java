package org.neon.pathsFinder.model;

import java.util.ArrayList;

import simplenlg.features.Feature;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.english.Realiser;

public class GrammaticalPath {
	private ArrayList<String> conditions;
	private ArrayList<Sentence> exampleSentences;
	
	
	private int counter;
	
	public GrammaticalPath(ArrayList<String> conditions){
		this.conditions = conditions;
	}

	public ArrayList<String> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<String> conditions) {
		this.conditions = conditions;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	@Override
	public boolean equals(Object o){
		GrammaticalPath gr = (GrammaticalPath) o;
		boolean equal = true;
		if (this.getConditions().size() != gr.getConditions().size()){
			equal = false;
		}
		int i = 0;
		while (equal && i<this.getConditions().size() && i<gr.getConditions().size()){
			if (!this.getConditions().get(i).equalsIgnoreCase(gr.getConditions().get(i))){
				equal = false;
			}
			i++;
		}
		return equal;
	}

	public ArrayList<Sentence> getExampleSentences() {
		return exampleSentences;
	}

	public void setExampleSentences(ArrayList<Sentence> exampleSentences) {
		this.exampleSentences = exampleSentences;
	}
	
	public String getShortestExampleSentence(){
		int minLength = 0;
		int shortestSentencePosition = 0;
		
		for (int i = 0; i<exampleSentences.size(); i++){
			Sentence currentSentence = exampleSentences.get(i);
			if (currentSentence.getText().length()<minLength){
				minLength = currentSentence.getText().length();
				shortestSentencePosition = i;
			}
		}
		return exampleSentences.get(shortestSentencePosition).getText();
	}
	
	
     public String getDependenciesPath(){
		   String precDep = "";
		   String returnString = "";
		   
		   for (String condition: this.getConditions()){
			   String dep;
			   if (condition.contains("\"")){
				   dep = condition.substring(0,condition.indexOf("."));
			   }else{
				   dep = condition.substring(condition.indexOf("=")+1, condition.lastIndexOf("."));
			   }
			   if (!dep.equalsIgnoreCase(precDep)){
				   if (!returnString.isEmpty()){
					   returnString = returnString + "/";
				   }
				   returnString = returnString + dep;
				   precDep = dep;
			   }
		   }
		   return returnString;
	   }

	
	
	
	 public String getTemplateText(){
		   String returnString = "";
		   try{
		   
		   
		   String path = this.getDependenciesPath();
		   
		   String[] dependencies = path.split("/");
		   
		   
	       Lexicon lexicon = new XMLLexicon();
		   NLGFactory phraseFactory = new NLGFactory(lexicon);
		   Realiser realiser = new Realiser(lexicon);
		   ArrayList<SPhraseSpec> phrases = new ArrayList<SPhraseSpec>(); 
		   
		      
			String verbo = "";
		    String precReln = "";
			for (String s: this.getConditions()){
		
				
				if (s.contains("\"")){
					String reln = s.substring(0,s.indexOf("."));
					String childType = s.substring(s.indexOf(".")+1,s.indexOf("="));
					String tokens = s.substring(s.indexOf("\"")+1,s.lastIndexOf("\""));
					String tok[] = tokens.split(" ");
					if (reln.equalsIgnoreCase("nsubj") ||
						reln.equalsIgnoreCase("csubj") ||
						reln.equalsIgnoreCase("csubjpass") ||
						reln.equalsIgnoreCase("nsubjpass")) {
							if (!reln.equalsIgnoreCase(precReln)){
								SPhraseSpec phrase = phraseFactory.createClause();
								phrases.add(phrase);
							}
							SPhraseSpec phrase = phrases.get(phrases.size()-1);
							if (childType.equalsIgnoreCase("governor")){
								String verb = tok[tok.length-1];
								verbo = verb;
								phrase.setVerb(verb);
								if (reln.contains("pass")){
									phrase.setFeature(Feature.PASSIVE,true);
								}
							}else{
								if ((Boolean) phrase.getFeature(Feature.PASSIVE)){
									phrase.setObject(tok[tok.length-1]);
								}else{
									phrase.setSubject(tok[tok.length-1]);	
								}
							}
					}else if (reln.equalsIgnoreCase("cop")){
						SPhraseSpec s1 = null;
						if (!phrases.isEmpty()){
							s1 = phrases.get(phrases.size()-1);	
						}else{
							s1 = phraseFactory.createClause();
							phrases.add(s1);
						}
						if (childType.equalsIgnoreCase("dependent")){
							if(s1.getFeature(Feature.MODAL)!=null){
								s1.setFeature(Feature.MODAL, s1.getFeature(Feature.MODAL).toString()+" "+tok[tok.length-1]);
								
								
							}else{
								VPPhraseSpec temp = phraseFactory.createVerbPhrase(tok[tok.length-1]+" "+verbo);
								s1.setVerb(temp);
								
							}

						}else{
							if (s1.getVerb()!=null){
								
								VPPhraseSpec temp = phraseFactory.createVerbPhrase(verbo+" "+tok[tok.length-1]);
								s1.setVerb(temp);
							}
						}
					}
					else if (reln.equalsIgnoreCase("aux")){
						SPhraseSpec s1 = null;
						if (!phrases.isEmpty()){
							s1 = phrases.get(phrases.size()-1);	
						}else{
							s1 = phraseFactory.createClause();
							phrases.add(s1);
						}

						if (!childType.equalsIgnoreCase("governor")){
							
							if (!tok[tok.length-1].equalsIgnoreCase("be") ){
								s1.setFeature(Feature.MODAL, tok[tok.length-1]);	
							}else if (tok[tok.length-1].equalsIgnoreCase("be") && s1.getVerb()!=null){
								s1.setFeature(Feature.PROGRESSIVE, true);
							}
						}else if (s1.getVerb() == null){
							String verb = tok[tok.length-1]; 
							verbo = verb;
							s1.setVerb(verb);
						}
					
				}else if (reln.equalsIgnoreCase("auxpass")){
						SPhraseSpec s1 = null;
						if (!phrases.isEmpty()){
							s1 = phrases.get(phrases.size()-1);	
						}else{
							s1 = phraseFactory.createClause();
							phrases.add(s1);
						}

							if (s1.getVerb()!=null){
								s1.setFeature(Feature.PASSIVE,true);
							}
							if (childType.equalsIgnoreCase("governor")){
								if (s1.getVerb() == null){
									String verb = tok[tok.length-1]; 
									verbo = verb;
									s1.setVerb(verb);
								}
								
							}
					
				}
			
					
				else if (reln.equalsIgnoreCase("dobj")){
					SPhraseSpec s1 = null;
					if (!phrases.isEmpty()){
						s1 = phrases.get(phrases.size()-1);	
					}else{
						s1 = phraseFactory.createClause();
						phrases.add(s1);
					}
		
					if (!childType.equalsIgnoreCase("governor")){
						s1.setObject(tok[tok.length-1]);
					}else{
						if (s1.getVerb() == null){
							String verb = tok[tok.length-1]; 
							verbo = verb;
							s1.setVerb(verb);
						}
					}
				
				}else if (reln.equalsIgnoreCase("xcomp") || reln.equalsIgnoreCase("ccomp")){
					SPhraseSpec s1 = null;
					if (!phrases.isEmpty()){
						s1 = phrases.get(phrases.size()-1);	
					}else{
						s1 = phraseFactory.createClause();
						phrases.add(s1);
					}
					if (childType.equalsIgnoreCase("dependent")){
						s1.addComplement("to "+tok[tok.length-1]);
					}else{
						if (s1.getVerb() == null){
							String verb = tok[tok.length-1]; 
							verbo = verb;
							s1.setVerb(verb);
						}
					}
				}else if (reln.equalsIgnoreCase("acomp")){
					SPhraseSpec s1 = null;
					if (!phrases.isEmpty()){
						s1 = phrases.get(phrases.size()-1);	
					}else{
						s1 = phraseFactory.createClause();
						phrases.add(s1);
					}
					if (childType.equalsIgnoreCase("dependent")){
						if (s1.getVerb() != null){
							VPPhraseSpec temp = phraseFactory.createVerbPhrase(verbo+" "+tok[tok.length-1]);
							s1.setVerb(temp);
						}
					}else{
						if (s1.getVerb() == null){
							s1.setVerb(tok[tok.length-1]);
						}
					}
					
					
				
				}else if (reln.equalsIgnoreCase("iobj")){
						SPhraseSpec s1 = null;
						if (!phrases.isEmpty()){
							s1 = phrases.get(phrases.size()-1);	
						}else{
							s1 = phraseFactory.createClause();
							phrases.add(s1);
						}
			
						if (!childType.equalsIgnoreCase("governor")){
							s1.setIndirectObject("to "+tok[tok.length-1]);
							
						}else{
							if (s1.getVerb() == null){
								String verb = tok[tok.length-1]; 
								verbo = verb;
								s1.setVerb(verb);
							}
						}
				}else if (reln.equalsIgnoreCase("vmod")){
					SPhraseSpec s1 = null;
					if (!phrases.isEmpty()){
						s1 = phrases.get(phrases.size()-1);	
					}else{
						s1 = phraseFactory.createClause();
						phrases.add(s1);
					}
		
					if (!childType.equalsIgnoreCase("governor")){
						s1.addComplement(tok[tok.length-1]);
					}
				}
					
					
					
					
				}else if (s.contains("neg.")){
					SPhraseSpec s1 = null;
					if (!phrases.isEmpty()){
						s1 = phrases.get(phrases.size()-1);	
					}else{
						s1 = phraseFactory.createClause();
						phrases.add(s1);
					}
					s1.setFeature(Feature.NEGATED, true);
				}
				precReln = s.substring(0,s.indexOf("."));
				
			}
			CoordinatedPhraseElement c = phraseFactory.createCoordinatedPhrase();
			int index1 = 0;
			int index2 = 0;
			boolean firstNsubj = true;
			while (index1<phrases.size() && index2<dependencies.length){
			   SPhraseSpec sent = phrases.get(index1);
			   String reln = dependencies[index2];
			   if (sent.getVerb()==null){
				   sent.setVerb("[verb]");
			   }
			   if (reln.equalsIgnoreCase("dobj")){
				   if (sent.getObject() == null){
					   sent.setObject("[something]");
				   }
			   }
			   else if (reln.equalsIgnoreCase("aux")){
				   if (sent.getFeature(Feature.MODAL)==null){
					   sent.setFeature(Feature.MODAL, "[auxiliary]");
				   }
			   }
			   else if (reln.equalsIgnoreCase("iobj")){
				   if (sent.getIndirectObject()==null){
					   sent.setIndirectObject("to [something]");
				   }
			   }
			   
			   
			   else if (reln.equalsIgnoreCase("nsubj")){
	       		   if (!firstNsubj){
	       			   index1++;
	       		   }
				   if (sent.getSubject()==null){
					   sent.setSubject("[something]");
				   }
	       		   firstNsubj = false;
			   }
			   
			   index2++;
			  
			   
			}
		   
			for (SPhraseSpec sent: phrases){
				c.addCoordinate(sent);
			}
			
			returnString = realiser.realiseSentence(c);
		  
		   }catch(Exception ex){
			   returnString = "";
		   }
		   
		   return returnString;
	   }
	 	
	    public String identifySentenceType(){
	    	int interrogativeCounter = 0;
	    	int declarativeCounter = 0;
	    	String returnValue = "all";
	    	for (Sentence s: this.getExampleSentences()){
	    		if (s.isInterrogative()){
	    			interrogativeCounter++;
	    		}else{
	    			declarativeCounter++;
	    		}
	    	}
	    	if (declarativeCounter == 0){
	    		returnValue = "interrogative";
	    	}else if (interrogativeCounter == 0){
	    		returnValue = "declarative";
	    	}
	    	return returnValue;
	    }

	
}
