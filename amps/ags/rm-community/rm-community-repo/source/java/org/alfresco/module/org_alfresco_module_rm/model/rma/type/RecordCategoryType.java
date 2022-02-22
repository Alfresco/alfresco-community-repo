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

import static org.alfresco.model.ContentModel.TYPE_CONTENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.AbstractDisposableItem;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
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
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * rma:recordCategory behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:recordCategory"
)
public class RecordCategoryType extends AbstractDisposableItem
                                implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                           NodeServicePolicies.OnCreateNodePolicy,
                                           NodeServicePolicies.OnMoveNodePolicy
{
    private final static List<QName> ACCEPTED_UNIQUE_CHILD_TYPES = new ArrayList<>();
    private final static List<QName> ACCEPTED_NON_UNIQUE_CHILD_TYPES = Arrays.asList(TYPE_RECORD_CATEGORY, TYPE_RECORD_FOLDER);

    /** vital record service */
    protected VitalRecordService vitalRecordService;

    /** file plan permission service */
    protected FilePlanPermissionService filePlanPermissionService;

    /**
     * @param vitalRecordService    vital record service
     */
    public void setVitalRecordService(VitalRecordService vitalRecordService)
    {
        this.vitalRecordService = vitalRecordService;
    }

    /**
     * @param filePlanPermissionService file plan permission service
     */
    public void setFilePlanPermissionService(FilePlanPermissionService filePlanPermissionService)
    {
        this.filePlanPermissionService = filePlanPermissionService;
    }

    /**
     * On every event
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        QName childType = nodeService.getType(childAssocRef.getChildRef());

        // We need to automatically cast the created folder to record folder if it is a plain folder
        // This occurs if the RM folder has been created via IMap, WebDav, etc. Don't check subtypes.
        // Some modules use hidden folders to store information (see RM-3283).
        if (childType.equals(ContentModel.TYPE_FOLDER))
        {
            nodeService.setType(childAssocRef.getChildRef(), TYPE_RECORD_FOLDER);
        }

        validateNewChildAssociation(childAssocRef.getParentRef(), childAssocRef.getChildRef(), ACCEPTED_UNIQUE_CHILD_TYPES, ACCEPTED_NON_UNIQUE_CHILD_TYPES);

        if (bNew)
        {
            // setup the record folder
            recordFolderService.setupRecordFolder(childAssocRef.getChildRef());
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
    public void onCreateChildAssociationOnCommit(ChildAssociationRef childAssocRef, final boolean bNew)
    {
        final NodeRef child = childAssocRef.getChildRef();

        behaviourFilter.disableBehaviour();
        try
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // setup vital record definition
                    if(nodeService.exists(child))
                    {
                        vitalRecordService.setupVitalRecordDefinition(child);
                    }

                    return null;
                }
            });
        }
        finally
        {
            behaviourFilter.enableBehaviour();
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateNode(final ChildAssociationRef childAssocRef)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("rma:recordCategory|alf:onCreateNode|this.onCreateNode()|TRANSATION_COMMIT");
        }

        // execute behaviour code as system user
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // setup record category permissions
                if(nodeService.exists(childAssocRef.getChildRef()))
                {
                    filePlanPermissionService.setupRecordCategoryPermissions(childAssocRef.getChildRef());
                }

                return null;
            }
        });

    }

    /**
     * Record Category move behaviour
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
        // clean the child folders and records only if the old parent category has a disposition schedule set
        // if it doesn't, then there are no old properties on the child nodes that have to be cleaned in order
        // for new ones to be set
        if (nodeService.getType(newChildAssocRef.getChildRef()).equals(TYPE_RECORD_CATEGORY)
                && dispositionService.getDispositionSchedule(oldChildAssocRef.getParentRef()) != null)
        {
            reinitializeRecordFolders(newChildAssocRef);
        }
    }

    /**
     *  Recursively reinitialize each folder in a structure of categories
     *  Unwanted aspects will be removed from the child records and the records will be re-filed
     *  Disposition schedule aspects and properties will be inherited from the new parent category
     *
     * @param childAssociationRef
     */
    private void reinitializeRecordFolders(ChildAssociationRef childAssociationRef)
    {
        for (ChildAssociationRef newChildRef : nodeService.getChildAssocs(childAssociationRef.getChildRef(),
                ContentModel.ASSOC_CONTAINS,
                RegexQNamePattern.MATCH_ALL))
        {
            if (nodeService.getType(newChildRef.getChildRef()).equals(TYPE_RECORD_CATEGORY))
            {
                reinitializeRecordFolders(newChildRef);
            }
            else if (!nodeService.getType(newChildRef.getChildRef()).equals(TYPE_CONTENT))
            {
                reinitializeRecordFolder(newChildRef);
            }
        }
    }

    /**
     * Copy callback for record category
     */
    @Behaviour
    (
        kind = BehaviourKind.CLASS,
        policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback onCopyRecordCategory(final QName classRef, final CopyDetails copyDetails)
    {
        return new DefaultCopyBehaviourCallback()
        {
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
                return !nodeService.getType(copyDetails.getTargetParentNodeRef()).equals(TYPE_RECORD_FOLDER);
            }
        };
    }
}
