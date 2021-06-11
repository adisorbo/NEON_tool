package org.neon.pathsFinder.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neon.pathsFinder.model.Sentence;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class Parser {
		
	private static Parser instance;
    private LexicalizedParser lp;
	
    private float progress;
    
    
	private Parser(){
		lp = LexicalizedParser.loadModel();
	}
	
	
	
	
	
	public static Parser getInstance(){
		if (instance == null){
			instance = new Parser();
		}
		return instance;
	}
	

	public ArrayList<Sentence> parse(String text) {
		ArrayList<Sentence> parsedSentences = new ArrayList<Sentence>();
		
		this.refreshProgress();
		 
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		
		int sentencesCounter = 0;
		
		String[] strings = this.splitSentences(text);
		
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		int i = 1;
		for (String str: strings){
			sentencesCounter++;
			String sentence = this.cleanFilesNameAndMethods(str);
			sentence = this.cleanThreeDot(sentence);
			sentence = sentence.replaceAll("_", " ");
			sentence = sentence.replaceAll(">", " ");
			Annotation document = new Annotation(sentence);
			
			
			pipeline.annotate(document);
			
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			
			for (CoreMap s: sentences){
				
				
				String sent = s.get(TextAnnotation.class);
				Tree parse = lp.parse(sent);
				
				
				boolean interrogative = this.recognizeInterrogativeSentence(parse);
				
				
				SemanticGraph graph = SemanticGraphFactory.generateCCProcessedDependencies(parse);
				

				
				
				Sentence newSentence = new Sentence(sent, graph);
				newSentence.setInterrogative(interrogative);
				
				parsedSentences.add(newSentence);
				
				
				
			    

			}
			float progress = ((float) sentencesCounter / (float) strings.length)*50f;
			
			this.setProgress(progress);
		}
		
		
		
		
		return parsedSentences;
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
	   	
	   	
		private boolean recognizeInterrogativeSentence(Tree parsedSentence){
			boolean returnValue = false;
			if (parsedSentence.toString().contains("(SBARQ")  ||
					parsedSentence.toString().contains("(SQ")){
				returnValue = true;
			}
			return returnValue;
		}

	   	
	   	private String[] splitSentences(String s){
	   		String[] sents = s.split("(\\n|:)");
	   		return sents;
	   	}





		public float getProgress() {
			return progress;
		}



    	protected void setProgress(float progress) {
			this.progress = progress;
		}
	   	
    	public void refreshProgress() {
    		this.progress = 0;
    	}
	   	
	   	
	   	
}
