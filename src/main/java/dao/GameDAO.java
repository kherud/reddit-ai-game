package dao;

import config.Configuration;
import config.GameDifficulty;
import model.AnswersEntity;
import model.PredictionsEntity;
import model.QuestionsEntity;
import model.SubredditsEntity;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.*;
import java.util.List;

/**
 * This is the data access object for managing the JSF game flow.
 */
@Named
@Dependent
public class GameDAO {

    private final EntityManagerFactory factory;

    public GameDAO(){
        factory = Persistence.createEntityManagerFactory(Configuration.PERSISTENCE_UNIT_NAME);
    }

    /**
     * Searches for a random question that was not previously answered by the specified user.
     * @param userId id to select an user for which questions are searched for.
     * @return A list of either one or zero random unanswered questions
     */
    public List getNextQuestion(int userId) {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT q FROM QuestionsEntity q WHERE NOT EXISTS (FROM AnswersEntity a WHERE a.question = q.id AND a.responder = :userid) ORDER BY q.id DESC")
                .setParameter("userid", userId);
        query.setMaxResults(1);
        return query.getResultList();
    }

    /**
     * Determines whether to insert a new or update an existing user answer to a question.
     * If an answer to the question already exists, it is updated instead.
     * @param userId the id belonging to the user that answered.
     * @param question the entity of the question that was answered to.
     * @param answer the given answer.
     * @param difficulty the difficulty at which the question was answered.
     */
    public void persistAnswer(int userId, QuestionsEntity question, String answer, GameDifficulty difficulty){
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("FROM AnswersEntity a WHERE a.question=:questionId AND a.responder = :userId")
                .setParameter("questionId", question.getId())
                .setParameter("userId", userId);
        List<AnswersEntity> result = query.getResultList();
        if (result.isEmpty()){
            insertAnswer(userId, question, answer, difficulty);
        } else {
            AnswersEntity oldAnswer = result.get(0);
            updateAnswer(oldAnswer, answer, difficulty);
        }
    }

    /**
     * Mixes an actual correct answer with multiple wrong ones to give the user a list to choose from.
     * from which the user can take his choice.
     * @param difficulty the difficulty that determines how many wrong options are offered.
     * @param answer the correct answer that is mixed with wrong ones.
     * @return the list of all choices (one correct and multiple wrong answers)
     */
    public List<SubredditsEntity> getChoices(GameDifficulty difficulty, String answer){
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT s FROM SubredditsEntity s WHERE s.name!=:name ORDER BY RAND()").setParameter("name", answer);
        query.setMaxResults(difficulty.getChoices());
        List<SubredditsEntity> wrongAnswers = query.getResultList();
        query = em.createQuery("FROM SubredditsEntity s WHERE s.name=:name").setParameter("name", answer);
        SubredditsEntity correctAnswer = (SubredditsEntity) query.getResultList().get(0);
        wrongAnswers.add(correctAnswer);
        wrongAnswers.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
        return wrongAnswers;
    }

    /**
     * Returns the answer the AI gave to a specific question.
     * @param questionId id of the prediction to the requested question.
     * @return String of AI's prediction
     */
    public String getAIPrediciton(String questionId){
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("FROM PredictionsEntity p WHERE p.question=:questionId").setParameter("questionId", questionId);
        PredictionsEntity answer = (PredictionsEntity) query.getResultList().get(0);
        return answer.getAnswer();
    }

    /**
     * Inserts a new answer to a question given by a user.
     * @param userId id of the user that answered.
     * @param question entity of the question that was answered to.
     * @param answer the answer the user gave.
     * @param difficulty the difficulty at which the question was answered.
     */
    private void insertAnswer(int userId, QuestionsEntity question, String answer, GameDifficulty difficulty) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        AnswersEntity entity = new AnswersEntity();
        entity.setResponder(userId);
        entity.setAnswer(answer);
        entity.setCorrect(answer.equals(question.getAnswer()));
        entity.setQuestion(question.getId());
        entity.setDifficulty(difficulty.getDescription());
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }

    /**
     * Updates an already existing a user gave.
     * @param oldAnswer The entity of the old answer.
     * @param newAnswer The new answer.
     * @param difficulty The difficulty at which the question was now answered.
     */
    private void updateAnswer(AnswersEntity oldAnswer, String newAnswer, GameDifficulty difficulty){
        EntityManager em = factory.createEntityManager();
        em.createQuery("UPDATE AnswersEntity a SET a.answer=:answer, a.difficulty=:difficulty WHERE a.id=:answerId")
                .setParameter("answer", newAnswer)
                .setParameter("difficulty", difficulty)
                .setParameter("answerId", oldAnswer.getId());
        em.close();
    }
}
