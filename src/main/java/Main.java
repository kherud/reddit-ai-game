import model.QuestionsEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class Main {
    private static final String PERSISTENCE_UNIT_NAME = "GameUnit";
    private static EntityManagerFactory factory;

    public static void main(String[] args) {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = factory.createEntityManager();
        // read the existing entries and write to console
        Query q = em.createQuery("SELECT q FROM QuestionsEntity q WHERE NOT EXISTS (FROM AnswersEntity a WHERE a.question = q.id AND a.responder = 3) ORDER BY RAND()");
        /*ÜList<QuestionsEntity> todoList = q.getResultList();
        for (QuestionsEntity todo : todoList) {
            System.out.println(todo);
        }
        System.out.println("Size: " + todoList.size());*/
        QuestionsEntity question = (QuestionsEntity) q.getSingleResult();
        System.out.print(question.getQuestion());

        /*em.getTransaction().begin();
        QuestionsEntity todo = new QuestionsEntity();
        todo.setQuestion("This is a test");
        em.persist(todo);
        em.getTransaction().commit();*/

        em.close();
    }
}
