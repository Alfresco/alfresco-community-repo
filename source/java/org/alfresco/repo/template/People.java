/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ParameterCheck;

/**
 * People and users support in FreeMarker templates.
 * 
 * @author Kevin Roast
 */
public class People extends BaseTemplateProcessorExtension
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    private AuthorityDAO authorityDAO;
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    private PersonService personService;
    private StoreRef storeRef;
    
    
    /**
     * Set the default store reference
     * 
     * @param   storeRef the default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        // ensure this is not set again
        if (this.storeRef != null)
        {
            throw new IllegalStateException("Default store URL can only be set once.");
        }
        this.storeRef = new StoreRef(storeRef);
    }

    /**
     * Set the service registry
     * 
     * @param serviceRegistry	the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

    /**
     * Set the authority DAO
     *
     * @param authorityDAO  authority dao
     */
    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }
    
    /**
     * Set the authority service
     * 
     * @param authorityService The authorityService to set.
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * Set the person service
     * 
     * @param personService The personService to set.
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Sets the authentication service.
     * 
     * @param authenticationService
     *            the new authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Gets the Person given the username
     * 
     * @param username  the username of the person to get
     * @return the person node (type cm:person) or null if no such person exists 
     */
    public TemplateNode getPerson(String username)
    {
        ParameterCheck.mandatoryString("Username", username);
        TemplateNode person = null;
        if (personService.personExists(username))
        {
            NodeRef personRef = personService.getPerson(username);
            person = new TemplateNode(personRef, services, getTemplateImageResolver());
        }
        return person;
    }

    /**
     * Gets the Group given the group name
     * 
     * @param groupName  name of group to get
     * @return the group node (type usr:authorityContainer) or null if no such group exists
     */
    public TemplateNode getGroup(String groupName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        TemplateNode group = null;
        NodeRef groupRef = authorityDAO.getAuthorityNodeRefOrNull(groupName);
        if (groupRef != null)
        {
            group = new TemplateNode(groupRef, services, getTemplateImageResolver());
        }
        return group;
    }
    
    /**
     * Gets the members (people) of a group (including all sub-groups)
     * 
     * @param group        the group to retrieve members for
     * @param recurse      recurse into sub-groups
     * 
     * @return list of nodes representing the group members
     */
    public List<TemplateNode> getMembers(TemplateNode group)
    {
        ParameterCheck.mandatory("Group", group);
        return getContainedAuthorities(group, AuthorityType.USER, true);
    }

    /**
     * Gets the members (people) of a group
     * 
     * @param group        the group to retrieve members for
     * @param recurse      recurse into sub-groups
     * 
     * @return list of nodes representing the group members
     */
    public List<TemplateNode> getMembers(TemplateNode group, boolean recurse)
    {
        ParameterCheck.mandatory("Group", group);
        return getContainedAuthorities(group, AuthorityType.USER, recurse);
    }
    
    /**
     * Gets the groups that contain the specified authority
     * 
     * @param person       the user (cm:person) to get the containing groups for
     * 
     * @return the containing groups as a JavaScript array, can be null
     */
    public List<TemplateNode> getContainerGroups(TemplateNode person)
    {
        ParameterCheck.mandatory("Person", person);
        List<TemplateNode> parents;
        Set<String> authorities = this.authorityService.getContainingAuthorities(
                AuthorityType.GROUP,
                (String)person.getProperties().get(ContentModel.PROP_USERNAME),
                false);
        parents = new ArrayList<TemplateNode>(authorities.size());
        for (String authority : authorities)
        {
            TemplateNode group = getGroup(authority);
            if (group != null)
            {
                parents.add(group); 
            }
        }
        return parents;
    }
    
    /**
     * Return true if the specified user is an Administrator authority.
     * 
     * @param person to test
     * 
     * @return true if an admin, false otherwise
     */
    public boolean isAdmin(TemplateNode person)
    {
        ParameterCheck.mandatory("Person", person);
        return this.authorityService.isAdminAuthority((String)person.getProperties().get(ContentModel.PROP_USERNAME));
    }
    
    /**
     * Return true if the specified user is an Administrator authority.
     * 
     * @param person to test
     * 
     * @return true if an admin, false otherwise
     */
    public boolean isGuest(TemplateNode person)
    {
        ParameterCheck.mandatory("Person", person);
        return this.authorityService.isGuestAuthority((String)person.getProperties().get(ContentModel.PROP_USERNAME));
    }

    /**
     * Return true if the specified user account is enabled.
     *  
     * @param person to test
     * 
     * @return true if account enabled, false if disabled
     */
    public boolean isAccountEnabled(TemplateNode person)
    {
        // Only admins have rights to check authentication enablement
        if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            return this.authenticationService.getAuthenticationEnabled((String) person.getProperties().get(
                    ContentModel.PROP_USERNAME));
        }
        return true;
    }

    /**
     * Get Contained Authorities
     * 
     * @param container  authority containers
     * @param type       authority type to filter by
     * @param recurse    recurse into sub-containers
     * 
     * @return contained authorities
     */
    private List<TemplateNode> getContainedAuthorities(TemplateNode container, AuthorityType type, boolean recurse)
    {
        List<TemplateNode> members = null;
        
        if (container.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String groupName = (String)container.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            Set<String> authorities = authorityService.getContainedAuthorities(type, groupName, !recurse);
            members = new ArrayList<TemplateNode>(authorities.size());
            for (String authority : authorities)
            {
                AuthorityType authorityType = AuthorityType.getAuthorityType(authority);
                if (authorityType.equals(AuthorityType.GROUP))
                {
                    TemplateNode group = getGroup(authority);
                    if (group != null)
                    {
                        members.add(group);
                    }
                }
                else if (authorityType.equals(AuthorityType.USER))
                {
                    TemplateNode person = getPerson(authority);
                    if (person != null)
                    {
                        members.add(person);
                    }
                }
            }
        }
        
        return members != null ? members : Collections.<TemplateNode>emptyList();
    }
}
