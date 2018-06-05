package bean;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * This class provides session functionality to any class in the package extending it.
 */
class SessionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    }

    HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    String getUserName() {
        HttpSession session = getSession();
        return session.getAttribute("username").toString();
    }

    int getUserId() {
        HttpSession session = getSession();
        return (int) session.getAttribute("userID");
    }

    String getVariable(String name) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
    }

    void logout(){
        getSession().invalidate();
    }
}
