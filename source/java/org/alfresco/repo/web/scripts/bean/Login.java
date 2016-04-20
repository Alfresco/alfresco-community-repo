package org.alfresco.repo.web.scripts.bean;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Login and establish a ticket
 * 
 * @author davidc
 */
public class Login extends AbstractLoginBean
{   
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // extract username and password
        String username = req.getParameter("u");
        if (username == null || username.length() == 0)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
        }
        String password = req.getParameter("pw");
        if (password == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Password not specified");
        }

        try
        {
            return login(username, password);
        }
        catch(WebScriptException e)
        {
            status.setCode(e.getStatus());
            status.setMessage(e.getMessage());
            status.setRedirect(true);
            return null;
        }
    }
}