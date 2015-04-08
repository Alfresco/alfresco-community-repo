/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts.servlet;

import javax.servlet.ServletContext;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.web.context.ServletContextAware;


/**
 * Used for local web script tests when MT is enabled - eg. WebScriptTestSuite, BaseCMISTest (AspectTest, PolicyTest), etc.
 * 
 * When MT is enabled the repository container required authentication must be "guest" or higher (ie. not "none") to determine the tenant domain.
 * 
 * This dummy authenticator will effectively pass-through the runAs user ... note: it needs to set the runAs user since it will be cleared first (by RepositoryContainer.authenticate).
 *
 * @author janv
 * @since 4.0 (thor)
 */
public class LocalTestRunAsAuthenticatorFactory implements ServletAuthenticatorFactory, ServletContextAware
{
    @Override
    public void setServletContext(ServletContext context)
    {
    }
    
    @Override
    public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
    {
        String runAsUser = AuthenticationUtil.getRunAsUser();
        if (runAsUser == null)
        {
            runAsUser = AuthenticationUtil.getSystemUserName();
        }
        return new LocalTestRunAsAuthenticator(runAsUser);
    }
    
    public static class LocalTestRunAsAuthenticator implements Authenticator
    {
        private String userName;
        
        public LocalTestRunAsAuthenticator(String userName)
        {
            this.userName = userName;
        }
        
        @Override
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            if (! emptyCredentials())
            {
                AuthenticationUtil.setRunAsUser(userName);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean emptyCredentials()
        {
            return (userName == null || userName.length() == 0);
        }
    }

}