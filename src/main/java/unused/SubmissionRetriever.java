package unused;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubmissionRetriever {

    private final String URL_TEMPLATE = "https://www.reddit.com/r/%s/%s/.json-compact";
    private final String ERROR_TEMPLATE = "<span>Unfortunately, the original submission could not be loaded.</span><br>" +
            "<span>Correct Answer</span><br>" +
            "<span>%s</span>";

    public SubmissionRetriever(){

    }

    public String retrieveSubmission(String subreddit, String submissionId){
        HttpURLConnection request;
        try {
            request = getConnection(subreddit, submissionId);
            return parseResponse(request);
        } catch (IOException | ParseException e) {
            return String.format(ERROR_TEMPLATE, subreddit);
        }
    }

    private HttpURLConnection getConnection(String subreddit, String submissionId) throws IOException {
        String url = String.format(URL_TEMPLATE, subreddit, submissionId);
        HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
        request.setRequestProperty("User-agent", "pb_frex");
        request.connect();
        return request;
    }

    private String parseResponse(HttpURLConnection request) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray contentArray = (JSONArray) parser.parse(new InputStreamReader((InputStream) request.getContent()));
        JSONObject contentJSON = (JSONObject) contentArray.get(0);
        JSONArray contentDataArray = (JSONArray) contentJSON.get("data");
        JSONObject submissionJSON = (JSONObject) contentDataArray.get(0);
        JSONObject submissionData = (JSONObject) submissionJSON.get("data");
        return submissionData.get("content").toString();
    }
}
