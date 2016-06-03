package org.alfresco.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Login Ticket
 * 
 * @author davidc
 */
public class LoginTicket extends DeclarativeWebScript
{
    // dependencies
    private TicketComponent ticketComponent;
    
    /**
     * @param ticketComponent TicketComponent
     */
    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // retrieve ticket from request and current ticket
        String ticket = req.getExtensionPath();
        if (ticket == null || ticket.length() == 0)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Ticket not specified");
        }
        
        // construct model for ticket
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("ticket",  ticket);
        
        try
        {
            String ticketUser = ticketComponent.validateTicket(ticket);
            
            String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

            // do not go any further if tickets are different 
            // or the user is not fully authenticated
            if (currentUser == null || !currentUser.equals(ticketUser))
            {
                status.setRedirect(true);
                status.setCode(HttpServletResponse.SC_NOT_FOUND);
                status.setMessage("Ticket not found");
            }
        }
        catch (AuthenticationException e)
        {
            status.setRedirect(true);
            status.setCode(HttpServletResponse.SC_NOT_FOUND);
            status.setMessage("Ticket not found");
        }
        
        return model;
    }

}
