/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.jscript;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ParameterCheck;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Scripted People service for describing and executing actions against People & Groups.
 * 
 * @author davidc
 */
public final class People extends BaseScopableProcessorExtension
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    private AuthorityDAO authorityDAO;
    private AuthorityService authorityService;

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
     * @param authorityService The authorityService to set.
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Gets the Person given the username
     * 
     * @param username  the username of the person to get
     * @return the person node (type cm:person) or null if no such person exists 
     */
    public Node getPerson(String username)
    {
        ParameterCheck.mandatoryString("Username", username);
        Node person = null;
        PersonService personService = services.getPersonService();
        if (personService.personExists(username))
        {
            NodeRef personRef = personService.getPerson(username);
            person = new Node(personRef, services, getScope());
        }
        return person;
    }

    /**
     * Gets the Group given the group name
     * 
     * @param groupName  name of group to get
     * @return  the group node (type usr:authorityContainer) or null if no such group exists
     */
    public Node getGroup(String groupName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        Node group = null;
        NodeRef groupRef = authorityDAO.getAuthorityNodeRefOrNull(groupName);
        if (groupRef != null)
        {
            group = new Node(groupRef, services, getScope());
        }
        return group;
    }
    
    /**
     * Deletes a group from the system.
     * 
     * @param group     The group to delete
     */
    public void deleteGroup(Node group)
    {
        ParameterCheck.mandatory("Group", group);
        if (group.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String groupName = (String)group.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            authorityService.deleteAuthority(groupName);
        }
    }
    
    /**
     * Create a new root level group with the specified unique name
     * 
     * @param groupName     The unique group name to create - NOTE: do not prefix with "GROUP_"
     * 
     * @return the group reference if successful or null if failed
     */
    public Node createGroup(String groupName)
    {
        return createGroup(null, groupName);
    }
    
    /**
     * Create a new group with the specified unique name
     * 
     * @param parentGroup   The parent group node - can be null for a root level group
     * @param groupName     The unique group name to create - NOTE: do not prefix with "GROUP_"
     * 
     * @return the group reference if successful or null if failed
     */
    public Node createGroup(Node parentGroup, String groupName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        
        Node group = null;
        
        String actualName = services.getAuthorityService().getName(AuthorityType.GROUP, groupName);
        if (authorityService.authorityExists(groupName) == false)
        {
            String parentGroupName = null;
            if (parentGroup != null)
            {
                parentGroupName = (String)parentGroup.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            }
            String result = authorityService.createAuthority(AuthorityType.GROUP, parentGroupName, groupName);
            group = getGroup(result);
        }
        
        return group;
    }
    
    /**
     * Add an authority (a user or group) to a group container as a new child
     * 
     * @param parentGroup   The parent container group
     * @param authority     The authority (user or group) to add
     */
    public void addAuthority(Node parentGroup, Node authority)
    {
        ParameterCheck.mandatory("Authority", authority);
        ParameterCheck.mandatory("ParentGroup", parentGroup);
        if (parentGroup.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String parentGroupName = (String)parentGroup.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            String authorityName;
            if (authority.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            }
            else
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_USERNAME);
            }
            authorityService.addAuthority(parentGroupName, authorityName);
        }
    }
    
    /**
     * Remove an authority (a user or group) from a group
     * 
     * @param parentGroup   The parent container group
     * @param authority     The authority (user or group) to remove
     */
    public void removeAuthority(Node parentGroup, Node authority)
    {
        ParameterCheck.mandatory("Authority", authority);
        ParameterCheck.mandatory("ParentGroup", parentGroup);
        if (parentGroup.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String parentGroupName = (String)parentGroup.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            String authorityName;
            if (authority.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            }
            else
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_USERNAME);
            }
            authorityService.removeAuthority(parentGroupName, authorityName);
        }
    }
    
    /**
     * Gets the members (people) of a group (including all sub-groups)
     * 
     * @param group        the group to retrieve members for
     * @param recurse      recurse into sub-groups
     * 
     * @return members of the group as a JavaScript array
     */
    public Scriptable getMembers(Node group)
    {
        ParameterCheck.mandatory("Group", group);
        Object[] members = getContainedAuthorities(group, AuthorityType.USER, true);
        return Context.getCurrentContext().newArray(getScope(), members);
    }

    /**
     * Gets the members (people) of a group
     * 
     * @param group        the group to retrieve members for
     * @param recurse      recurse into sub-groups
     * 
     * @return the members of the group as a JavaScript array
     */
    public Scriptable getMembers(Node group, boolean recurse)
    {
        ParameterCheck.mandatory("Group", group);
        Object[] members = getContainedAuthorities(group, AuthorityType.USER, recurse);
        return Context.getCurrentContext().newArray(getScope(), members);
    }
    
    /**
     * Gets the groups that contain the specified authority
     * 
     * @param person       the user (cm:person) to get the containing groups for
     * 
     * @return the containing groups as a JavaScript array, can be null
     */
    public Scriptable getContainerGroups(Node person)
    {
        ParameterCheck.mandatory("Person", person);
        Object[] parents = null;
        Set<String> authorities = this.authorityService.getContainingAuthorities(
                AuthorityType.GROUP,
                (String)person.getProperties().get(ContentModel.PROP_USERNAME),
                false);
        parents = new Object[authorities.size()];
        int i = 0;
        for (String authority : authorities)
        {
            Node group = getGroup(authority);
            if (group != null)
            {
                parents[i++] = group; 
            }
        }
        return Context.getCurrentContext().newArray(getScope(), parents);
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
    private Object[] getContainedAuthorities(Node container, AuthorityType type, boolean recurse)
    {
        Object[] members = null;
        
        if (container.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String groupName = (String)container.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            Set<String> authorities = authorityService.getContainedAuthorities(type, groupName, !recurse);
            members = new Object[authorities.size()];
            int i = 0;
            for (String authority : authorities)
            {
                AuthorityType authorityType = AuthorityType.getAuthorityType(authority);
                if (authorityType.equals(AuthorityType.GROUP))
                {
                    Node group = getGroup(authority);
                    if (group != null)
                    {
                        members[i++] = group; 
                    }
                }
                else if (authorityType.equals(AuthorityType.USER))
                {
                    Node person = getPerson(authority);
                    if (person != null)
                    {
                        members[i++] = person; 
                    }
                }
            }
        }
        
        return members != null ? members : new Object[0];
    }
}
