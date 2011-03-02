/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.security.person;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;

/**
 * Utility class to help write tests which require creation of people.
 * @author Nick Smith
 *
 */
public class TestPersonManager
{
    private static final String ORGANISATION_SUFFIX = "Organisation";
    private static final String JOB_SUFFIX = "JobTitle";
    private static final String EMAIL_SUFFIX = "@email.com";
    private static final String LAST_NAME_SUFFIX = "LastName";
    private static final String FIRST_NAME_SUFFIX = "FirstName";

    private final MutableAuthenticationService authenticationService;
    private final PersonService personService;
    private final NodeService nodeService;

    private final Map<String, NodeRef> people = new HashMap<String, NodeRef>(); 
    
    public TestPersonManager(MutableAuthenticationService authenticationService,
            PersonService personService,
            NodeService nodeService)
    {
        this.authenticationService = authenticationService;
        this.personService = personService;
        this.nodeService = nodeService;
    }

    public NodeRef createPerson(final String userName)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                if (authenticationService.authenticationExists(userName) == false)
                {
                    authenticationService.createAuthentication(userName, "password".toCharArray());
                    return makePersonNode(userName);
                }
                else
                {
                    return personService.getPerson(userName);
                }
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private NodeRef makePersonNode(String userName)
    {
        PropertyMap personProps = new PropertyMap();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, userName+FIRST_NAME_SUFFIX);
        personProps.put(ContentModel.PROP_LASTNAME, userName+LAST_NAME_SUFFIX);
        personProps.put(ContentModel.PROP_EMAIL, userName+EMAIL_SUFFIX);
        personProps.put(ContentModel.PROP_JOBTITLE, userName+JOB_SUFFIX);
        personProps.put(ContentModel.PROP_JOBTITLE, userName+ORGANISATION_SUFFIX);
        
        NodeRef person = personService.createPerson(personProps);
        people.put(userName, person);
        return person;
    }
    
    public NodeRef get(String userName)
    {
        NodeRef person = people.get(userName);
        if(person !=null)
            return person;
        else throw new IllegalArgumentException("Cannot get user as unregistered person:"+userName);
    }
    
    public void setUser(String userName)
    {
        if(people.containsKey(userName))
        {
            AuthenticationUtil.setFullyAuthenticatedUser(userName);
        }
        else throw new IllegalArgumentException("Cannot set user as unregistered person: "+userName);
    }
    
    public void clearPeople()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                {
            public Void doWork() throws Exception
            {
                for (String user : people.keySet()) {
                    personService.deletePerson(user);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    public String getFirstName(String userName)
    {
        NodeRef person = get(userName);
        return (String) nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
    }
    
    public String getLastName(String userName)
    {
        NodeRef person = get(userName);
        return (String) nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
    }
}