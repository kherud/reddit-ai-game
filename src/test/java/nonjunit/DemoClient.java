package nonjunit;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tensorflow.*;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

/**
 * This class demonstrates the process of retrieving new questions from the official website www.reddit.com,
 * and subsequently utilizing our pre-trained Artificial Intelligence to answer them.
 */
public class DemoClient {
    private static final int SEQUENCE_LENGTH = 128; // input sequence length of the AI model

    /**
     * Returns the index of the largest float value in an array.
     * Used for mapping prediction vectors to class affiliations.
     * @param vector array to be looked into
     * @return array index of biggest value
     */
    private static int argmax(float[] vector){
        float max = -1;
        int indexMax = 0;
        for (int i = 0; i < vector.length; i++){
            if (vector[i] > max) {
                max = vector[i];
                indexMax = i;
            }
        }
        return indexMax;
    }

    public static void main(String[] args) throws Exception {
        JSONParser parser = new JSONParser();

        // load the vocabulary data that was created when the RNN model was trained
        String path = parser.getClass().getClassLoader().getResource("ai/vocabulary.json").getFile();
        JSONObject vocabularyJSON = (JSONObject) parser.parse(new FileReader(path));
        JSONArray subreddits = (JSONArray) vocabularyJSON.get("labels");
        JSONObject vocabulary = (JSONObject) vocabularyJSON.get("vocabulary");

        // retrieve new reddit submissions from the official website
        String joinedSubreddits = (String) subreddits.stream().collect(Collectors.joining("+"));
        String url = "https://www.reddit.com/r/{}/.json?limit=100".replace("{}", joinedSubreddits);
        HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
        // properly set header to avoid spam policy of reddit
        request.setRequestProperty("User-agent", "pb_frex");
        request.connect();
        JSONObject redditJSON = (JSONObject) parser.parse(new InputStreamReader((InputStream) request.getContent()));
        JSONObject jsonData = (JSONObject) redditJSON.get("data");
        JSONArray submissions = (JSONArray) jsonData.get("children");

        final int NUM_PREDICTIONS = submissions.size();

        // mathematical input array for model
        float[] modelInputs = new float[NUM_PREDICTIONS * SEQUENCE_LENGTH];

        // use the vocabulary data to map characters to integers (floats)
        for (int i = 0; i < submissions.size(); i++) {
            Object record = submissions.get(i);
            JSONObject submission = (JSONObject) record;
            JSONObject submissionData = (JSONObject) submission.get("data");
            String title = submissionData.get("title").toString();
            // Only consider n chars -- automatically pad array with zeroes if length is below sequence length
            // Also ignore unknown chars by using a zero
            for (int j = 0; j < (title.length() > SEQUENCE_LENGTH ? SEQUENCE_LENGTH : title.length()); j++) {
                String charAt = String.valueOf(title.charAt(j));
                if (vocabulary.containsKey(charAt)){
                    modelInputs[i * SEQUENCE_LENGTH + j] = (long) vocabulary.get(charAt);
                }
            }
        }

        // load frozen model and session
        String directory = parser.getClass().getClassLoader().getResource("ai/model").getPath();
        directory = (directory.charAt(0) == '/') ? directory.substring(1) : directory; // necessary fix (windows)
        SavedModelBundle b = SavedModelBundle.load(directory, "serve"); // load the recurrent neural network (RNN)
        Session sess = b.session();

        // create an input tensor for the model
        Tensor x = Tensor.create(
                new long[]{NUM_PREDICTIONS, SEQUENCE_LENGTH},
                FloatBuffer.wrap(modelInputs)
        );

        float[][] predictions = new float[NUM_PREDICTIONS][subreddits.size()];

        // run prediction
        sess.runner()
                .feed("embedding_1_input", x) // input layer
                .fetch("y/Sigmoid") // output layer
                .run()
                .get(0)
                .copyTo(predictions);

        // print out the results
        int correct = 0;

        for (int i = 0; i < predictions.length; i++) {
            Object record = submissions.get(i);
            JSONObject submission = (JSONObject) record;
            JSONObject submissionData = (JSONObject) submission.get("data");
            int predictionIndex = argmax(predictions[i]);
            String prediction = (String) subreddits.get(predictionIndex);
            if (prediction.equals(submissionData.get("subreddit"))) correct++;
            System.out.print((prediction.equals(submissionData.get("subreddit")) ? "CORRECT" : "FALSE"));
            System.out.println(" # " + submissionData.get("title"));
        }
        System.out.println(correct + " correct out of " + predictions.length);
    }
}
