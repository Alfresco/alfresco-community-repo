/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * rma:filePlan behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:filePlan"
)
public class FilePlanType extends    BaseBehaviourBean
                          implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                     NodeServicePolicies.OnCreateNodePolicy,
                                     NodeServicePolicies.OnDeleteNodePolicy
{
    /** file plan service */
    protected FilePlanService filePlanService;

    /** record folder service */
    protected RecordFolderService recordFolderService;

    /** identifier service */
    protected IdentifierService identifierService;

    /** file plan role service */
    protected FilePlanRoleService filePlanRoleService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION
    )
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        // ensure we are not trying to put content in the file plan root node
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (instanceOf(nodeRef, ContentModel.TYPE_CONTENT))
        {
            throw new AlfrescoRuntimeException("Operation failed, because you can't place content in the root of the file plan.");
        }

        // ensure we are not trying to put a record folder in the root of the file plan
        NodeRef parent = childAssocRef.getParentRef();
        if (filePlanService.isFilePlan(parent) && recordFolderService.isRecordFolder(nodeRef))
        {
            throw new AlfrescoRuntimeException("Operation failed, because you can not place a record folder in the root of the file plan.");
        }

    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    @Override
    public void onCreateNode(final ChildAssociationRef childAssocRef)
    {
        final NodeRef filePlan = childAssocRef.getChildRef();

        AuthenticationUtil.runAsSystem(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                if (nodeService.hasAspect(filePlan, ASPECT_FILE_PLAN_COMPONENT) &&
                    nodeService.getProperty(filePlan, PROP_IDENTIFIER) == null)
                {
                    String id = identifierService.generateIdentifier(filePlan);
                    nodeService.setProperty(filePlan, RecordsManagementModel.PROP_IDENTIFIER, id);
                }

                return null;
            }
        });

        // setup the file plan roles
        filePlanRoleService.setupFilePlanRoles(filePlan);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy#onDeleteNode(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean archived)
    {
        // tear down the file plan roles
        filePlanRoleService.tearDownFilePlanRoles(childAssocRef.getChildRef());
    }
}
