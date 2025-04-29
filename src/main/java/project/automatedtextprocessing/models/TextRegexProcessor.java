package project.automatedtextprocessing.models;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TextRegexProcessor {
    private Pattern patternObj;

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

    }

}
