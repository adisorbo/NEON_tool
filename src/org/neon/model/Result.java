package org.neon.model;


public class Result {

	private String heuristic;

	private int classification;

	private String sentence;

	private String message;

	private String category;
	

	
	public Result(){
		
	}
	
	public Result(String heuristic, int classification){
		this.heuristic = heuristic;
		this.classification = classification;
	}
	
	
	
	public String getSentence() {
		return sentence;
	}



	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getHeuristic() {
		return heuristic;
	}
	
	public void setHeuristic(String heuristic) {
		this.heuristic = heuristic;
	}
	
	public int getClassification() {
		return classification;
	}
		
	public void setClassification(int classification) {
		this.classification = classification;
	}
	
	public String getSentenceClass(){
		return category;
	}
	
	public void setSentenceClass(String category){
		this.category = category;
	}
}
