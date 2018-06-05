package model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "answers", schema = "game", catalog = "")
public class AnswersEntity {
    private int id;
    private int responder;
    private String question;
    private String answer;
    private boolean correct;
    private String difficulty;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "responder", nullable = false)
    public int getResponder() {
        return responder;
    }

    public void setResponder(int responder) {
        this.responder = responder;
    }

    @Basic
    @Column(name = "question", nullable = false, length = 6)
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Basic
    @Column(name = "answer", nullable = false, length = 20)
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Basic
    @Column(name = "correct", nullable = false)
    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    @Basic
    @Column(name = "difficulty", nullable = false, length = 6)
    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswersEntity that = (AnswersEntity) o;
        return id == that.id &&
                responder == that.responder &&
                correct == that.correct &&
                Objects.equals(question, that.question) &&
                Objects.equals(answer, that.answer) &&
                Objects.equals(difficulty, that.difficulty);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, responder, question, answer, correct, difficulty);
    }
}
