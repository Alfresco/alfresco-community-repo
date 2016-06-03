package org.alfresco.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Delete Login Ticket
 * 
 * @author davidc
 */
public class LoginTicketDelete extends DeclarativeWebScript
{
    // dependencies
    private AuthenticationService authenticationService;
    private TicketComponent ticketComponent;
    
    /**
     * @param ticketComponent TicketComponent
     */
    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }

    /**
     * @param authenticationService AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
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

            // do not go any further if tickets are different
            if (!AuthenticationUtil.getFullyAuthenticatedUser().equals(ticketUser))
            {
                status.setCode(HttpServletResponse.SC_NOT_FOUND);
                status.setMessage("Ticket not found");
            }
            else
            {
                // delete the ticket
                authenticationService.invalidateTicket(ticket);
                status.setMessage("Deleted Ticket " + ticket);
            }
        }
        catch(AuthenticationException e)
        {
            status.setCode(HttpServletResponse.SC_NOT_FOUND);
            status.setMessage("Ticket not found");
        }

        status.setRedirect(true);
        return model;
    }

}
