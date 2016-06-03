package org.alfresco.repo.web.scripts.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.events.types.Event;
import org.alfresco.events.types.RepositoryEventImpl;
import org.alfresco.repo.events.EventPreparator;
import org.alfresco.repo.events.EventPublisher;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Common code between Get based login and POST based login
 */
/* package scope */ abstract class AbstractLoginBean extends DeclarativeWebScript
{
    // dependencies
    private AuthenticationService authenticationService;
    protected EventPublisher eventPublisher;
    
    /**
     * @param authenticationService AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @param eventPublisher EventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) 
    {
    	return null;
    }
    
    protected Map<String, Object> login(final String username, String password)
    {
        try
        {
            // get ticket
            authenticationService.authenticate(username, password.toCharArray());

            eventPublisher.publishEvent(new EventPreparator(){
                @Override
                public Event prepareEvent(String user, String networkId, String transactionId)
                {
                	// TODO need to fix up to pass correct seqNo and alfrescoClientId
                    return new RepositoryEventImpl(-1l, "login", transactionId, networkId, new Date().getTime(),
                    		username, null);
                }
            });
            
            // add ticket to model for javascript and template access
            Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
            model.put("username", username);
            model.put("ticket",  authenticationService.getCurrentTicket());
            
            return model;
        }
        catch(AuthenticationException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Login failed");
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }


}