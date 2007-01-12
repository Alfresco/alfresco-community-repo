/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.mozilla.javascript.Scriptable;

/**
 * Scripted People service for describing and executing actions against People & Groups.
 * 
 * @author davidc
 */
public final class People extends BaseScriptImplementation implements Scopeable
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    private AuthorityDAO authorityDAO;

    /** Root scope for this object */
    private Scriptable scope;

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
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    /**
     * Gets the Person given the username
     * 
     * @param username  the username of the person to get
     * @return the person node (type cm:person) or null if no such person exists 
     */
    public Node getPerson(String username)
    {
        Node person = null;
        PersonService personService = services.getPersonService();
        if (personService.personExists(username))
        {
            NodeRef personRef = personService.getPerson(username);
            person = new Node(personRef, services, scope);
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
        Node group = null;
        NodeRef groupRef = authorityDAO.getAuthorityNodeRefOrNull(groupName);
        if (groupRef != null)
        {
            group = new Node(groupRef, services, scope);
        }
        return group;
    }
    
    /**
     * Gets the members (people) of a group (including all sub-groups)
     * 
     * @param group  the group to retrieve members for
     * @param recurse  recurse into sub-groups
     * @return  the members of the group
     */
    public Node[] getMembers(Node group)
    {
        return getContainedAuthorities(group, AuthorityType.USER, true);
    }

    /**
     * Gets the members (people) of a group
     * 
     * @param group  the group to retrieve members for
     * @param recurse  recurse into sub-groups
     * @return  the members of the group
     */
    public Node[] getMembers(Node group, boolean recurse)
    {
        return getContainedAuthorities(group, AuthorityType.USER, recurse);
    }
    
    /**
     * Get Contained Authorities
     * 
     * @param container  authority containers
     * @param type  authority type to filter by
     * @param recurse  recurse into sub-containers
     * @return  contained authorities
     */
    private Node[] getContainedAuthorities(Node container, AuthorityType type, boolean recurse)
    {
        Node[] members = null;
        if (container.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            AuthorityService authorityService = services.getAuthorityService();
            String groupName = (String)container.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            Set<String> authorities = authorityService.getContainedAuthorities(type, groupName, !recurse);
            members = new Node[authorities.size()];
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
        return members;
    }
    
}
