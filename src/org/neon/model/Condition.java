package org.neon.model;

public class Condition {

	private Term fistTerm = new Term();
	private Term lastTerm = new Term();
	private String type;
	private String conditionString;
	private boolean negativeCond;
	
	
	public String getConditionString() {
		return conditionString;
	}
	public void setConditionString(String conditionString) {
		this.conditionString = conditionString;
	}
	
	public String toString() {
		return conditionString;
	}
	
	public Term getFirstTerm() {
		return fistTerm;
	}
	public Term getLastTerm() {
		return lastTerm;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setNegative(boolean negative){
		this.negativeCond = negative;
	}
	
	public boolean isNegative(){
		return this.negativeCond;
	}
	

}
