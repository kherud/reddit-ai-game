package bean;

import config.Configuration;
import schedule.QuestionPredicter;
import schedule.QuestionUpdater;
import schedule.SubredditUpdater;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Singleton
@Startup
public class QuestionFetchingBean {

    private static final Logger log = Logger.getLogger(QuestionFetchingBean.class.getName());

    /**
     * This method constructs a single scheduler upon application start.
     * The scheduler then updates and answers new question by the AI appropriate to the configured time interval.
     */
    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            updateSubreddits();
            fetchQuestions();
            predictQuestions();
        }, 0, Configuration.UPDATE_INTERVAL, TimeUnit.MINUTES);
    }

    /**
     * Updates all available subreddits.
     */
    private void updateSubreddits(){
        log.info("Updating relevant subreddits.");
        SubredditUpdater subredditUpdater = new SubredditUpdater();
        subredditUpdater.updateSubreddits();
    }

    /**
     * Loads new question content from the official website.
     */
    private void fetchQuestions(){
        log.info("Fetching new questions.");
        QuestionUpdater questionUpdater = new QuestionUpdater();
        questionUpdater.updateQuestions();
    }

    /**
     * Uses AI to predict the answers of the questions that were received before hand.
     */
    private void predictQuestions(){
        log.info("Answering new questions by AI.");
        QuestionPredicter AI = new QuestionPredicter();
        AI.predictQuestions();
    }

}
