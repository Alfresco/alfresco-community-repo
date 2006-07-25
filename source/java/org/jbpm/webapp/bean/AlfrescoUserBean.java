package org.jbpm.webapp.bean;

import org.alfresco.service.cmr.security.AuthenticationService;

public class AlfrescoUserBean extends UserBean
{
    AuthenticationService authService;


    public void setAuthenticationService(AuthenticationService authService)
    {
        this.authService = authService;
    }

    
    @Override
    public String getUserName()
    {
        return authService.getCurrentUserName();
    }

}
