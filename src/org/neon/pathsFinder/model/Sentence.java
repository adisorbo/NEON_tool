package org.neon.pathsFinder.model;



import edu.stanford.nlp.semgraph.SemanticGraph;

public class Sentence {
	private String text;
	private SemanticGraph graph;
	private boolean interrogative;
	
	public Sentence(String text, SemanticGraph graph){
		this.text = text;
		this.graph = graph;
		
	}
	
	

	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public SemanticGraph getGraph() {
		return graph;
	}



	public void setGraph(SemanticGraph graph) {
		this.graph = graph;
	}



	public boolean isInterrogative() {
		return interrogative;
	}



	public void setInterrogative(boolean interrogative) {
		this.interrogative = interrogative;
	}
	

}
