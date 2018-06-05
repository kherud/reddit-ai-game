package dao;

import config.Configuration;
import model.UsersEntity;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This is the data access object for managing the login and registration page.
 */
@Named
@Dependent
public class UserDAO {

    private final EntityManagerFactory factory;
    // @PersistenceContext
    // private EntityManager em;
    public UserDAO(){
        factory = Persistence.createEntityManagerFactory(Configuration.PERSISTENCE_UNIT_NAME);
    }

    /**
     * Compares the given credentials with the database and returns the id of the user if they were valid.
     * Otherwise returns -1.
     * @param name user name.
     * @param password user password.
     * @return user id or -1 if invalid.
     */
    public int login(String name, String password) {
        try {
            password = hashPassword(password);
            EntityManager em = factory.createEntityManager();
            Query query = em.createQuery("SELECT u FROM UsersEntity u WHERE name=:name AND password=:password")
                    .setParameter("name", name)
                    .setParameter("password", password);
            UsersEntity user = (UsersEntity) query.getSingleResult();
            return user.getId();
        } catch (NoSuchAlgorithmException | NoResultException e) {
            return -1;
        }
    }

    /**
     * Verifies if a user name is available, thus not already taken.
     * @param name requested user name.
     * @return true if the user name is available, otherwise false.
     */
    public boolean nameExists(String name) {
        EntityManager em = factory.createEntityManager();
        Query query = em.createQuery("SELECT u FROM UsersEntity u WHERE name=:name")
                .setParameter("name", name);
        return query.getResultList().size() == 1;
    }

    /**
     * Creates a new user entity and persists it.
     * Also hashes the user's password.
     * @param username name of the user.
     * @param password password of the new user.
     * @return true if user was successfully created, otherwise false.
     */
    public boolean createUser(String username, String password) {
        try {
            UsersEntity user = new UsersEntity();
            user.setName(username);
            password = hashPassword(password);
            user.setPassword(password);
            EntityManager em = factory.createEntityManager();
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return true;
        } catch (NoSuchAlgorithmException e){
            return false;
        }
    }

    /**
     * Uses SHA-256 for hashing a String / user password.
     * Since the importance of our users' security is very low we forgo salting
     * @param password the String / password that is hashed.
     * @return the calculated hash.
     * @throws NoSuchAlgorithmException
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Transforms the bytes of a hash to an actual String.
     * @param hash bytes of a hash.
     * @return calculated String.
     */
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte hashChar : hash) {
            String hex = Integer.toHexString(0xff & hashChar);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}