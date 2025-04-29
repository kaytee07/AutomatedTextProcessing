package project.automatedtextprocessing.Utils;

public class RegexMatchResult {
    private String matchedText;
    private int startIndex;
    private int endIndex;

    RegexMatchResult(String matchedText, int startIndex, int endIndex) {
        this.matchedText = matchedText;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
