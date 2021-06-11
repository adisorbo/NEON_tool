package org.neon.model;

import java.util.ArrayList;

public class Heuristic {
	
	private String sentence_type;
	private String type;
	private String text;
	private ArrayList<Condition> conditions;
	private String sentence_class;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public ArrayList<Condition> getConditions() {
		return conditions;
	}
	public void setConditions(ArrayList<Condition> conditions) {
		this.conditions = conditions;
	}
	public String getSentence_class() {
		return sentence_class;
	}
	public void setSentence_class(String sentence_class) {
		this.sentence_class = sentence_class;
	}
	public String getSentence_type() {
		return sentence_type;
	}
	public void setSentence_type(String sentence_type) {
		this.sentence_type = sentence_type;
	}
	
	
	
	
}
