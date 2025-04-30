package project.automatedtextprocessing.models;

import project.automatedtextprocessing.Utils.RegexMatchResult;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TextRegexProcessor {
    private Pattern patternObj;
    ArrayList<RegexMatchResult> results;
    int matchedWordIndex = 0;

    public void  compileRegexPattern(String pattern){
        try {
            patternObj = Pattern.compile(pattern);
        } catch ( PatternSyntaxException e){
            throw new PatternSyntaxException(e.getMessage(),pattern,0);
        }
    }

    public void compileTextPattern(String pattern){
        patternObj = Pattern.compile(pattern, Pattern.LITERAL);
    }

    public ArrayList<RegexMatchResult> getAllMatches(String Input){
        if (patternObj == null) {
            throw new IllegalStateException("Regex pattern is null: call compileTextPattern or compileRegexPattern");
        }
        results = new ArrayList<>();
        matchedWordIndex = 0;
        Matcher matcher = patternObj.matcher(Input);
        while (matcher.find()){
            results.add(new RegexMatchResult(matcher.group(), matcher.start(), matcher.end()));
        }
        return results;
    }

    public RegexMatchResult getMatchResult(){
        if (results == null){
            return null;
        }
        return results.get(matchedWordIndex);
    }

    public void nextMatchIndex(){
        if (matchedWordIndex < results.size()){
            matchedWordIndex++;
        }
    }

    public  void prevMatchIndex(){
        if (matchedWordIndex > 0){
            matchedWordIndex--;
        }
    }


    public String replaceAllMatches(String input, String replacement){
        if (patternObj == null) {
            throw new IllegalStateException("Regex pattern is null: call compileTextPattern or compileRegexPattern");
        }
        return patternObj.matcher(input).replaceAll(replacement);
    }

    public String replaceAMatch(String input, String replacement){
        if (results == null || results.isEmpty()){
            return input;
        }
        RegexMatchResult matchResult = getMatchResult();
        StringBuilder result = new StringBuilder();
        result.append(input.substring(0, matchResult.getStartIndex()));
        result.append(replacement);
        result.append(input.substring(matchResult.getEndIndex() ));

        return  result.toString();
    }


}
