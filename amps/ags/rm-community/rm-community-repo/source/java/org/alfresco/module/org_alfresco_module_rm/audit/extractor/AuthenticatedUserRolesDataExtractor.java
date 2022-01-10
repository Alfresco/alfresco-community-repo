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

package org.alfresco.module.org_alfresco_module_rm.audit.extractor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * An extractor that uses a node context to determine the currently-authenticated
 * user's RM roles.  This is not a data generator because it can only function in
 * the context of a given node.
 *
 * @author Derek Hulley
 * @since 3.2
 */
public final class AuthenticatedUserRolesDataExtractor extends AbstractDataExtractor
{
    private NodeService nodeService;
    private FilePlanService filePlanService;
    private FilePlanRoleService filePlanRoleService;
    private DictionaryService dictionaryService;

    /**
     * Used to check that the node in the context is a fileplan component
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

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
     * @param dictionaryService	dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return  Returns <tt>true</tt> if the data is a NodeRef and it represents either a fileplan component or
     *          a subtype of content
     */
    public boolean isSupported(Serializable data)
    {
        if (!(data instanceof NodeRef))
        {
            return false;
        }
        NodeRef nodeRef = (NodeRef) data;
        return nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT)  ||
                dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_CONTENT);
    }

    /**
     * @see org.alfresco.repo.audit.extractor.DataExtractor#extractData(java.io.Serializable)
     */
    public Serializable extractData(Serializable value)
    {
        NodeRef nodeRef = (NodeRef) value;
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        if (user == null)
        {
            // No-one is authenticated
            return null;
        }

        StringBuilder sb = new StringBuilder(100);
        
        // Get the rm root
        NodeRef rmRootNodeRef = filePlanService.getFilePlan(nodeRef);

        // if we don't have an rm root and the given node is a subtype of content
        if (rmRootNodeRef == null &&
            dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_CONTENT))
        {
            // use the default file plan
            rmRootNodeRef = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        }

        if (rmRootNodeRef != null)
        {
            Set<Role> roles = filePlanRoleService.getRolesByUser(rmRootNodeRef, user);
            for (Role role : roles)
            {
                if (sb.length() > 0)
                {
                    sb.append(", ");
                }
                sb.append(role.getDisplayLabel());
            }
        }

        // Done
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        AuthenticatedUserRolesDataExtractor that = (AuthenticatedUserRolesDataExtractor) o;
        return Objects.equals(nodeService, that.nodeService) && Objects.equals(filePlanService, that.filePlanService)
            && Objects.equals(filePlanRoleService, that.filePlanRoleService);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nodeService, filePlanService, filePlanRoleService);
    }
}
