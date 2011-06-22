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

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
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
    private ScriptAuthorityType authorityType = ScriptAuthorityType.USER;
    private String userName;
    private String shortName;
    private String displayName;
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
       this.scope = scope;
       this.personNodeRef = personNodeRef;
       this.userName = userName;
       
       shortName = authorityService.getShortName(userName);
       displayName = authorityService.getAuthorityDisplayName(userName);
    }
    
    /**
     * @deprecated The ServiceRegistry and a Scriptable scope are now required
     */
    public ScriptUser(String userName, AuthorityService authorityService)
    {
       this.authorityService = authorityService;
       this.userName = userName; 
       shortName = authorityService.getShortName(userName);
       displayName = authorityService.getAuthorityDisplayName(userName);
    }
    
    public void setAuthorityType(ScriptAuthorityType authorityType) 
    {
       this.authorityType = authorityType;
    }

    public ScriptAuthorityType getAuthorityType() 
    {
       return authorityType;
    }

    public void setShortName(String shortName) 
    {
       this.shortName = shortName;
    }

    public String getShortName() 
    {
       return shortName;
    }

    public void setFullName(String fullName) 
    {
       this.userName = fullName;
    }

    public String getFullName() 
    {
       return userName;
    }

    /**
     * Return the User Name, also known as the 
     *  Authority Full Name 
     */
    public String getUserName() 
    {
       return userName;
    }

    public void setDisplayName(String displayName) 
    {
       this.displayName = displayName;
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
}
