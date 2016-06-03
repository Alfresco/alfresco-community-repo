package org.alfresco.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * WebScript java backed bean implementation - to return information about the
 * authentication system, such as account mutability.
 * 
 * @author Kevin Roast
 */
public class Authentication extends DeclarativeWebScript
{
    private MutableAuthenticationService authenticationService;
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = (MutableAuthenticationService)authenticationService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>(2);
        
        model.put("creationAllowed", this.authenticationService.isAuthenticationCreationAllowed());
        
        return model;
    }
}