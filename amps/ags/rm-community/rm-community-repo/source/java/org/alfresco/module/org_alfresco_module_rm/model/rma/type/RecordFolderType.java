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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.AbstractDisposableItem;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:recordFolder behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:recordFolder"
)
public class RecordFolderType extends    AbstractDisposableItem
                              implements NodeServicePolicies.OnMoveNodePolicy,
                                         NodeServicePolicies.OnCreateChildAssociationPolicy
{

    /** vital record service */
    protected VitalRecordService vitalRecordService;

    /** identifier service */
    protected IdentifierService identifierService;

    /** I18N */
    private static final String MSG_CANNOT_CREATE_RECORD_FOLDER_CHILD = "rm.action.create.record.folder.child-error-message";

    private static final String MSG_CANNOT_CREATE_CHILDREN_IN_CLOSED_RECORD_FOLDER = "rm.service.add-children-to-closed-record-folder";

    /**
     * @param vitalRecordService    vital record service
     */
    public void setVitalRecordService(VitalRecordService vitalRecordService)
    {
        this.vitalRecordService = vitalRecordService;
    }

    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * Record folder move behaviour
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (!nodeService.getType(newChildAssocRef.getParentRef()).equals(TYPE_RECORD_FOLDER))
        {
            if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()))
            {
                reinitializeRecordFolder(newChildAssocRef);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Cannot move record folder into another record folder.");
        }
    }

    /**
     * Record folder copy callback
     */
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback getCopyCallback(final QName classRef, final CopyDetails copyDetails)
    {
        return new DefaultCopyBehaviourCallback()
        {
            @Override
            public Map<QName, Serializable> getCopyProperties(QName classRef, CopyDetails copyDetails,  Map<QName, Serializable> properties)
            {
                Map<QName, Serializable> sourceProperties = super.getCopyProperties(classRef, copyDetails, properties);

                // ensure that the 'closed' status of the record folder is not copied
                if (sourceProperties.containsKey(PROP_IS_CLOSED))
                {
                    sourceProperties.remove(PROP_IS_CLOSED);
                }

                return sourceProperties;
            }

            /**
             * If the targets parent is a Record Folder -- Do Not Allow Copy
             *
             * @param classQName
             * @param copyDetails
             * @return boolean
             */
            @Override
            public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
            {
                boolean targetParentIsRecordFolder = nodeService.getType(copyDetails.getTargetParentNodeRef())
                            .equals(TYPE_RECORD_FOLDER);
                boolean containsUnwantedAspect = ArrayUtils.contains(unwantedAspects, classQName);
                return !(targetParentIsRecordFolder || containsUnwantedAspect);
            }
        };
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
             kind = BehaviourKind.ASSOCIATION,
             notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();

        if (nodeService.exists(nodeRef))
        {
            boolean notFolderOrRmFolderSubType = !instanceOf(nodeRef, ContentModel.TYPE_FOLDER) ||
                                              instanceOf(nodeRef, RecordsManagementModel.TYPE_RECORDS_MANAGEMENT_CONTAINER) ||
                                              instanceOf(nodeRef, RecordsManagementModel.TYPE_RECORD_FOLDER) ||
                                              instanceOf(nodeRef, RecordsManagementModel.TYPE_TRANSFER);

            if (!instanceOf(nodeRef, ContentModel.TYPE_CONTENT) && notFolderOrRmFolderSubType)
            {
                throw new IntegrityException(I18NUtil.getMessage(MSG_CANNOT_CREATE_RECORD_FOLDER_CHILD, nodeService.getType(nodeRef)), null);
            }
            // ensure nothing is being added to a closed record folder
            NodeRef recordFolder = childAssocRef.getParentRef();
            Boolean isClosed = (Boolean) nodeService.getProperty(recordFolder, PROP_IS_CLOSED);
            if (isClosed != null && isClosed)
            {
                throw new IntegrityException(I18NUtil.getMessage(MSG_CANNOT_CREATE_CHILDREN_IN_CLOSED_RECORD_FOLDER), null);
            }
        }
    }

    /**
     * On transaction commit
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION,
       policy = "alf:onCreateChildAssociation",
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateChildAssociationOnCommit(ChildAssociationRef childAssocRef, boolean bNew)
    {
        final NodeRef child = childAssocRef.getChildRef();

        if(!nodeService.exists(child))
        {
            return;
        }

        // only records can be added in a record folder or hidden folders(is the case of e-mail attachments)
        if (instanceOf(child, ContentModel.TYPE_FOLDER) && !nodeService.hasAspect(child, ContentModel.ASPECT_HIDDEN))
        {
            throw new IntegrityException(I18NUtil.getMessage(MSG_CANNOT_CREATE_RECORD_FOLDER_CHILD, nodeService.getType(child)), null);
        }

        behaviourFilter.disableBehaviour();
        try
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // setup vital record definition
                    vitalRecordService.setupVitalRecordDefinition(child);

                    return null;
                }
            });
        }
        finally
        {
            behaviourFilter.enableBehaviour();
        }
    }
}
