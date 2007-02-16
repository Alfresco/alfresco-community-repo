/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authority;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * The default implementation of the authority service.
 * 
 * @author Andy Hind
 */
public class SimpleAuthorityServiceImpl implements AuthorityService
{
    private PersonService personService;

    private NodeService nodeService;

    private Set<String> adminSet = Collections.singleton(PermissionService.ADMINISTRATOR_AUTHORITY);

    private Set<String> guestSet = Collections.singleton(PermissionService.GUEST_AUTHORITY);

    private Set<String> allSet = Collections.singleton(PermissionService.ALL_AUTHORITIES);

    private Set<String> adminUsers;

    private AuthenticationComponent authenticationComponent;

    public SimpleAuthorityServiceImpl()
    {
        super();
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Currently the admin authority is granted only to the ALFRESCO_ADMIN_USER
     * user.
     */
    public boolean hasAdminAuthority()
    {
        String currentUserName = authenticationComponent.getCurrentUserName();
        return ((currentUserName != null) && adminUsers.contains(currentUserName));
    }

    // IOC

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setAdminUsers(Set<String> adminUsers)
    {
        this.adminUsers = adminUsers;
    }
 
    public Set<String> getAuthorities()
    {
        Set<String> authorities = new HashSet<String>();
        String currentUserName = authenticationComponent.getCurrentUserName();
        if (adminUsers.contains(currentUserName))
        {
            authorities.addAll(adminSet);
        }
        if(AuthorityType.getAuthorityType(currentUserName) != AuthorityType.GUEST)
        {
           authorities.addAll(allSet);
        }
        return authorities;
    }

    public Set<String> getAllAuthorities(AuthorityType type)
    {
        Set<String> authorities = new HashSet<String>();
        switch (type)
        {
        case ADMIN:
            authorities.addAll(adminSet);
            break;
        case EVERYONE:
            authorities.addAll(allSet);
            break;
        case GUEST:
            authorities.addAll(guestSet);
            break;
        case GROUP:
            authorities.addAll(allSet);
            break;
        case OWNER:
             break;
        case ROLE:
            break;
        case USER:
            for (NodeRef personRef : personService.getAllPeople())
            {
                authorities.add(DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(personRef,
                        ContentModel.PROP_USERNAME)));
            }
            break;
        default:
            break;
        }
        return authorities;
    }

    public void addAuthority(String parentName, String childName)
    {
        
    }

    
    public String createAuthority(AuthorityType type, String parentName, String shortName)
    {
       return "";
    }

    public void deleteAuthority(String name)
    {
      
    }

    public Set<String> getAllRootAuthorities(AuthorityType type)
    {
        return getAllAuthorities(type);
    }

    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return Collections.<String>emptySet();
    }

    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate)
    {
        return Collections.<String>emptySet();
    }

    public String getName(AuthorityType type, String shortName)
    {
        if (type.isFixedString())
        {
            return type.getFixedString();
        }
        else if (type.isPrefixed())
        {
            return type.getPrefixString() + shortName;
        }
        else
        {
            return shortName;
        }
    }

    public String getShortName(String name)
    {
        AuthorityType type = AuthorityType.getAuthorityType(name);
        if (type.isFixedString())
        {
            return "";
        }
        else if (type.isPrefixed())
        {
            return name.substring(type.getPrefixString().length());
        }
        else
        {
            return name;
        }

    }

    public void removeAuthority(String parentName, String childName)
    {
        
    }

    public boolean authorityExists(String name)
    {
        return false;
    }

}
