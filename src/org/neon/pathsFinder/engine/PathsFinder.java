package org.neon.pathsFinder.engine;

import info.debatty.java.stringsimilarity.MetricLCS;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.NGram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.neon.pathsFinder.model.GrammaticalPath;
import org.neon.pathsFinder.model.Sentence;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.process.Morphology;

import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.Pair;

public class PathsFinder {
	private static PathsFinder finder;
	
	private static float scannedSentences;
	
	private PathsFinder(){
		
	}
	
	public static PathsFinder getInstance(){
		if (finder == null){
			finder = new PathsFinder();
		}
		return finder;
	}
	
	
  /* Normalized N-Gram distance as defined by Kondrak, 
   * "N-Gram Similarity and Distance", String Processing and 
   * Information Retrieval, Lecture Notes in Computer Science Volume 3772, 2005, pp 115-126.   
   */	
	
   private double computeSentencesDistance(String s1, String s2){
	   Jaccard jaccard = new Jaccard();
	   return jaccard.distance(s1,s2);
	   
   }
	
	
   
   
   
	
	public ArrayList<GrammaticalPath> discoverCommonPaths(List<Sentence> parsedSentences){
		ArrayList<GrammaticalPath> paths = new ArrayList<GrammaticalPath>();
		
		int sentencesCounter = 1;
		refreshScannedSentences();
		
		
		for (int i = 0; i<parsedSentences.size()-1; i++){
			sentencesCounter++;
			Sentence s1 = parsedSentences.get(i);
			
			
			for (int j = i+1; j<parsedSentences.size(); j++){
				Sentence s2 = parsedSentences.get(j);
				
				
				
				if (!s1.equals(s2) &&
					computeSentencesDistance(s1.getText().toLowerCase(), s2.getText().toLowerCase()) > 0.50){
					double sim = computeSentencesDistance(s1.getText().toLowerCase(), s2.getText().toLowerCase());
					
					
					
					ArrayList<String> conditions = discoverCommonPath(s1,s2);
					if (!conditions.isEmpty()){
						
						boolean found = false;
						int k = 0;
						while (!found && k<paths.size() ){
							if (paths.get(k).getConditions().equals(conditions)){
								found = true;
							}								
								
								
							
							k++;
						}
											
						if (!found){
							GrammaticalPath path = new GrammaticalPath(conditions);
							ArrayList<Sentence> exampleSentences = new ArrayList<Sentence>();
							exampleSentences.add(s1);
							exampleSentences.add(s2);
							path.setExampleSentences(exampleSentences);
							path.setCounter(1);
							
							paths.add(path);	
						}else{
							GrammaticalPath path = paths.get(k-1);
							path.getExampleSentences().add(s1);
							path.getExampleSentences().add(s2);
							path.setCounter(path.getCounter()+1);
							
						
						}
							
						
					}
								
				}
			}
			float progress = ((float) sentencesCounter / (float) parsedSentences.size()) * 35f;  
			setScannedSentences(progress);
		}
		
		paths = minimizePaths(paths);
			
		return paths;
	}
	
	private ArrayList<GrammaticalPath> minimizePaths(ArrayList<GrammaticalPath> paths){
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator rel1 = new WuPalmer(db);
				
		for (int i = 0; i<paths.size()-1; i++){
			
			for (int j = i+1; j<paths.size(); j++){
				GrammaticalPath path1 = paths.get(i);
				GrammaticalPath path2 = paths.get(j);
				if (!path1.equals(path2)){
					boolean equal = true;
					int k = 0;
					
					if (path1.getConditions().size() == path2.getConditions().size()){	
						ArrayList<String> newConditions = new ArrayList<String>();
						
						while (equal && k<path1.getConditions().size()){
							String cond1 = path1.getConditions().get(k);
							String cond2 = path2.getConditions().get(k);
							
							if (cond1.contains("\"") && cond2.contains("\"")){
								String dependency1 = cond1.substring(0, cond1.indexOf("="));
								String terms1 = cond1.substring(cond1.indexOf("=")+1);
								terms1 = terms1.replaceAll("\"", "");
								String dependency2 = cond2.substring(0, cond2.indexOf("="));
								String terms2 = cond2.substring(cond2.indexOf("=")+1);
								terms2 = terms2.replaceAll("\"", "");
								if (dependency1.equalsIgnoreCase(dependency2)){
									String[] splittedTerms1 = terms1.split(" ");
									String[] splittedTerms2 = terms2.split(" ");
									ArrayList<String> newTerms = new ArrayList<String>();
									for (String t1: splittedTerms1){
										for (String t2: splittedTerms2){
											double score = rel1.calcRelatednessOfWords(t1,t2);
											if (score<0.85){
												equal = false;
											}else{
												if (!newTerms.contains(t1)){
													newTerms.add(t1);
												}
												if (!newTerms.contains(t2)){
													newTerms.add(t2);
												}
											}
										}
									}
									if (equal){
										String newTermsString = "";
										for (String t: newTerms){
											newTermsString = t+" "+newTermsString;
										}
										newTermsString = newTermsString.trim();
										newConditions.add(dependency1+"=\""+newTermsString+"\"");
									}
								}else{
									equal = false;
								}
						}else if (!cond1.equalsIgnoreCase(cond2)){
							equal = false;
						}else{
							newConditions.add(cond1);
						}
						
						k++;
						
					}
					if (equal){
						
						int sumCounter = path1.getCounter() + path2.getCounter();
						ArrayList<Sentence> sampleSentences = new ArrayList<Sentence>();
						
						sampleSentences.addAll(path1.getExampleSentences());
						sampleSentences.addAll(path2.getExampleSentences());
						
						paths.remove(path1);
						paths.remove(path2);
						GrammaticalPath gp = new GrammaticalPath(newConditions);
						gp.setCounter(sumCounter);
						gp.setExampleSentences(sampleSentences);
						paths.add(gp);
						if (i>0) i = i -1;
					}
					
				}
					
				}
			}
		}
		
		
			
		for (int i = 0; i<paths.size(); i++){
			GrammaticalPath path1 = paths.get(i);
			if (path1.getConditions().size()<1 || path1.getConditions().size()>8){
				paths.remove(path1);
				if (i>0) i=i-1;
				 
			}else if (path1.getConditions().size()<2 && !path1.getConditions().get(0).contains("\"")){
				paths.remove(path1);
				if (i>0) i=i-1;
			}else{
				ArrayList<String> conditions = path1.getConditions();
			    int count = 0;
				for (String s : conditions){
					if (s.contains("\"")){
						count++;
					}
				}
				float rate = (float) count / (float) conditions.size();
				
				
				if (rate<0.00){
					paths.remove(path1);
					if (i>0) i=i-1;
				}else{
					for (int j = 0; j<paths.size(); j++){
						
						GrammaticalPath path2 = paths.get(j);
						if (!path1.equals(path2) && path1.getConditions().containsAll(path2.getConditions())){
								paths.remove(path2);
								if (i>0) i=i-1;
								if (j>0) j=j-1;
							
						
					}
				
				}

				}
				
			}
				
		}
		
		Collections.sort(paths, new Comparator<GrammaticalPath>() {
	        @Override
	        public int compare(GrammaticalPath g1, GrammaticalPath g2)
	        {
	            int score1 = g1.getCounter() * (g1.getConditions().size());
	            int score2 = g2.getCounter() * (g2.getConditions().size());
	        	
	        	return score1 < score2 ? 1 : score1 == score2 ? 0 : -1;
	        }
	    });
		
		
		for (GrammaticalPath gp: paths){
			for (int i = 0; i<gp.getConditions().size(); i++){
				String cond = gp.getConditions().get(i);
				if (!cond.contains("\"")){
					if (cond.substring(0, cond.indexOf("=")).equalsIgnoreCase(
							cond.substring(cond.indexOf("=")+1))){
						gp.getConditions().remove(cond);
						i = i - 1;
					}
							
				}
			}
		}
		
		
		
		
		setScannedSentences(getScannedSentences()+15f);		
		return paths;
		
		
	}
	
	
	
	
	
	private ArrayList<String> discoverCommonPath(Sentence s1, Sentence s2){
		ArrayList<String> conditions = new ArrayList<String>();
	    
		
		
		Morphology morph = new Morphology();
		ArrayList<Pair<GrammaticalRelation, IndexedWord>> similarRelns = new ArrayList<Pair<GrammaticalRelation,IndexedWord>>(); 
		
		
		SemgrexPattern verbs = SemgrexPattern.compile("[{tag:/VB.*/}|{tag:/NN.*/}]");
		if (s1.getGraph() != null && s2.getGraph()!= null && 
				!s1.getGraph().isEmpty() && !s2.getGraph().isEmpty()
				  && !s1.getGraph().getRoots().isEmpty() && !s2.getGraph().isEmpty() &&
				   !s1.getText().equalsIgnoreCase(s2.getText())){
		
			SemgrexMatcher matcher = verbs.matcher(s1.getGraph());
			
			ILexicalDatabase db = new NictWordNet();
			RelatednessCalculator rel1 = new WuPalmer(db);
			
			
			
			while(matcher.find()){
				try{
				boolean firstTime = true;
				IndexedWord verb = matcher.getMatch();
				
				String verbLemma = morph.stem(verb.originalText());
				
				List<Pair<GrammaticalRelation, IndexedWord>> pairs1 = s1.getGraph().childPairs(verb);
				
				
				
				
				for (int i = 0; i<pairs1.size(); i++){
					Pair<GrammaticalRelation, IndexedWord> pair = pairs1.get(i);
					
					
					GrammaticalRelation gr1 = pair.first;
					if (gr1.getShortName().equalsIgnoreCase("nsubj") ||
						gr1.getShortName().equalsIgnoreCase("nsubjpass") ||
						gr1.getShortName().equalsIgnoreCase("csubj") ||
						gr1.getShortName().equalsIgnoreCase("csubjpass") ||
						gr1.getShortName().equalsIgnoreCase("dobj") ||
						gr1.getShortName().equalsIgnoreCase("agent") ||
						gr1.getShortName().equalsIgnoreCase("iobj") ||
						gr1.getShortName().equalsIgnoreCase("xcomp") ||
						gr1.getShortName().equalsIgnoreCase("aux") ||
						gr1.getShortName().equalsIgnoreCase("auxpass") ||
						gr1.getShortName().equalsIgnoreCase("neg") ||
						gr1.getShortName().equalsIgnoreCase("acomp") ||
						gr1.getShortName().equalsIgnoreCase("vmod") ||
						gr1.getShortName().equalsIgnoreCase("cop") ||
						gr1.getShortName().equalsIgnoreCase("ccomp") ||
						gr1.getShortName().startsWith("prep")){
						    boolean td_condensed = false;
						    if (gr1.getSpecific()!=null){
						    	td_condensed = true;
						    }
						
						
						
							String lemmaWord1 = morph.stem(pair.second.originalText());
							
							
							SemgrexMatcher matcher2 = verbs.matcher(s2.getGraph());
							
							while(matcher2.find()){
								IndexedWord verb2 = matcher2.getMatch();
								
								String verbLemma2 = morph.stem(verb2.originalText());
								double score = rel1.calcRelatednessOfWords(verbLemma2, verbLemma);
								
											
								if (score>=0.90 || verbLemma2.equalsIgnoreCase(verbLemma)){
									List<Pair<GrammaticalRelation, IndexedWord>> pairs2 = s2.getGraph().childPairs(verb2);
								
									
									
									for (int j = 0; j<pairs2.size(); j++){
										
										Pair<GrammaticalRelation, IndexedWord> pair2 = pairs2.get(j);
										
										
										
										GrammaticalRelation gr2 = pair2.first;
										String lemmaWord2 = morph.stem(pair2.second.originalText());
										if (gr1.compareTo(gr2)==0){
											String depName = "";
											if (td_condensed){
												depName = gr1.getShortName()+"_"+gr1.getSpecific();
											}else{
												depName = gr1.getShortName();
											}
											similarRelns.add(pair2);
											if (firstTime){
												if (verbLemma.equalsIgnoreCase(verbLemma2)){
												
													String conditionString = depName+".governor=\""+verbLemma+"\"";
													conditions.add(conditionString);	
												}else{
													String conditionString = depName+".governor=\""+verbLemma+" "+verbLemma2+"\"";
													conditions.add(conditionString);
												
												}
												
												firstTime = false;
											}
											double score2 = rel1.calcRelatednessOfWords(lemmaWord1, lemmaWord2);
											
											if (score2>=0.90 || lemmaWord1.equalsIgnoreCase(lemmaWord2)){
												if (lemmaWord1.equalsIgnoreCase(lemmaWord2)){
													String conditionString = depName+".dependent=\""+lemmaWord2+"\"";
													conditions.add(conditionString);
												}else{
													String conditionString = depName+".dependent=\""+lemmaWord1+" "+lemmaWord2+"\"";
													conditions.add(conditionString);
												}
												
											}
										
										
										}
												
									}
									
								}
		
						}
					
					}
				}
				
				for (int k=0; k<similarRelns.size()-1; k++){
					Pair<GrammaticalRelation,IndexedWord> p = similarRelns.get(k);
					Pair<GrammaticalRelation,IndexedWord> p2 = similarRelns.get(k+1);
					
					Collection<TypedDependency> tdlist = s2.getGraph().typedDependencies();
					Iterator<TypedDependency> tdIterator = tdlist.iterator();
					TypedDependency td_p = null;
					
					TypedDependency td_p2 = null;
					
					while (tdIterator.hasNext() && (td_p==null || td_p2==null)){
						TypedDependency currentTd = tdIterator.next();
						
							 
						if (currentTd.reln().compareTo(p.first)==0 && (currentTd.gov().nodeString().equalsIgnoreCase(p.second.originalText())
							&& currentTd.gov().index()==p.second.index()) ||
							(currentTd.dep().nodeString().equalsIgnoreCase(p.second.originalText())
							&& currentTd.dep().index()==p.second.index())){
								td_p = currentTd;
						}
						if (currentTd.reln().compareTo(p2.first)==0 && 
							    (currentTd.gov().nodeString().equalsIgnoreCase(p2.second.originalText())
								&& currentTd.gov().index()==p2.second.index()) ||
								(currentTd.dep().nodeString().equalsIgnoreCase(p2.second.originalText())
								&& currentTd.dep().index()==p2.second.index())){
										td_p2 = currentTd;
						}
					}
					
					String conditionString = "";	
					String depName1 = p.first.getSpecific()==null ? p.first.getShortName() : p.first.getShortName()+"_"+p.first.getSpecific();
					String depName2 = p2.first.getSpecific()==null ? p2.first.getShortName() : p2.first.getShortName()+"_"+p2.first.getSpecific();
					
					
					if (td_p.gov().nodeString().equalsIgnoreCase(td_p2.gov().nodeString())){
										
						conditionString = depName1+".governor="+depName2+".governor";
					}else if (td_p.dep().nodeString().equalsIgnoreCase(td_p2.gov().nodeString())){
						conditionString = depName1+".dependent="+depName2+".governor";
					}else if (td_p.gov().nodeString().equalsIgnoreCase(td_p2.dep().nodeString())){
						conditionString = depName1+".governor="+depName2+".dependent";
					}else if (td_p.dep().nodeString().equalsIgnoreCase(td_p2.dep().nodeString())){
						conditionString = depName1+".dependent="+depName2+".dependent";
					}
					
					
					 
					
					
					
					if (!conditionString.equals("")){
						conditions.add(conditionString);	
					}
					
				}
					
					
							
						
			
					
				
				
				}catch(RuntimeException ex){
					System.err.println("Sentence skipped no roots in graph");
				}
				
					
			}
		}
		
		
		
		return sortConditions(conditions);	
		
		
	}
	
	
	private ArrayList<String> cleanConditions(ArrayList<String> conditions){
		
		
		
		ArrayList<String> cleanedConditions = new ArrayList<String>();
		if (conditions.size()>0){
			cleanedConditions.add(conditions.get(0));
			int conditionCount = 0;
			
			
			
			for (int i=1; i<conditions.size(); i++){
				String c1 = cleanedConditions.get(conditionCount);
				String c2 = conditions.get(i);
				
				
				if ((!c1.contains("\"")) && c2.contains("\"")){
					String relC1 = c1.substring(c1.indexOf("=")+1, c1.lastIndexOf("."));
					String partC1 = c1.substring(c1.lastIndexOf(".")+1);
					String relC2 = c2.substring(0, c2.indexOf("."));
					String partC2 = c2.substring(c2.indexOf(".")+1, c2.indexOf("="));
					
					
					
					if ((relC1.equalsIgnoreCase(relC2) && (!partC1.equals(partC2)))){
						
						cleanedConditions.add(c2);
						conditionCount++;
					}
						
				}else if (c1.contains("\"") && c2.contains("\"")){
					String relC1 = c1.substring(0, c1.indexOf("."));
					String relC2 = c2.substring(0, c2.indexOf("."));
					String partC1 = c1.substring(c1.indexOf(".")+1, c1.indexOf("="));
					String partC2 = c2.substring(c2.indexOf(".")+1, c2.indexOf("="));
					
					if (relC1.equals(relC2) && (!partC1.equals(partC2))){
						cleanedConditions.add(c2);
						conditionCount++;					
					}
					
				}else if ((!c1.contains("\"")) && (!c2.contains("\""))){
					String part1_c1 = c1.substring(0, c1.indexOf("="));
					String part2_c1 = c1.substring(c1.indexOf("=")+1);
					String part1_c2 = c2.substring(0, c2.indexOf("="));
					String part2_c2 = c2.substring(c2.indexOf("=")+1);
					
					String relC1 = c1.substring(c1.indexOf("=")+1, c1.lastIndexOf("."));
					String relC2 = c2.substring(0, c2.indexOf("."));
					
					if (relC1.equals(relC2)){
						if (!(part1_c1.equals(part2_c2) && part2_c1.equals(part1_c2))){
							cleanedConditions.add(c2);
							conditionCount++;
						}
					}
					
				}else if (c1.contains("\"") && !c2.contains("\"")){
					String relC1 = c1.substring(0,c1.indexOf("."));
					String relC2 = c2.substring(0,c2.indexOf("."));
					if (relC1.equals(relC2)){
						cleanedConditions.add(c2);
						conditionCount++;
					}
					
					
				}
				
				
				
			}
		}
		
		
		return cleanedConditions;
		
	}

	
	private ArrayList<String> sortConditions(ArrayList<String> conditions){
		ArrayList<String> sortedConditions = new ArrayList<String>();
		
			
		for (int i = 0; i<conditions.size();i++){

			String s1 = conditions.get(i);
			if (!sortedConditions.contains(s1)){
				sortedConditions.add(s1);	
			}

			int j = 0;	
			while(j<conditions.size()){
				boolean trovato = false;
				String s2 = conditions.get(j);
						String relnS2 = s2.substring(0,s2.indexOf("."));
												
         				if (s1.contains("\"")){
							String relnS1 = s1.substring(0,s1.indexOf("."));
							
							if (relnS1.equalsIgnoreCase(relnS2) && 
									!sortedConditions.contains(s2)){
									sortedConditions.add(s2);	
									trovato = true;
							}
						}else{
							String relnS1 = s1.substring(s1.indexOf("=")+1,s1.lastIndexOf("."));
						
							if (relnS1.equalsIgnoreCase(relnS2) && !sortedConditions.contains(s2)){
								sortedConditions.add(s2);	
								trovato = true;
	
							}
						}
						
						if (trovato){
							s1 = s2;
							j = 0;
						}else{
							j++;
						}
					}			
				}
	
		return cleanConditions(sortedConditions);
		
	}

	public static float getScannedSentences() {
		return scannedSentences;
	}

	private static void setScannedSentences(float scannedSentences) {
		PathsFinder.scannedSentences = scannedSentences;
	}
	
	public static void refreshScannedSentences() {
		scannedSentences = 0f;
	}
	
}
