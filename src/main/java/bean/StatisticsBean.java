package bean;

import dao.StatisticsDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class is used for managing the Statistics page.
 */
@Named
@RequestScoped
public class StatisticsBean extends SessionBean {

    private static final long serialVersionUID = 1L;

    @Inject
    transient private StatisticsDAO statisticsDAO;

    /**
     * Return a String that represents the rate of success of the AI for answering questions correctly.
     * @return String representing the success rate.
     */
    public String getAIRate(){
        return statisticsDAO.getAIRate();
    }

    /**
     * Return a String that represents the rate of success of the user for answering question of a given difficulty correctly.
     * @param difficulty The difficulty of the requested answers.
     * @return String representing success rate at the given difficulty.
     */
    public String getPlayerRate(String difficulty){
        int userId = getUserId();
        return statisticsDAO.getPlayerRate(userId, difficulty);
    }

    /**
     * Count all answers the AI has given.
     * @return Amount of answers.
     */
    public Long countAIGames(){
        return statisticsDAO.countAIGames();
    }

    /**
     * Count all answers the user has given at a given difficulty.
     * @param difficulty The requested game difficulty.
     * @return Amount of answers at specified game difficulty.
     */
    public Long countPlayerGames(String difficulty){
        int userId = getUserId();
        return statisticsDAO.countPlayerGames(userId, difficulty);
    }
}
