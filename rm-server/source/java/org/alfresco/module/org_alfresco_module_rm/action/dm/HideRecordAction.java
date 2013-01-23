/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hides a record within a collaboration site.
 *
 * Note: This is a 'normal' dm action, rather than a records management action.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class HideRecordAction extends ActionExecuterAbstractBase implements RecordsManagementModel
{

    /** Logger */
    private static Log logger = LogFactory.getLog(HideRecordAction.class);

    /** Action name */
    public static final String NAME = "hide-record";

    /** Node service */
    private NodeService nodeService;

    /** Permission service */
    private PermissionService permissionService;

    /** Extended security service */
    private ExtendedSecurityService extendedSecurityService;

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param extendedSecurityService   extended security service
     */
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD) == false)
        {
            // We cannot hide a document which is not a record
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Cannot hide the document, because '" + actionedUponNodeRef.toString() + "' is not a record.");
            }
        }
        else if (permissionService.hasPermission(actionedUponNodeRef, PermissionService.WRITE) != AccessStatus.ALLOWED)
        {
            // We do a sanity check to ensure that the user has at least write permissions on the record
            throw new AccessDeniedException("Cannot hide record, because the user '" + AuthenticationUtil.getFullyAuthenticatedUser() + "' does not have write permissions on the record '" + actionedUponNodeRef.toString() + "'.");
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_HIDDEN) == true)
        {
            // We cannot hide records which are already hidden
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Cannot hide record, because '" + actionedUponNodeRef.toString() + "' is already hidden.");
            }
        }
        else
        {
            // remove the child association
            NodeRef originalLocation = (NodeRef) nodeService.getProperty(actionedUponNodeRef, PROP_ORIGINAL_LOCATION);
            List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(actionedUponNodeRef);
            for (ChildAssociationRef childAssociationRef : parentAssocs)
            {
                if (childAssociationRef.isPrimary() == false && childAssociationRef.getParentRef().equals(originalLocation))
                {
                    nodeService.removeChildAssociation(childAssociationRef);
                    break;
                }
            }

            // remove the extended security from the node ... this prevents the users from continuing to see the record in searchs and other linked locations
            extendedSecurityService.removeAllExtendedReaders(actionedUponNodeRef);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Intentionally empty
    }
}
