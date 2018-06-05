package schedule;

import config.Configuration;
import model.SubredditsEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class SubredditUpdater {
    private static final Logger log = Logger.getLogger(SubredditUpdater.class.getName());
    private final EntityManagerFactory factory;

    /**
     * This class is used to update all available subreddits by parsing them from the local JSON file.
     */
    public SubredditUpdater(){
        factory = Persistence.createEntityManagerFactory(Configuration.PERSISTENCE_UNIT_NAME);
    }

    /**
     * Meta method, loads the local JSON file and updates each entry.
     */
    public void updateSubreddits(){
        JSONParser parser = new JSONParser();
        try {
            String path = this.getClass().getClassLoader().getResource("ai/vocabulary.json").getFile();
            JSONObject vocabularyJSON = (JSONObject) parser.parse(new FileReader(path));
            JSONArray subreddits = (JSONArray) vocabularyJSON.get("labels");
            subreddits.stream().forEach(x -> updateSubreddit(x.toString()));
        } catch (ParseException e) {
            log.warning("Vocabulary file is corrupted. Exiting.");
            e.printStackTrace();
        } catch (IOException e) {
            log.warning("Vocabulary file could not be found or read.");
            e.printStackTrace();
        }
    }

    /**
     * Inserts a new subreddit to the database, if it does not exist already.
     * @param subreddit name of subreddit to be inserted.
     */
    private void updateSubreddit(String subreddit){
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT COUNT(*) FROM SubredditsEntity WHERE name=:name").setParameter("name", subreddit);
        long count = (long) query.getSingleResult();
        if (count == 0) {
            log.info("Inserting new Subreddit: " + subreddit);
            em.getTransaction().begin();
            SubredditsEntity entity = new SubredditsEntity();
            entity.setName(subreddit);
            em.persist(entity);
            em.getTransaction().commit();
        }
        em.close();
    }
}
