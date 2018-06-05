package schedule;

import config.Configuration;
import model.QuestionsEntity;
import model.SubredditsEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class QuestionUpdater {
    private static final Logger log = Logger.getLogger(QuestionUpdater.class.getName());
    private final EntityManagerFactory factory;
    // 100 is the limit for one request, set by reddit's spam policy
    private final String URL_TEMPLATE = "https://www.reddit.com/r/%s/%s/.json?limit=100";

    /**
     * This class is used to search for new submissions / questions from the official website www.reddit.com
     */
    public QuestionUpdater() {
        factory = Persistence.createEntityManagerFactory(Configuration.PERSISTENCE_UNIT_NAME);
    }

    /**
     * Meta method, constructs query url, queries web site, parses response and inserts new content into the database.
     */
    public void updateQuestions() {
        String joinedSubreddits = getSubredditTargetString();
        String url = String.format(URL_TEMPLATE, joinedSubreddits, Configuration.REDDIT_SORT_BY);
        log.info("Querying " + url);
        HttpURLConnection request;
        try {
            request = getConnection(url);
            JSONArray submissions = parseResponse(request);
            submissions.forEach(x -> updateSubmission((JSONObject) x));
        } catch (IOException | ParseException e) {
            log.warning("HTTP Connection to Reddit could not be established or response was invalid. Exiting.");
            System.exit(1);
        }
    }

    /**
     * Concatenates available subreddits to a single String in order to query the content from the website
     * @return single String with concatenated subreddits.
     */
    private String getSubredditTargetString(){
        EntityManager em = factory.createEntityManager();
        Query q = em.createQuery("FROM SubredditsEntity");
        List<SubredditsEntity> subreddits = q.getResultList();
        em.close();
        if (subreddits.size() == 0) {
            log.warning("No subreddits could be found - fatal error. Exiting.");
            System.exit(1);
            return "all"; // descend to meta feed of reddit
        } else {
            return subreddits.stream().map(SubredditsEntity::getName).collect(Collectors.joining("+"));
        }
    }

    /**
     * Makes the HTTP request and sets header (User-agent) to avoid the spam policy of reddit.
     * @param url The url that is requested.
     * @return Ready request object.
     * @throws IOException
     */
    private HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
        request.setRequestProperty("User-agent", "pb_frex");
        request.connect();
        return request;
    }

    /**
     * Parses the JSON response from a HTTP request.
     * @param request Before-hand prepared request.
     * @return Parsed JSONArray with all submission JSON objects.
     * @throws IOException
     * @throws ParseException
     */
    private JSONArray parseResponse(HttpURLConnection request) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject redditJSON = (JSONObject) parser.parse(new InputStreamReader((InputStream) request.getContent()));
        JSONObject jsonData = (JSONObject) redditJSON.get("data");
        return (JSONArray) jsonData.get("children");
    }

    /**
     * Inserts a new submission into the database, if it does not already exist.
     * @param submission JSON data containing the question.
     */
    private void updateSubmission(JSONObject submission){
        JSONObject submissionData = (JSONObject) submission.get("data");
        String submissionID = submissionData.get("id").toString();
        String submissionTitle = submissionData.get("title").toString();
        String submissionSubreddit = submissionData.get("subreddit").toString();
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT COUNT(*) FROM QuestionsEntity WHERE id=:id").setParameter("id", submissionID);;
        long count = (long) query.getSingleResult();
        if (count == 0) {
            log.info("Inserting new Question from submission: " + submissionID);
            em.getTransaction().begin();
            QuestionsEntity entity = new QuestionsEntity();
            entity.setId(submissionID);
            entity.setQuestion(submissionTitle);
            entity.setAnswer(submissionSubreddit);
            em.persist(entity);
            em.getTransaction().commit();
        }
        em.close();
    }
}
