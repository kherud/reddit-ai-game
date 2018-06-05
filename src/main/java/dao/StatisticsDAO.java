package dao;

import config.Configuration;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

/**
 * This is the data access object for managing the statistics page.
 */
@Named
@Dependent
public class StatisticsDAO {

    private final EntityManagerFactory factory;

    public StatisticsDAO() {
        factory = Persistence.createEntityManagerFactory(Configuration.PERSISTENCE_UNIT_NAME);
    }

    /**
     * Returns a String that represents the prediction success rate of the AI
     * If the AI did not make any predicitons so far "n/a" is returned.
     * Otherwise the success rate in percent is returned.
     * @return String representing the success rate.
     */
    public String getAIRate() {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT AVG(p.correct) FROM PredictionsEntity p");
        List result = query.getResultList();
        if (result.isEmpty() || result.get(0) == null) { // second condition won't be evaluated if the first one is true
            return "n/a";
        } else {
            return String.format("%.1f%%", (Double) result.get(0) * 100);
        }
    }

    /**
     * Returns a String that represents the rate of correct answers of a user at a specified difficulty.
     * If the user did not give any answers at that difficulty so far "n/a" is returned.
     * Otherwise the success rate in percent is returned.
     * @param userId id of the user.
     * @param difficulty the difficulty of the questions for which the rate is calculated.
     * @return String representing the success rate.
     */
    public String getPlayerRate(int userId, String difficulty) {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT AVG(a.correct) FROM AnswersEntity a WHERE a.responder=:userId AND a.difficulty=:difficulty")
                .setParameter("userId", userId)
                .setParameter("difficulty", difficulty);
        List result = query.getResultList();
        if (result.isEmpty() || result.get(0) == null) { // second condition won't be evaluated if the first one is true
            return "n/a";
        } else {
            return String.format("%.1f%%", (Double) result.get(0) * 100);
        }
    }

    /**
     * Count all answers the AI gave.
     * @return Amount of answers.
     */
    public Long countAIGames() {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT COUNT(*) FROM PredictionsEntity");
        return (Long) query.getResultList().get(0);
    }

    /**
     * Count all answers the user gave at an specified difficulty.
     * @param userId id of the user.
     * @param difficulty the difficulty at which the answers are counted.
     * @return Amount of answers at the difficulty.
     */
    public Long countPlayerGames(int userId, String difficulty) {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT COUNT(*) FROM AnswersEntity a WHERE a.responder=:userId AND a.difficulty=:difficulty")
                .setParameter("userId", userId)
                .setParameter("difficulty", difficulty);
        return (Long) query.getResultList().get(0);
    }
}
