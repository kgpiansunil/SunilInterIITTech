package iitkgp.com.test;

import java.util.ArrayList;
import java.util.List;

class ScreenAgri {
    private List<String> mainKeyword;
    private List<String> supportingKeywords = new ArrayList<>();
    private List<String> textToSay;
    private int currentKeyword;
    private int screenid;


    ScreenAgri(List<String> mainKeyword, List<String> supportingKeywords, List<String> textToSay, int screenid) {
        this.mainKeyword = mainKeyword;
        this.supportingKeywords = supportingKeywords;
        this.textToSay = textToSay;
        this.currentKeyword = 0;
        this.screenid = screenid;
    }

    boolean matchScreen(List<String> allKeywords){
        int c = 0;
        for(String keyword : supportingKeywords){
            if(!allKeywords.contains(keyword.toLowerCase())) return false;
        }
        return !allKeywords.contains("next") || !allKeywords.contains("shipping") || supportingKeywords.contains("actual") || !allKeywords.contains("actual");
    }

    String getCurrentMainKeyword(){
        return mainKeyword.get(currentKeyword);
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
}
