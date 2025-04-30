package project.automatedtextprocessing.Utils;

public class RegexMatchResult {
    private final String matchedText;
    private final int startIndex;
    private final int endIndex;

    public RegexMatchResult(String matchedText, int startIndex, int endIndex) {
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
