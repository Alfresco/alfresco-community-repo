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
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.util.ValueDerivingMapFactory;
import org.alfresco.util.ValueDerivingMapFactory.ValueDeriver;
import org.springframework.beans.factory.InitializingBean;

/**
 * People and users support in FreeMarker templates.
 * 
 * @author Kevin Roast
 */
public class People extends BaseTemplateProcessorExtension implements InitializingBean
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    private AuthorityDAO authorityDAO;
    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private StoreRef storeRef;
    private ValueDerivingMapFactory<TemplateNode, String, Boolean> valueDerivingMapFactory;        
    
    public void afterPropertiesSet() throws Exception
    {
        Map <String, ValueDeriver<TemplateNode, Boolean>> capabilityTesters = new HashMap<String, ValueDeriver<TemplateNode, Boolean>>(5);
        capabilityTesters.put("isAdmin", new ValueDeriver<TemplateNode, Boolean>()
                {
                    public Boolean deriveValue(TemplateNode source)
                    {
                        return isAdmin(source);
                    }
                });
                capabilityTesters.put("isGuest", new ValueDeriver<TemplateNode, Boolean>()
                {
                    public Boolean deriveValue(TemplateNode source)
                    {
                        return isGuest(source);
                    }
                });
                capabilityTesters.put("isMutable", new ValueDeriver<TemplateNode, Boolean>()
                {
                    public Boolean deriveValue(TemplateNode source)
                    {
                        // Check whether the account is mutable according to the authentication service
                        String sourceUser = (String) source.getProperties().get(ContentModel.PROP_USERNAME);
                        if (!authenticationService.isAuthenticationMutable(sourceUser))
                        {
                            return false;
                        }
                        // Only allow non-admin users to mutate their own accounts
                        String currentUser = authenticationService.getCurrentUserName();
                        if (currentUser.equals(sourceUser) || authorityService.isAdminAuthority(currentUser))
                        {
                            return true;
                        }
                        return false;
                    }
                });
        this.valueDerivingMapFactory = new ValueDerivingMapFactory<TemplateNode, String, Boolean>(capabilityTesters);
    }

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
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
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
     * @return the containing groups as a List of TemplateNode objects, can be null
     */
    public List<TemplateNode> getContainerGroups(TemplateNode person)
    {
        ParameterCheck.mandatory("Person", person);
        List<TemplateNode> parents;
        Set<String> authorities = this.authorityService.getContainingAuthoritiesInZone(
                AuthorityType.GROUP,
                (String)person.getProperties().get(ContentModel.PROP_USERNAME),
                AuthorityService.ZONE_APP_DEFAULT, null, 1000);
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
     * Return true if the specified user is an Guest authority.
     * 
     * @param person to test
     * 
     * @return true if a guest user, false otherwise
     */
    public boolean isGuest(TemplateNode person)
    {
        ParameterCheck.mandatory("Person", person);
        return this.authorityService.isGuestAuthority((String)person.getProperties().get(ContentModel.PROP_USERNAME));
    }
    
    /**
     * Gets a map of capabilities (boolean assertions) for the given person.
     * 
     * @param person the person
     * @return the capability map
     */
    public Map<String, Boolean> getCapabilities(final TemplateNode person)
    {
        ParameterCheck.mandatory("Person", person);
        return this.valueDerivingMapFactory.getMap(person);
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
