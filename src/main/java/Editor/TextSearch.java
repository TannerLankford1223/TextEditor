package Editor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextSearch extends SwingWorker<List<MatchedResult>, Void> {

    private final String searchPattern;
    private final String text;

    private List<MatchedResult> matchedResults;

    public TextSearch(String searchPattern, String text) {
        this.searchPattern = searchPattern;
        this.text = text;
        this.matchedResults = new ArrayList<>();
    }

    @Override
    protected List<MatchedResult> doInBackground() throws Exception {
        List<MatchedResult> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchPattern);
        Matcher matcher = pattern.matcher(text);


        while(matcher.find()) {
            results.add(new MatchedResult(matcher.start(), matcher.end(), matcher.group()));
        }

        return results;
    }
}
