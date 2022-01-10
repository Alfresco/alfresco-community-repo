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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.RMContainerCacheManager;
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
                                            implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                            NodeServicePolicies.OnDeleteChildAssociationPolicy
{
    /** behaviour name */
    private static final String BEHAVIOUR_NAME = "onCreateContainerType";

    /** identifier service */
    protected IdentifierService identifierService;

    /** record service */
    protected RecordService recordService;

    /** record folder service */
    protected RecordFolderService recordFolderService;

    /** RM container cache manager **/
    private RMContainerCacheManager rmContainerCacheManager;

    /** I18N */
    private static final String MSG_CANNOT_CAST_TO_RM_TYPE = "rm.action.cast-to-rm-type";

    /**
     * @param rmContainerCacheManager        RM container cache manager
     *
     */
    public void setRmContainerCacheManager(RMContainerCacheManager rmContainerCacheManager)
    {
        this.rmContainerCacheManager = rmContainerCacheManager;
    }

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
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * Disable the behaviours for this transaction
     *
     * @since 2.3
     */
    public void disable()
    {
        getBehaviour(BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable behaviours for this transaction
     *
     * @since 2.3
     */
    public void enable()
    {
        getBehaviour(BEHAVIOUR_NAME).enable();
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation
     *      (ChildAssociationRef, boolean)
     */
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION,
       policy = "alf:onCreateChildAssociation",
       notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onCreateChildAssoiationFirstEvent(final ChildAssociationRef childAssocRef, final boolean bNew)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                QName parentType = nodeService.getType(childAssocRef.getParentRef());
                boolean isContentSubType = dictionaryService.isSubClass(nodeService.getType(childAssocRef.getChildRef()), ContentModel.TYPE_CONTENT);
                boolean parentIsUnfiledRecordContainer = parentType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
                boolean parentIsUnfiledRecordFolder = parentType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);

                NodeRef child = childAssocRef.getChildRef();
                if((parentIsUnfiledRecordContainer || parentIsUnfiledRecordFolder) && isContentSubType && !recordService.isRecord(child))
                {
                    if (!nodeService.hasAspect(child, ASPECT_FILE_PLAN_COMPONENT))
                    {
                        nodeService.addAspect(child, ASPECT_FILE_PLAN_COMPONENT, null);
                    }
                    if (!nodeService.hasAspect(child, ASPECT_RECORD))
                    {
                        recordService.makeRecord(child);
                    }
                }
                return null;
            }
        });
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(ChildAssociationRef, boolean)
     *
     */
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT,
       name = BEHAVIOUR_NAME
    )
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // Get the elements of the created association
                final NodeRef child = childAssocRef.getChildRef();
                if (nodeService.exists(child))
                {
                    QName childType = convertNodeToFileplanComponent(childAssocRef);

                    // We only care about "folder" or sub-types that are not hidden.
                    // Some modules use hidden files to store information (see RM-3283)
                    if (dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER) &&
                            !nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN))
                    {
                        if (dictionaryService.isSubClass(childType, ContentModel.TYPE_SYSTEM_FOLDER))
                        {
                            // this is a rule container, make sure it is an file plan component
                            nodeService.addAspect(child, ASPECT_FILE_PLAN_COMPONENT, null);
                        }
                        else
                        {
                            // Catch all to generate the rm id (assuming it doesn't already have one!)
                            setIdenifierProperty(child);
                        }
                    }

                    if (rmContainerCacheManager != null)
                    {
                        rmContainerCacheManager.add(child);
                    }
                }

                return null;
            }
        });
    }

    /**
     * Attempts to remove a deleted node from records management root cache
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy#onDeleteAssociation(org.alfresco.service.cmr.repository.AssociationRef)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
	public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // Get the elements of the deleted association
                final NodeRef child = childAssocRef.getChildRef();

                if (rmContainerCacheManager != null)
                {
                    rmContainerCacheManager.remove(child);
                }

                return null;
            }
        });
    }

    /**
     * Set the identifier property
     *
     * @param nodeRef	node reference
     */
    protected void setIdenifierProperty(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                if (nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT) &&
                    nodeService.getProperty(nodeRef, PROP_IDENTIFIER) == null)
                {
                    String id = identifierService.generateIdentifier(nodeRef);
                    nodeService.setProperty(nodeRef, RecordsManagementModel.PROP_IDENTIFIER, id);
                }
                return null;
            }
        });
    }

    /**
     * Converted the child node to a fileplan component
     * The conversion is needed here to be able to generate the identifier
     * If there is no conversion rule for the created type nothing happens and the current type is returned
     *
     * @param childAssocRef reference to the new association
     * @return the new type of the child node
     */
    protected QName convertNodeToFileplanComponent(final ChildAssociationRef childAssocRef)
    {
        NodeRef child = childAssocRef.getChildRef();
        QName childType = nodeService.getType(child);
        QName parentType = nodeService.getType(childAssocRef.getParentRef());

        if(childType.equals(ContentModel.TYPE_FOLDER))
        {
            if(parentType.equals(TYPE_FILE_PLAN))
            {
                nodeService.setType(child, TYPE_RECORD_CATEGORY);
                return TYPE_RECORD_CATEGORY;
            }
            if(parentType.equals(TYPE_RECORD_CATEGORY))
            {
                nodeService.setType(child, TYPE_RECORD_FOLDER);
                return TYPE_RECORD_FOLDER;
            }
            if(parentType.equals(TYPE_UNFILED_RECORD_CONTAINER) || parentType.equals(TYPE_UNFILED_RECORD_FOLDER))
            {
                nodeService.setType(child, TYPE_UNFILED_RECORD_FOLDER);
                return TYPE_UNFILED_RECORD_FOLDER;
            }
        }
        return childType;
    }
}
