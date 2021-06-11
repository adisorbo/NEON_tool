package org.neon.engine;

import java.io.File;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neon.model.Condition;
import org.neon.model.Heuristic;
import org.neon.model.Result;

import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.TypedDependency;

/* The Classifier class reads the xml file containing the user specified NLP heuristics
 * and process these heuristics in order to analyze the text under analysis and
 * categorize the text pharagraphs.
 */
public class Classifier {
	
	// class fields
	
	private Morphology m = new Morphology();
	
	private Collection<TypedDependency> tdl;
	
	private String sent;
	
	private HashMap<String,ArrayList<TypedDependency>> taggedDependencies;
	
	private File strategy;
	
	private boolean interrogative;
	
	protected Classifier(String sentence, boolean interrogative, Collection<TypedDependency> tdl, File strategy){
		// For each sentence an object of type classifier is instantiated 
	
	
		this.tdl = tdl;
		this.sent = sentence;
		this.strategy = strategy;
		this.interrogative = interrogative;
		
		taggedDependencies = new HashMap<String, ArrayList<TypedDependency>>(); 
		


		
		// The following code constructs the hashmap containing the typed dependencies 
		// grouped according to the the grammatical relations (e.g., in this data 
		// structure all the nsubj are collected together, all the dobj are collected together, etc.)
		
		for (TypedDependency td: this.tdl){
//			System.out.println(td.toString());
//	 		System.out.flush();
			ArrayList<TypedDependency> arr = null;
			arr = taggedDependencies.get(td.reln().toString());
			if (arr==null){
				arr = new ArrayList<TypedDependency>();
			}
			arr.add(td);
			taggedDependencies.put(td.reln().toString(), arr);
		}
			
	
	}
	
	
	
	
	
	/* The following method performs the recognition: the sentence is analyzed towards
	 * the defined NLP heuristics.
	 */
	protected ArrayList<Result> recognize(){
		this.sent = this.sent.replace("^", ".");
		try{

			// The heuristics defined in the xml file are read
			ArrayList<Heuristic> heuristics = XMLReader.read(strategy);
			
			ArrayList<Result> results = new ArrayList<Result>();
			
			Iterator<Heuristic> iterator = heuristics.iterator();
			Result res = null;

			// Each defined heuristic is processed 
			// in order to find the first heuristic matching 
			// with the sentence under analysis.

			while (iterator.hasNext()){ // && res == null ){
				Heuristic h = iterator.next();
				
				res = this.processHeuristic(h);
				if (res!=null){
					res.setSentence(this.sent);
					results.add(res);
				}
			}
			return results;
			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	/* The following method allows to get the right collection of typed dependencies
	 * through the specification of the dependency name
	 */
		
	private ArrayList<TypedDependency> getTheCollection(String str){
		if (taggedDependencies.containsKey(str)){
			return taggedDependencies.get(str);
		}else{
			return new ArrayList<TypedDependency>();
		}
		
		
	}
	
	/* Since each NLP heuristic comprises a set of conditions,
	 * the following method represents the core part of the Classifier class because 
	 * it processes each heuristic condition in order to verify
	 * if there are some paths in the Stanford Dependencies tree related to the 
	 * sentence under analysis matching with the specific condition
	 */
	
	private ArrayList<TypedDependency> processCondition(Condition c, ArrayList<TypedDependency> prec_s){
		String tokens = "";
		ArrayList<TypedDependency> res = new ArrayList<TypedDependency>();
		if (c.getLastTerm().isTokensType()){
			tokens = c.getLastTerm().getTokens();
			if (!tokens.trim().startsWith("\"regex:")){
				tokens = tokens.toLowerCase();
			}
			tokens = tokens.replaceAll("\"", "")+" ";
		}
		

		if (prec_s == null){
			
			for (TypedDependency s: getTheCollection(c.getFirstTerm().getTd_type())){
				if (c.getLastTerm().isTokensType()){
					if (c.getFirstTerm().getChild_type().equals("gov()")){
								if (tokens.trim().startsWith("regex:")){
			                    	String regex = tokens.trim().substring(6).trim();
			                    	if (s.gov().nodeString().matches(regex)){
				                    		res.add(s);
			
			                    	}
			                    	
			                    }else{
	
							
									String lemma = m.stem(s.gov().nodeString().toLowerCase());
									if (tokens.contains(" "+lemma.toLowerCase()+" ")){
											res.add(s);
		
									}
			                    }
		                    
		                    	
					}else{
	                    if (tokens.trim().startsWith("regex:")){
	                    	String regex = tokens.trim().substring(6).trim();
	                    	if (s.dep().nodeString().matches(regex)){
		                    		res.add(s);
	                    			
	                    	}
	                    }else{
						
							String lemma = m.stem(s.dep().nodeString().toLowerCase());
							if (tokens.contains(" "+lemma+" ")){
									res.add(s);	
								
							}
	                    }
					}
				}else{
					for (TypedDependency s2:getTheCollection(c.getLastTerm().getTd_type())){
						if (c.getFirstTerm().getChild_type().equals("gov()")){
							if (c.getLastTerm().getChild_type().equals("gov()")){
								if (s.gov().equals(s2.gov()) && !s.equals(s2)){
									if (c.isNegative()){
										res.add(s);
									}else{
										res.add(s2);	
									}
									
								}
							}else{
								if (s.gov().equals(s2.dep())  && !s.equals(s2)){
									if (c.isNegative()){ // || s2.reln().toString().equalsIgnoreCase("neg")){
										res.add(s);
									}else{
										res.add(s2);	
									}
									
								}
							}
						}else{
							if (c.getLastTerm().getChild_type().equals("gov()")){
								if (s.dep().equals(s2.gov())  && !s.equals(s2)){
									if (c.isNegative()){ // || s2.reln().toString().equalsIgnoreCase("neg")){
										res.add(s);
									}else{
										res.add(s2);	
									}

								}
							}else{
								if (s.dep().equals(s2.dep()) && !s.equals(s2)){
									if (c.isNegative()){ // || s2.reln().toString().equalsIgnoreCase("neg")){
										res.add(s);
									}else{
										res.add(s2);	
									}
	
									
								}
							}
							
						}
					}
				}
			}
		}else{
			if (c.getLastTerm().isTokensType()){
				for (TypedDependency td: prec_s){
					if (c.getFirstTerm().getChild_type().equals("gov()")){
						if (tokens.trim().startsWith("regex:")){
	                    	
							String regex = tokens.trim().substring(6).trim();
							if (td.gov().nodeString().matches(regex)){
	                    			res.add(td);	
								
	                    	}
						}else{
						String lemma = m.stem(td.gov().nodeString().toLowerCase());
						if (tokens.contains(" "+lemma.toLowerCase()+" ")){
								res.add(td);	
							
						}
						}
					}else{
						if (tokens.trim().startsWith("regex:")){
	                    	String regex = tokens.trim().substring(6).trim();
	                    	if (td.dep().nodeString().matches(regex)){
	                    			res.add(td);	
	                    		
	                    	}
						}else{
         					String lemma = m.stem(td.dep().nodeString().toLowerCase());
							if (tokens.contains(" "+lemma+" ")){
									res.add(td);	
								
							}
						}
					}
				}
			}else{
				for (TypedDependency td: prec_s){
					for (TypedDependency s2:getTheCollection(c.getLastTerm().getTd_type())){
						if (c.getFirstTerm().getChild_type().equals("gov()")){
							if (c.getLastTerm().getChild_type().equals("gov()")){
								if (td.gov().equals(s2.gov()) && !td.equals(s2)){
									if (c.isNegative()){ 
										res.add(td);
									}else{
										res.add(s2);	
									}
										
									
								}
							}else{
								if (td.gov().equals(s2.dep()) && !td.equals(s2)){
									if (s2.reln().toString().equalsIgnoreCase("neg")){
										res.add(s2);
									}else{
										res.add(s2);	
									}
									
								}
							}
						}else{
							if (c.getLastTerm().getChild_type().equals("gov()")){
								if (td.dep().equals(s2.gov()) && !td.equals(s2)){
									if (c.isNegative()){ 
										res.add(td);
									}else{
										res.add(s2);	
									}
									
								}
							}else{
								if (td.dep().equals(s2.dep()) && !td.equals(s2)){
									if (c.isNegative()){ 
										res.add(td);
									}else{
										res.add(s2);	
									}
									
								}
							}
							
						}
					}
				}
			}
		}
		return res;
	  }	
	
	
	private Result processHeuristic(Heuristic h){
		Result res = null;
		
		if ((h.getSentence_type().equalsIgnoreCase("interrogative") && this.interrogative)||
		    (h.getSentence_type().equalsIgnoreCase("declarative") && !this.interrogative) ||
		    (h.getSentence_type().equalsIgnoreCase("all"))){
		
			Iterator<Condition> conditions = h.getConditions().iterator();
			Condition c1 = conditions.next();
			
			
			ArrayList<TypedDependency> tdep = processCondition(c1,null);
			
			
			ArrayList<TypedDependency> neg_ret = new ArrayList<TypedDependency>();
			while (conditions.hasNext() && !tdep.isEmpty()){
				Condition cond = conditions.next();
				if (cond.isNegative()){
					neg_ret = this.processCondition(cond, tdep);
					if (neg_ret.isEmpty()){
						continue;
					}else{
						tdep = neg_ret;
					}
					
				}else{
					tdep = processCondition(cond, tdep);
				}
			}
			if (!tdep.isEmpty() && neg_ret.isEmpty()){
				res = new Result();
				res.setSentenceClass(h.getSentence_class());
				res.setHeuristic(h.getText());
			}
		}
		return res;
		
	}		
		

	
	
   private ArrayList<String> extractUrls(String input) {
        ArrayList<String> result = new ArrayList<String>();

        Pattern pattern = Pattern.compile(
            "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" + 
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" + 
            "|mil|biz|info|mobi|name|aero|jobs|museum" + 
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" + 
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" + 
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" + 
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + 
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" + 
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
    
 

}
