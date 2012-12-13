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
package org.alfresco.repo.security.authority.script;

import java.io.Serializable;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.mozilla.javascript.Scriptable;

/**
 * The Script user is a USER authority exposed to the scripting API
 * 
 * @author mrogers
 */
public class ScriptUser implements Authority, Serializable
{
    private static final long serialVersionUID = 7865300693011208293L;
    private transient ServiceRegistry serviceRegistry;
    private transient AuthorityService authorityService;
    private transient PersonService personService;
    private ScriptAuthorityType authorityType = ScriptAuthorityType.USER;
    private String userName;
    private String shortName;
    private String displayName;
    private String fullName;
    private NodeRef personNodeRef;
    private Scriptable scope;
    
    /**
     * Constructs a scriptable object representing a user.
     * 
     * @param userName The username
     * @param personNodeRef The NodeRef
     * @param serviceRegistry A ServiceRegistry instance
     * @param scope Script scope
     * @since 4.0
     */
    public ScriptUser(String userName, NodeRef personNodeRef, ServiceRegistry serviceRegistry, Scriptable scope)
    {
       this.serviceRegistry = serviceRegistry;
       this.authorityService = serviceRegistry.getAuthorityService();
       this.personService = serviceRegistry.getPersonService();
       this.scope = scope;
       this.personNodeRef = personNodeRef == null ? personService.getPerson(userName) : personNodeRef;
       this.userName = userName;
       
       this.shortName = authorityService.getShortName(userName);
       NodeService nodeService = serviceRegistry.getNodeService();
       String firstName = (String)nodeService.getProperty(this.personNodeRef, ContentModel.PROP_FIRSTNAME);
       String lastName = (String)nodeService.getProperty(this.personNodeRef, ContentModel.PROP_LASTNAME);
       this.displayName = this.fullName = (firstName != null ? firstName : "") + (lastName != null ? (' ' + lastName) : "");
    }
    
    public ScriptAuthorityType getAuthorityType() 
    {
       return authorityType;
    }

    public String getShortName() 
    {
       return shortName;
    }

    public String getFullName() 
    {
       return fullName;
    }

    /**
     * Return the User Name, also known as the Authority Full Name 
     */
    public String getUserName() 
    {
       return userName;
    }

    public String getDisplayName() 
    {
       return displayName;
    }

    /**
     * Return the NodeRef of the person
     * 
     * @since 4.0
     */
    public NodeRef getPersonNodeRef()
    {
       if (personNodeRef == null)
       {
          // Lazy lookup for Authority based creation
          personNodeRef = authorityService.getAuthorityNodeRef(userName);
       }
       return personNodeRef;
    }

    /**
     * Return a ScriptNode wrapping the person
     * 
     * @since 4.0
     */
    public ScriptNode getPerson()
    {
       return new ScriptNode(getPersonNodeRef(), serviceRegistry, this.scope);
    }

    /**
     * Gets all the zones of this user
     *
     * @return The name of the zones of this user.
     *
     * @since 4.1.3
     */
    @Override
    public Set<String> getZones()
    {
        return authorityService.getAuthorityZones(fullName);
    }
}
