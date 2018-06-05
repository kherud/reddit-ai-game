package bean;

import dao.UserDAO;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * This class manages the JSF flow that is used for the registration process.
 */
@Named
@FlowScoped("register")
public class RegisterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    transient private UserDAO userDAO;

    private String name, password, password2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    /**
     * If all requirements are met a new user will be created.
     * Otherwise the front end displays an appropriate error message.
     * @return JSF id for the next page.
     */
    public String register() {
        if (inputInvalid(name, password, password2)) {
            errorMessage("Incorrect input.", "Please enter all credentials.");
            return "register";
        }

        if (userDAO.nameExists(name)) {
            errorMessage("Incorrect input.", "Name is already in use!");
            return "register";
        }

        if (!password.equals(password2)) {
            errorMessage("Incorrect input.", "Entered passwords are different!");
            return "register";
        }

        if (!userDAO.createUser(name, password)) {
            errorMessage("Something went wrong.", "User could not be created!");
            return "register";
        } else {
            return "success";
        }

    }

    /**
     * Checks whether a list of Strings contains an empty String or a null reference, thus being invalid.
     * @param inputs Argument list used for any amount of Strings to check
     * @return Boolean representing if any value was invalid.
     */
    private boolean inputInvalid(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Displays an error message in the frontend.
     * @param summary Summary of the error.
     * @param detail Details of the error.
     */
    private void errorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail)
        );
    }
}
