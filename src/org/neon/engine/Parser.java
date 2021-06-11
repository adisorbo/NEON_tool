package org.neon.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neon.model.Result;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class Parser {
	/**
	 */
	private ArrayList<Result> recognized;
	
	private float scannedSentences;
	
	
	
	private LexicalizedParser lp;
	/**
	 */
	private static Parser instance;
	
	
	private ArrayList<String> labelled;
	
	private Parser(){
		lp = LexicalizedParser.loadModel();
	}
	
	
	public static Parser getInstance(){
		if (instance == null){
			instance = new Parser();
		}
		return instance;
	}
	
	
	
	   private String cleanFilesNameAndMethods(String s){
	    	
		    Pattern pattern = Pattern.compile("[a-zA-Z0-9* ]+[.]+[a-z]+");
	    	Matcher matcher = pattern.matcher(s);
	    	String sNew = s;
	    	while (matcher.find()){
	    		
	    		if (matcher.group().contains(".")){
	    			String fileName = matcher.group().replaceAll("\\.", "^");
	    			sNew = sNew.replace(matcher.group(), fileName);
	    		}
	    	}
	    	return sNew;
	    }
	   
	   	private String cleanThreeDot(String s){
	   		Pattern pattern = Pattern.compile("[.]{3}");
	   		Matcher matcher = pattern.matcher(s);
	   		String sNew = s;
	   		while (matcher.find()){
	   			String f = matcher.group().replace("...", ".");
	   			sNew = sNew.replace(matcher.group(), f);
	   			
	   		}
	   		Pattern p2 = Pattern.compile("[?]{2}");
	   		matcher = p2.matcher(sNew);
	   		while (matcher.find()){
	   			String f = matcher.group().replace("??", "?");
	   			sNew = sNew.replace(matcher.group(), f);
	   			
	   		}
	   			   		
	   		return sNew;
	   		
	   	}
	   	
	   	private String[] splitSentences(String s){
	   		
	   		String[] sents = s.split("([\\n]{2}|:|(?=[ ]{2,}[A-Z])|[.][ ]+|\\?\\n|\\.\\n)");
	   		return sents;
	   	}
	
	public ArrayList<Result> extract(String text, File strategy){
		
		labelled = new ArrayList<String>();
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		recognized = new ArrayList<Result>();
		int sentencesCounter = 0;
		String cleansentence = this.cleanFilesNameAndMethods(text);
		cleansentence = this.cleanThreeDot(cleansentence);
			
			
		for (String str: this.splitSentences(cleansentence)){
				Annotation document = new Annotation(str);
				sentencesCounter++;
				pipeline.annotate(document);
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
				
				
				for (CoreMap s: sentences){
				
				String sent = s.get(TextAnnotation.class);
				
	
				try{
					String cleanedSentence = sent.replaceAll("[>]+\n", ".");
					cleanedSentence = cleanedSentence.replaceAll(">", "");
					
					cleanedSentence = cleanedSentence.replaceAll("<", "");
					cleanedSentence = cleanedSentence.replaceAll("_", " ");
					
					// Input sentence parsing with PennTreebank
					Tree parse = lp.parse(cleanedSentence);
					TreebankLanguagePack tlp = new PennTreebankLanguagePack();
					GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
					GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
						
					// Get Stanford dependencies output 
					Collection<TypedDependency> tdl = 
						gs.typedDependenciesCCprocessed();
					boolean interrogative = this.recognizeInterrogativeSentence(parse);
					
					
					Classifier classifier = new Classifier(str, interrogative, tdl, strategy);
					
					
					ArrayList<Result> results = classifier.recognize();
					
					if (results!=null && !results.isEmpty()){
						
						
						for (Result result: results){

							if (!labelled.contains(result.getSentence())){
								labelled.add(result.getSentence());
							}
						
						}
						recognized.addAll(results);
					}
	
				}catch(Exception ex){
					ex.printStackTrace();
	
				}catch(OutOfMemoryError e){
					System.err.println("Warning: sentence too long, trying to split");
					String[] phrases = sent.split("[:;!]");
					for (String phrase: phrases){
						try{
	
							phrase = phrase.trim();   
							if (!phrase.isEmpty()){
								// Input sentence parsing with PennTreebank
								System.err.println("SENTENCE: "+phrase);
								Tree parse = lp.parse(phrase);
								TreebankLanguagePack tlp = new PennTreebankLanguagePack();
								GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
								GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	
								// Get Stanford dependencies output 
								Collection<TypedDependency> tdl = 
									gs.typedDependenciesCCprocessed(true);
								boolean interrogative = this.recognizeInterrogativeSentence(parse);
								Classifier classifier = new Classifier(phrase, interrogative, tdl, strategy);
								ArrayList<Result> results = classifier.recognize();
								
								if (results!=null && !results.isEmpty()){
									for (Result result: results){

										if (!labelled.contains(result.getSentence())){
											labelled.add(result.getSentence());
										}
									
									}

									
									recognized.addAll(results);
								}
							}
						}catch(OutOfMemoryError e1){
							System.err.println("Error: sentence too long");
						}catch(Error err){
							System.err.println("Error in sentence parsing: Sentence skipped");
						}
	
					}
				}catch(Error err){
					System.err.println("Error in sentence parsing: Sentence skipped");
				}
				
				
				
			}
		    float progress = (float) sentencesCounter/(float) this.splitSentences(cleansentence).length;
			setScannedSentences(progress);

				
		}
		
		
		
		
		return recognized;
	
	}
	
	private boolean recognizeInterrogativeSentence(Tree parsedSentence){
		boolean returnValue = false;
		if (parsedSentence.toString().contains("(SBARQ")  ||
				parsedSentence.toString().contains("(SQ")){
			returnValue = true;
		}
		return returnValue;
	}

	
		
   protected ArrayList<Result> getRecognized(){
	   return this.recognized;
   }

   public float getScannedSentences() {
	   return this.scannedSentences;
   }
   
   protected void setScannedSentences(float value) {
	   this.scannedSentences = value;
   }

   public void refreshScannedSentences() {
	   this.scannedSentences = 0f;
   }


}
