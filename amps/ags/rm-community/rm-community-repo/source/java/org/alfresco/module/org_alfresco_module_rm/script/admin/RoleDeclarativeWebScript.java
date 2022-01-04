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

package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Base declarative web script for role API.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RoleDeclarativeWebScript extends DeclarativeWebScript
{
    /** File plan service */
    protected FilePlanService filePlanService;

    /** File plan role service */
    protected FilePlanRoleService filePlanRoleService;

    /** Authority service */
    protected AuthorityService authorityService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Utility method to get the file plan from the passed parameters.
     *
     * @param req
     * @return
     */
    protected NodeRef getFilePlan(WebScriptRequest req)
    {
        NodeRef filePlan = null;

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String siteId = templateVars.get("siteid");
        if (siteId != null)
        {
            filePlan = filePlanService.getFilePlanBySiteId(siteId);
        }

        if (filePlan == null)
        {
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String id = templateVars.get("id");

            if (!StringUtils.isEmpty(storeType) &&
                !StringUtils.isEmpty(storeId) &&
                !StringUtils.isEmpty(id))
            {
                StoreRef storeRef = new StoreRef(storeType, storeId);
                NodeRef nodeRef = new NodeRef(storeRef, id);
                if (filePlanService.isFilePlan(nodeRef))
                {
                    filePlan = nodeRef;
                }
            }
        }

        if (filePlan == null)
        {
            // Assume we are in a legacy repository and we will grab the default file plan
            filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        }

        return filePlan;
    }

    /**
     * Create role items
     *
     * @param filePlan
     * @param roles
     * @return
     */
    protected Set<RoleItem> createRoleItems(NodeRef filePlan, Set<Role> roles)
    {
        return createRoleItems(filePlan, roles, false);
    }

    /**
     * Create role items
     *
     * @param filePlan
     * @param roles
     * @param showAuths
     * @return
     */
    protected Set<RoleItem> createRoleItems(NodeRef filePlan, Set<Role> roles, boolean showAuths)
    {
        Set<RoleItem> items = new HashSet<>(roles.size());
        for (Role role : roles)
        {
            RoleItem item = null;
            if (showAuths)
            {
                item = new RoleItem(role,
                                    createAuthorityItems(filePlanRoleService.getUsersAssignedToRole(filePlan, role.getName())),
                                    createAuthorityItems(filePlanRoleService.getGroupsAssignedToRole(filePlan, role.getName())));
            }
            else
            {
                item = new RoleItem(role);
            }
            items.add(item);
        }
        return items;
    }

    /**
     * Create authority items
     *
     * @param authorities
     * @return
     */
    private Set<AuthorityItem> createAuthorityItems(Set<String> authorities)
    {
        Set<AuthorityItem> result = new HashSet<>(authorities.size());

        for (String authority : authorities)
        {
            String displayLabel = authority;
            if (!AuthorityType.getAuthorityType(authority).equals(AuthorityType.USER))
            {
                displayLabel = authorityService.getAuthorityDisplayName(authority);
            }
            result.add(new AuthorityItem(authority, displayLabel));
        }

        return result;
    }

    /**
     * Role Item Helper Class
     *
     * @author Roy Wetherall
     * @since 2.1
     */
    public class RoleItem
    {
        private String name;
        private String groupShortName;
        private String displayLabel;
        private Set<Capability> capabilities;
        private boolean showAuths = false;
        private Set<AuthorityItem> assignedUsers;
        private Set<AuthorityItem> assignedGroups;

        public RoleItem(Role role)
        {
            this.name = role.getName();
            this.displayLabel = role.getDisplayLabel();
            this.capabilities = role.getCapabilities();
        }

        public RoleItem(Role role, Set<AuthorityItem> assignedUsers, Set<AuthorityItem> assignedGroups)
        {
            this.name = role.getName();
            this.groupShortName = role.getGroupShortName();
            this.displayLabel = role.getDisplayLabel();
            this.capabilities = role.getCapabilities();
            this.showAuths = true;
            this.assignedUsers = assignedUsers;
            this.assignedGroups = assignedGroups;
        }

        public String getName()
        {
            return name;
        }

        public String getGroupShortName()
        {
            return groupShortName;
        }

        public String getDisplayLabel()
        {
            return displayLabel;
        }

        public Set<Capability> getCapabilities()
        {
            return capabilities;
        }

        public boolean getShowAuths()
        {
            return showAuths;
        }

        public Set<AuthorityItem> getAssignedGroups()
        {
            return assignedGroups;
        }

        public Set<AuthorityItem> getAssignedUsers()
        {
            return assignedUsers;
        }
    }

    /**
     * Authority Item Helper Class
     *
     * @author Roy Wetherall
     * @since 2.1
     */
    public class AuthorityItem
    {
        private String name;
        private String displayLabel;

        public AuthorityItem(String name, String displayLabel)
        {
            this.name = name;
            this.displayLabel = displayLabel;
        }

        public String getName()
        {
            return name;
        }

        public String getDisplayLabel()
        {
            return displayLabel;
        }
    }
}
