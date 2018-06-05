package schedule;

import config.Configuration;
import model.PredictionsEntity;
import model.QuestionsEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlowException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.logging.Logger;

import static config.Configuration.SEQUENCE_LENGTH;

public class QuestionPredicter {
    private static final Logger log = Logger.getLogger(QuestionPredicter.class.getName());
    private final EntityManagerFactory factory;
    private JSONObject vocabulary;
    private JSONArray classTargets;

    /**
     * This class is used for activating the AI, thus answering all new questions.
     * Since this process is crucial to the application, any error will shut the web server down.
     */
    public QuestionPredicter() {
        factory = Persistence.createEntityManagerFactory(Configuration.PERSISTENCE_UNIT_NAME);
    }

    /**
     * Meta method, searches for new questions, loads vocabulary, encodes questions,
     * predicts and inserts answers
     */
    public void predictQuestions() {
        List questions = getQuestions();
        if (questions.size() > 0) {
            getVocabulary();
            float[] encodedQuestions = encodeQuestions(questions);
            float[][] predictions = getPredictions(encodedQuestions);
            for (int i = 0; i < questions.size(); i++) {
                String prediction = classTargets.get(argmax(predictions[i])).toString();
                insertAnswer((QuestionsEntity) questions.get(i), prediction);
            }
        }
    }

    /**
     * Propagates a vector through our RNN (AI)
     * First, unfreezes computation graph and session.
     * Then, creates and input tensor and feeds it to the model.
     *
     * @param modelInputs questions that were encoded with the vocabulary
     * @return raw RNN output data (float vector with class affiliation probabilities for each input)
     */
    private float[][] getPredictions(float[] modelInputs) {
        String path = this.getClass().getClassLoader().getResource("ai/model").getFile();
        path = (path.charAt(0) == '/') ? path.substring(1) : path; // necessary fix
        try (SavedModelBundle b = SavedModelBundle.load(path, "serve")) {
            Session sess = b.session();
            int inputSize = modelInputs.length / SEQUENCE_LENGTH;
            Tensor x = Tensor.create(
                    new long[]{inputSize, SEQUENCE_LENGTH},
                    FloatBuffer.wrap(modelInputs)
            );
            long amountOfClasses = classTargets.size();
            float[][] result = new float[inputSize][(int) amountOfClasses];
            sess.runner()
                    .feed("embedding_1_input", x) // input layer
                    .fetch("y/Sigmoid") // output layer
                    .run()
                    .get(0)
                    .copyTo(result);
            return result;
        } catch (TensorFlowException e) {
            log.warning("AI model could not be loaded - Fatal. Exiting.");
            System.exit(1);
        }
        return null;
    }

    /**
     * Uses the vocabulary data to map / encode characters to numbers.
     *
     * @param questions List of question entities to encode
     * @return 1d float vector with all encoded questions concatenated.
     */
    private float[] encodeQuestions(List questions) {
        final int questionsSize = questions.size();
        float[] encodedQuestions = new float[questionsSize * SEQUENCE_LENGTH];
        for (int i = 0; i < questionsSize; i++) {
            QuestionsEntity question = (QuestionsEntity) questions.get(i);
            String title = question.getQuestion();
            // Only consider n chars -- automatically pad array with zeroes if length is below sequence length
            // Also ignore unknown chars by using a zero
            for (int j = 0; j < (title.length() > SEQUENCE_LENGTH ? SEQUENCE_LENGTH : title.length()); j++) {
                String charAt = String.valueOf(title.charAt(j));
                if (vocabulary.containsKey(charAt)) {
                    encodedQuestions[i * SEQUENCE_LENGTH + j] = (long) vocabulary.get(charAt);
                }
            }
        }
        return encodedQuestions;
    }

    /**
     * Reads the vocabulary JSON file from the file system.
     */
    private void getVocabulary() {
        JSONParser parser = new JSONParser();
        JSONObject vocabularyJSON = null;
        try {
            String path = this.getClass().getClassLoader().getResource("ai/vocabulary.json").getFile();
            vocabularyJSON = (JSONObject) parser.parse(new FileReader(path));
        } catch (IOException | ParseException e) {
            log.warning("Vocabulary file could not be found or is corrupted - fatal. Exiting.");
            System.exit(1);
        }
        vocabulary = (JSONObject) vocabularyJSON.get("vocabulary");
        classTargets = (JSONArray) vocabularyJSON.get("labels");
    }

    /**
     * Searches for questions where no prediction from the AI exists so far.
     *
     * @return possibly empty list with all found questions.
     */
    private List getQuestions() {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT q FROM QuestionsEntity q WHERE NOT EXISTS (FROM PredictionsEntity p WHERE p.question = q.id)");
        List questions = query.getResultList();
        em.close();
        return questions;
    }

    /**
     * Returns the index of the largest float value in an array.
     * Used for mapping prediction vectors to class affiliations.
     *
     * @param vector array to be looked into
     * @return array index of biggest value
     */
    private int argmax(float[] vector) {
        float max = -1;
        int indexMax = 0;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > max) {
                max = vector[i];
                indexMax = i;
            }
        }
        return indexMax;
    }

    /**
     * Persists a prediction in the database.
     *
     * @param question the question entity that was answered to.
     * @param answer   the answer the AI gave.
     */
    private void insertAnswer(QuestionsEntity question, String answer) {
        log.info("Inserting new Prediction for question: " + question.getId());
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        PredictionsEntity entity = new PredictionsEntity();
        entity.setQuestion(question.getId());
        entity.setAnswer(answer);
        entity.setCorrect(answer.equals(question.getAnswer()));
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }
}
