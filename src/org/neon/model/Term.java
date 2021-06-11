package org.neon.model;


	public class Term {
		private String td_type;
		private String child_type;
		private String tokens;
		private boolean tokensType;
		
		public String getTd_type() {
			return td_type;
		}
		public void setTd_type(String td_type) {
			this.td_type = td_type;
		}
		public String getChild_type() {
			return child_type;
		}
		public void setChild_type(String child_type) {
			if (child_type.equalsIgnoreCase("governor")){
				this.child_type = "gov()";	
			}else{
				this.child_type = "dep()";
			}
			
			
		}
		public String getTokens() {
			return tokens;
		}
		public void setTokens(String tokens) {
			this.tokens = " "+tokens+" ";
		}
		public boolean isTokensType() {
			return tokensType;
		}
		public void setTokensType(boolean tokensType) {
			this.tokensType = tokensType;
		}
	
}


