package bean;

import dao.UserDAO;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

@Named
@RequestScoped
public class LoginBean extends SessionBean {

    private static final long serialVersionUID = 1L;

    @Inject
    private UserDAO userDAO;

    private String password, username;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String login() {
        int userID = userDAO.login(username, password);
        if (userID != -1) {
            HttpSession session = getSession();
            session.setAttribute("username", username);
            session.setAttribute("userID", userID);
            return "game";
        } else {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Invalid Login!",
                            "Please Try Again!"));
            return "index";
        }
    }
}