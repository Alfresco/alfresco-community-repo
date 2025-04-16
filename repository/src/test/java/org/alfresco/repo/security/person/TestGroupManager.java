/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.person;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class TestGroupManager
{
    private final AuthorityService authorityService;

    private final Map<String, NodeRef> groups = new HashMap<String, NodeRef>();

    public TestGroupManager(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Creates a group with the given name if one does not already exist.
     * 
     * @param groupShortName
     *            String
     * @return The group's full name.
     */
    public String createGroupIfNotExist(String groupShortName)
    {
        String fullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
        if (groups.containsKey(groupShortName) == false)
        {
            if (authorityService.authorityExists(fullName) == false)
            {
                Set<String> zones = new HashSet<String>(2, 1.0f);
                zones.add(AuthorityService.ZONE_APP_DEFAULT);
                fullName = authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, zones);
                groups.put(groupShortName, findGroupNode(groupShortName));
            }
        }
        return fullName;
    }

    /**
     * Adds the child group as a sub-authority of the parent group. Creates the child group and parent group if they do not exist.
     * 
     * @param parentGroupShortName
     *            String
     * @param childGroupShortName
     *            String
     * @return The full name of the child group.
     */
    public String addGroupToParent(String parentGroupShortName, String childGroupShortName)
    {
        String parentFullName = createGroupIfNotExist(parentGroupShortName);
        String groupFullName = createGroupIfNotExist(childGroupShortName);
        authorityService.addAuthority(parentFullName, groupFullName);
        return groupFullName;
    }

    /**
     * Adds the user as a sub-authroity of the specified group. Creates the group if it doesn't exist.
     * 
     * @param groupShortName
     *            String
     * @param userName
     *            String
     */
    public void addUserToGroup(String groupShortName, String userName)
    {
        String fullGroupName = createGroupIfNotExist(groupShortName);
        authorityService.addAuthority(fullGroupName, userName);
    }

    public void deleteGroup(String groupShortName)
    {
        String groupFullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
        if (authorityService.authorityExists(groupFullName))
        {
            authorityService.deleteAuthority(groupFullName);
        }
    }

    public void clearGroups()
    {
        for (String group : groups.keySet())
        {
            String fullName = authorityService.getName(AuthorityType.GROUP, group);
            if (authorityService.authorityExists(fullName))
            {
                authorityService.deleteAuthority(fullName);
            }
        }
        groups.clear();
    }

    public NodeRef get(String groupShortName)
    {
        NodeRef result = groups.get(groupShortName);
        if (result == null)
        {
            result = findGroupNode(groupShortName);
        }
        return result;
    }

    private NodeRef findGroupNode(String groupShortName)
    {
        String fullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
        NodeRef group = authorityService.getAuthorityNodeRef(fullName);
        return group;
    }
}
