package bean;

import config.GameDifficulty;
import dao.GameDAO;
import model.QuestionsEntity;
import model.SubredditsEntity;
import unused.SubmissionRetriever;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * This class manages the JSF flow that is used for the game.
 */
@Named
@FlowScoped("game")
public class GameBean extends SessionBean {

    private static final long serialVersionUID = 1L;

    @Inject
    transient private GameDAO gameDAO;

    private QuestionsEntity question;
    private String answer;
    private String prediction;
    private GameDifficulty difficulty = GameDifficulty.NORMAL;

    /**
     * This method is used to retrieve a question when the JSF flow is entered.
     */
    @PostConstruct
    public void init(){
        nextQuestion();
    }

    /**
     * UNUSED
     */
    public String retrieveSubmission(){
        SubmissionRetriever submissionRetriever = new SubmissionRetriever();
        String subreddit = question.getAnswer();
        String submissionId = question.getId();
        return submissionRetriever.retrieveSubmission(subreddit, submissionId);
    }

    /**
     * Save the selected answer from the first page of the JSF flow and return the next page.
     * @return JSF String leading to the next page
     */
    public String selectAnswer(){
        this.answer = getVariable("choice");
        int userId = getUserId();
        gameDAO.persistAnswer(userId, question, answer, difficulty);
        return "prediction";
    }

    /**
     * Load answering possibilities for a question from the database.
     * @return List of Subreddit entities.
     */
    public List<SubredditsEntity> getChoices(){
        String answer = question.getAnswer();
        return gameDAO.getChoices(difficulty, answer);
    }

    /**
     * Load the answer of the AI from the database.
     * @return answer string
     */
    public String getAIPrediction(){
        String questionId = question.getId();
        prediction = gameDAO.getAIPrediciton(questionId);
        return prediction;
    }

    public QuestionsEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionsEntity question) {
        this.question = question;
    }

    public String getDifficulty() {
        return difficulty.getDescription();
    }

    /**
     * Maps an incoming difficulty to its appropriate enumerator.
     * Default: Normal difficulty.
     * @param difficulty Form value
     */
    public void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "Easy": this.difficulty = GameDifficulty.EASY; break;
            case "Hard": this.difficulty = GameDifficulty.HARD; break;
            default: this.difficulty = GameDifficulty.NORMAL;
        }
    }

    /**
     * Retrieve a new question from the database
     * and call the id to reenter the JSF flow.
     * @return JSF flow ID
     */
    public String nextQuestion(){
        loadNextQuestion();
        return "nextQuestion";
    }

    /**
     * Tries to retrieve a new question from the database.
     * If none is available JSF recognizes the null reference to display an error message.
     */
    private void loadNextQuestion(){
        int userId = getUserId();
        List result = gameDAO.getNextQuestion(userId);
        if(!result.isEmpty()){
            question = null; // (QuestionsEntity) result.get(0);
        } else {
            question = null;
        }
    }

    /**
     * Gets a String for the frontend to represent whether an answer was correct.
     * @param answer Given answer that is compared with the answer of the question.
     * @return String representing the resulting score.
     */
    public String getScore(String answer){
        return answer.equals(question.getAnswer()) ? "+1" : "0";
    }

    public String getAnswer(){
        return answer;
    }

    public String getPrediction(){
        return prediction;
    }

    /**
     * Destroys the current authentication session
     * and calls an id to leave the JSF flow.
     * @return JSF flow id.
     */
    public String leaveFlow(){
        logout();
        return "exitGame";
    }
}
