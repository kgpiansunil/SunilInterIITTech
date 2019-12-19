package iitkgp.com.test;

import java.util.ArrayList;
import java.util.List;

class MainKeyword {
	private String keyword; //actual keyword
	private String supportingKeyword; //supports in finding main keyword
	private List<String> alternateKeyword; //in case the main keyword is not found

	MainKeyword(String keyword, String supportingKeyword, List<String> alternateKeyword) {
		this.keyword = keyword;
		this.supportingKeyword = supportingKeyword;
		this.alternateKeyword = alternateKeyword;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getSupportingKeyword() {
		return supportingKeyword;
	}

	public List<String> getAlternateKeyword() {
		return alternateKeyword;
	}
}

class Screen {
	private List<MainKeyword> mainKeyword;
	private List<String> supportingKeywords = new ArrayList<>();
	private List<String> textToSay;
	private int currentKeyword;
	private int screenid;
	private int matchKeywords;
	private int gesture; //0: touch, 1: scroll right
	private Screen basescreen;

	
	Screen(Screen basescreen, List<MainKeyword> mainKeyword, List<String> supportingKeywords, int matchKeywords, int gesture, List<String> textToSay, int screenid) {
		this.mainKeyword = mainKeyword;
		this.supportingKeywords = supportingKeywords;
		this.matchKeywords = matchKeywords;
		this.textToSay = textToSay;
		this.currentKeyword = 0;
		this.screenid = screenid;
		this.gesture = gesture;
		this.basescreen = basescreen;
	}

	boolean matchScreen(List<String> allKeywords){
		int c = 0;
		for(String keyword : supportingKeywords){
			if(allKeywords.contains(keyword.toLowerCase())) c+=1;
		}
		if (c>=matchKeywords) return true;
		return false;
	}
	
	int getCurrentMainKeywordIndex(List<String> allKeywords){
		MainKeyword currentMainKeyword = mainKeyword.get(currentKeyword);
		if (!allKeywords.contains(currentMainKeyword.getKeyword())) {
			int c=0;
			for (String keyword: allKeywords) {
				for (String altKeyword: currentMainKeyword.getAlternateKeyword()) {
					if (keyword.toLowerCase().equals(altKeyword.toLowerCase())) return c;
				}
				c++;
			}
		}
		if (currentMainKeyword.getSupportingKeyword().equals("")) {
			return allKeywords.indexOf(currentMainKeyword.getKeyword().toLowerCase());
		}
		for (int i=0;i<allKeywords.size()-1;i++) {
			if ((allKeywords.get(i).toLowerCase().equals(currentMainKeyword.getKeyword().toLowerCase()) && allKeywords.get(i+1).toLowerCase().contains(currentMainKeyword.getSupportingKeyword().toLowerCase())) || i<=allKeywords.size()-1 && (allKeywords.get(i).toLowerCase().equals(currentMainKeyword.getKeyword().toLowerCase()) && allKeywords.get(i+2).toLowerCase().contains(currentMainKeyword.getSupportingKeyword().toLowerCase()))) {
				return i;
			}
			if (i<=allKeywords.size()-2 && (allKeywords.get(i).toLowerCase().equals(currentMainKeyword.getKeyword().toLowerCase()) && allKeywords.get(i+3).toLowerCase().contains(currentMainKeyword.getSupportingKeyword().toLowerCase()))) {
				return i;
			}
		}
		for (int i=0;i<allKeywords.size()-1;i++) {
			if (allKeywords.get(i).toLowerCase().contains(currentMainKeyword.getSupportingKeyword().toLowerCase()) && allKeywords.get(i+1).toLowerCase().equals(currentMainKeyword.getKeyword().toLowerCase())) {
				return i+1;
			}
			else if ( i<=allKeywords.size()-1 && (allKeywords.get(i).toLowerCase().contains(currentMainKeyword.getSupportingKeyword().toLowerCase()) && allKeywords.get(i+2).toLowerCase().equals(currentMainKeyword.getKeyword().toLowerCase()))) {
				return i+2;
			}
			else if (i<=allKeywords.size()-2 && (allKeywords.get(i).toLowerCase().contains(currentMainKeyword.getSupportingKeyword().toLowerCase()) && allKeywords.get(i+3).toLowerCase().equals(currentMainKeyword.getKeyword().toLowerCase()))) {
				return i+3;
			}
		}
		return -1;
	}
	
	
	String getTextToSay() {
		return textToSay.get(currentKeyword);
	}
	
	void incrementCurrentKeyword(){
		currentKeyword = (currentKeyword+1)%mainKeyword.size();
	}
	
	void resetCurrentKeyword(){
		currentKeyword = 0;
	}
	
	public int getScreenid() {
		return screenid;
	}

	public int getGesture() {
		return gesture;
	}

	public Screen getBasescreen() {
		return basescreen;
	}
}

