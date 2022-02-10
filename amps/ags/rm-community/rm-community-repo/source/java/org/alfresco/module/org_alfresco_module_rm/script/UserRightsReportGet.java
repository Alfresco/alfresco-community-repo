/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return user rights report.
 *
 * @author Gavin Cornwell
 */
public class UserRightsReportGet extends DeclarativeWebScript
{
    protected AuthorityService authorityService;
    protected PersonService personService;
    protected NodeService nodeService;
    protected FilePlanRoleService filePlanRoleService;
    protected FilePlanService filePlanService;

    /**
     * Sets the AuthorityService instance
     *
     * @param authorityService AuthorityService instance
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Sets the PersonService instance
     *
     * @param personService PersonService instance
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Sets the NodeService instance
     *
     * @param nodeService NodeService instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        NodeRef filePlanNode = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);

        if (filePlanNode == null)
        {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST,
                        "The default RM site could not be found.");
            return null;
        }

        // construct all the maps etc. needed to build the model
        Map<String, UserModel> usersMap = new HashMap<>(8);
        Map<String, RoleModel> rolesMap = new HashMap<>(8);
        Map<String, GroupModel> groupsMap = new HashMap<>(8);

        // iterate over all the roles for the file plan and construct models
        Set<Role> roles = filePlanRoleService.getRoles(filePlanNode);
        for (Role role : roles)
        {
            // get or create the RoleModel object for current role
            String roleName = role.getName();
            RoleModel roleModel = rolesMap.get(roleName);
            if (roleModel == null)
            {
                roleModel = new RoleModel(role);
                rolesMap.put(roleName, roleModel);
            }

            // get the users for the current RM role
            String group = role.getRoleGroupName();
            Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, group, false);
            roleModel.setUsers(users);

            // setup a user model object for each user
            for (String userName : users)
            {
                UserModel userModel = usersMap.get(userName);
                if (userModel == null)
                {
                    NodeRef userRef = this.personService.getPerson(userName);
                    userModel = new UserModel(userName,
                                (String)this.nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME),
                                (String)this.nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME));
                    usersMap.put(userName, userModel);
                }

                userModel.addRole(roleName);
            }

            // get the groups for the cuurent RM role
            Set<String> groups = authorityService.getContainedAuthorities(AuthorityType.GROUP, group, false);
            roleModel.setGroups(groups);

            // setup a user model object for each user in each group
            for (String groupName : groups)
            {
                GroupModel groupModel = groupsMap.get(groupName);
                if (groupModel == null)
                {
                    groupModel = new GroupModel(groupName,
                                authorityService.getAuthorityDisplayName(groupName));
                    groupsMap.put(groupName, groupModel);
                }

                // get users in each group
                Set<String> groupUsers = this.authorityService.getContainedAuthorities(AuthorityType.USER, groupName, true);
                for (String userName : groupUsers)
                {
                    UserModel userModel = usersMap.get(userName);
                    if (userModel == null)
                    {
                        NodeRef userRef = this.personService.getPerson(userName);
                        userModel = new UserModel(userName,
                                    (String)this.nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME),
                                    (String)this.nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME));
                        usersMap.put(userName, userModel);
                    }

                    userModel.addGroup(groupName);
                    userModel.addRole(roleName);
                    groupModel.addUser(userName);
                }
            }
        }

        // add all the lists data to a Map
        Map<String, Object> reportModel = new HashMap<>(4);
        reportModel.put("users", usersMap);
        reportModel.put("roles", rolesMap);
        reportModel.put("groups", groupsMap);

        // create model object with the lists model
        Map<String, Object> model = new HashMap<>(1);
        model.put("report", reportModel);
        return model;
    }

    /**
     * Class to represent a role for use in a Freemarker template.
     *
     * @author Gavin Cornwell
     */
    public class RoleModel extends Role
    {
        private Set<String> users = new HashSet<>(8);
        private Set<String> groups = new HashSet<>(8);

        public RoleModel(Role role)
        {
            super(role.getName(), role.getDisplayLabel(), role.getCapabilities(), role.getRoleGroupName());
        }

        public void addUser(String username)
        {
            this.users.add(username);
        }

        public void addGroup(String groupName)
        {
            this.groups.add(groupName);
        }

        public void setUsers(Set<String> users)
        {
            this.users = users;
        }

        public void setGroups(Set<String> groups)
        {
            this.groups = groups;
        }

        public Set<String> getUsers()
        {
            return this.users;
        }

        public Set<String> getGroups()
        {
            return this.groups;
        }
    }

    /**
     * Class to represent a user for use in a Freemarker template.
     *
     * @author Gavin Cornwell
     */
    public class UserModel
    {
        private String userName;
        private String firstName;
        private String lastName;
        private Set<String> roles;
        private Set<String> groups;

        public UserModel(String userName, String firstName, String lastName)
        {
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.roles = new HashSet<>(2);
            this.groups = new HashSet<>(2);
        }

        public String getUserName()
        {
            return this.userName;
        }

        public String getFirstName()
        {
            return this.firstName;
        }

        public String getLastName()
        {
            return this.lastName;
        }

        public Set<String> getRoles()
        {
            return this.roles;
        }

        public Set<String> getGroups()
        {
            return this.groups;
        }

        public void addRole(String roleName)
        {
            this.roles.add(roleName);
        }

        public void addGroup(String groupName)
        {
            this.groups.add(groupName);
        }
    }

    /**
     * Class to represent a group for use in a Freemarker template.
     *
     * @author Gavin Cornwell
     */
    public class GroupModel
    {
        private String name;
        private String label;
        private Set<String> users;

        public GroupModel(String name, String label)
        {
            this.name = name;
            this.label = label;
            this.users = new HashSet<>(4);
        }

        public String getName()
        {
            return this.name;
        }

        public String getDisplayLabel()
        {
            return this.label;
        }

        public Set<String> getUsers()
        {
            return this.users;
        }

        public void addUser(String userName)
        {
            this.users.add(userName);
        }
    }
}
