package model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "game", catalog = "")
public class UsersEntity {
    private int id;
    private String name;
    private String password;
    private String oldestAnswered;
    private String newestAnswered;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 45)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "password", nullable = false, length = 64)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "oldest_answered", nullable = true, length = 6)
    public String getOldestAnswered() {
        return oldestAnswered;
    }

    public void setOldestAnswered(String oldestAnswered) {
        this.oldestAnswered = oldestAnswered;
    }

    @Basic
    @Column(name = "newest_answered", nullable = true, length = 6)
    public String getNewestAnswered() {
        return newestAnswered;
    }

    public void setNewestAnswered(String newestAnswered) {
        this.newestAnswered = newestAnswered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersEntity that = (UsersEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(password, that.password) &&
                Objects.equals(oldestAnswered, that.oldestAnswered) &&
                Objects.equals(newestAnswered, that.newestAnswered);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, password, oldestAnswered, newestAnswered);
    }
}
