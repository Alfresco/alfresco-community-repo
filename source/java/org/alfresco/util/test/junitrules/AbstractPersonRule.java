/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;
import org.springframework.context.ApplicationContext;

/**
 * This class is an abstract base class for JUnit rules which manage the lifecycle of <code>cm:person</code>
 * nodes and authentication details for the transient users often required within test code.
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public abstract class AbstractPersonRule extends ExternalResource
{
    private static final Log log = LogFactory.getLog(AbstractPersonRule.class);
    
    // Fixed defaults for the usual Alfresco cm:person metadata.
    protected static final String PASSWORD   = "PWD";
    protected static final String FIRST_NAME = "firstName";
    protected static final String LAST_NAME  = "lastName";
    protected static final String EMAIL      = "email@email.com";
    protected static final String JOB_TITLE  = "jobTitle";
    
    protected final ApplicationContext appContext;
    protected final ApplicationContextInit appContextRule;
    
    /**
     * Constructs a person rule with the specified spring context, which will be necessary
     * to actually create and delete the users.
     * 
     * @param appContext the spring app context (needed to get at Alfresco services).
     */
    public AbstractPersonRule(ApplicationContext appContext)
    {
        ParameterCheck.mandatory("appContext", appContext);
        
        this.appContext = appContext;
        this.appContextRule = null;
    }
    
    /**
     * Constructs a person rule with a reference to an {@link ApplicationContextInit rule}. This other rule will
     * be used to access the application context and from there the necessary services for the creation and deletion of users.
     * 
     * @param appContext a rule which can provide the spring application context.
     */
    public AbstractPersonRule(ApplicationContextInit appContextRule)
    {
        ParameterCheck.mandatory("appContextRule", appContextRule);
        
        this.appContext = null;
        this.appContextRule = appContextRule;
    }
    
    /**
     * This method retrieves the spring application context given to this rule.
     * 
     * @return the spring application context
     * @throws NullPointerException if the application context has not been initialised when requested.
     */
    protected ApplicationContext getApplicationContext()
    {
        ApplicationContext result = null;
        
        // The app context is either provided explicitly:
        if (appContext != null)
        {
            result = appContext;
        }
        // or is implicitly accessed via another rule:
        else 
        {
            ApplicationContext contextFromRule = appContextRule.getApplicationContext();
            if (contextFromRule != null)
            {
                result = contextFromRule;
            }
            else
            {
                throw new NullPointerException("Cannot retrieve application context from provided rule.");
            }
        }
        
        return result;
    }
    
    /**
     * This method creates a user with the specified username.
     * If an authentication for this username does not exist, it is created.
     * If a cm:person for this username does not exist, it is created.
     * This method does not handle transactions.
     * 
     * @param userName the username of the new user.
     * @return  the NodeRef of the created cm:person node.
     */
    protected NodeRef createPerson(final String userName)
    {
        // Get the spring context
        final ApplicationContext ctxt = getApplicationContext();
        
        // Extract required service beans
        final MutableAuthenticationService authService = (MutableAuthenticationService) ctxt.getBean("authenticationService");
        final PersonService personService = (PersonService) ctxt.getBean("personService");
        
        // Pre-create a person, if not already created.
        if (! authService.authenticationExists(userName))
        {
            log.debug("Creating authentication " + userName + "...");
            authService.createAuthentication(userName, PASSWORD.toCharArray());
        }
        
        NodeRef person;
        
        if (personService.personExists(userName))
        {
            person = personService.getPerson(userName, false);
        }
        else
        {
            log.debug("Creating personNode " + userName + "...");
            
            PropertyMap ppOne = new PropertyMap();
            ppOne.put(ContentModel.PROP_USERNAME,  userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, FIRST_NAME);
            ppOne.put(ContentModel.PROP_LASTNAME,  LAST_NAME);
            ppOne.put(ContentModel.PROP_EMAIL,     EMAIL);
            ppOne.put(ContentModel.PROP_JOBTITLE,  JOB_TITLE);
            
            person = personService.createPerson(ppOne);
        }
        
        return person;
    }
    
    /**
     * This method deletes the specified user's person and authentication details if they are
     * present in the system.
     * This method does not handle transactions.
     * 
     * @param userName the username of the user to be deleted.
     */
    protected void deletePerson(final String userName)
    {
        // Get the spring context
        final ApplicationContext ctxt = getApplicationContext();
        
        // Extract required service beans
        final PersonService personService = (PersonService) ctxt.getBean("personService");
        
        
        // And tear down afterwards.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                if (personService.personExists(userName))
                {
                    log.debug("Deleting person " + userName + "...");
                    personService.deletePerson(userName);
                }
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }
}
