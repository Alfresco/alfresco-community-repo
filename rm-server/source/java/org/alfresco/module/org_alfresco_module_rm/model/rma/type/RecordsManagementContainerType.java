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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * rma:recordsManagementContainer behaviour bean.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:recordsManagementContainer"
)
public class RecordsManagementContainerType extends    BaseBehaviourBean
                                            implements NodeServicePolicies.OnCreateChildAssociationPolicy
{
    /** identifier service */
    protected IdentifierService identifierService;

    /** record service */
    protected RecordService recordService;

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.model.BaseTypeBehaviour#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Get the elements of the created association
                final NodeRef child = childAssocRef.getChildRef();
                if (nodeService.exists(child) == true)
                {
                    QName childType = nodeService.getType(child);

                    // We only care about "folder" or sub-types
                    if (dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER) == true)
                    {
                        if (dictionaryService.isSubClass(childType, ContentModel.TYPE_SYSTEM_FOLDER) == true)
                        {
                            // this is a rule container, make sure it is an file plan component
                            nodeService.addAspect(child, ASPECT_FILE_PLAN_COMPONENT, null);
                        }
                        else
                        {
                            // We need to automatically cast the created folder to RM type if it is a plain folder
                            // This occurs if the RM folder has been created via IMap, WebDav, etc
                            if (nodeService.hasAspect(child, ASPECT_FILE_PLAN_COMPONENT) == false)
                            {
                                // TODO it may not always be a record folder ... perhaps if the current user is a admin it would be a record category??

                                // Assume any created folder is a rma:recordFolder
                                nodeService.setType(child, TYPE_RECORD_FOLDER);
                            }

                            // Catch all to generate the rm id (assuming it doesn't already have one!)
                            setIdenifierProperty(child);
                        }
                    }
                    else
                    {
                        NodeRef parentRef = childAssocRef.getParentRef();
                        QName parentType = nodeService.getType(parentRef);
                        boolean isContentSubType = dictionaryService.isSubClass(childType, ContentModel.TYPE_CONTENT);
                        boolean isUnfiledRecordContainerSubType = dictionaryService.isSubClass(parentType, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
                        if (isContentSubType == true && isUnfiledRecordContainerSubType == true)
                        {
                            if (nodeService.hasAspect(child, ASPECT_FILE_PLAN_COMPONENT) == false)
                            {
                                nodeService.addAspect(child, ASPECT_FILE_PLAN_COMPONENT, null);
                            }
                            if (nodeService.hasAspect(child, ASPECT_RECORD) == false)
                            {
                                recordService.makeRecord(child);
                            }
                        }
                    }
                }

                return null;
            }
        });

    }

    /**
     *
     * @param nodeRef
     */
    protected void setIdenifierProperty(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT) == true &&
                    nodeService.getProperty(nodeRef, PROP_IDENTIFIER) == null)
                {
                    String id = identifierService.generateIdentifier(nodeRef);
                    nodeService.setProperty(nodeRef, RecordsManagementModel.PROP_IDENTIFIER, id);
                }
                return null;
            }
        });
    }
}
