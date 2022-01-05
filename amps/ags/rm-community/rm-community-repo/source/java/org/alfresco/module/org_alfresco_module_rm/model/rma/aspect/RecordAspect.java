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

package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.AbstractDisposableItem;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.ContentBinDuplicationUtility;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:record behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:record"
)
public class RecordAspect extends    AbstractDisposableItem
                          implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                     NodeServicePolicies.BeforeAddAspectPolicy,
                                     RecordsManagementPolicies.OnCreateReference,
                                     RecordsManagementPolicies.OnRemoveReference,
                                     NodeServicePolicies.OnMoveNodePolicy,
                                     CopyServicePolicies.OnCopyCompletePolicy,
                                     ContentServicePolicies.OnContentPropertyUpdatePolicy
{
    /** Well-known location of the scripts folder. */
    // TODO make configurable
    private NodeRef scriptsFolderNodeRef = new NodeRef("workspace", "SpacesStore", "rm_behavior_scripts");

    /** extended security service */
    protected ExtendedSecurityService extendedSecurityService;

    /** script service */
    protected ScriptService scriptService;

    /** record service */
    protected RecordService recordService;

    /** quickShare service */
    private QuickShareService quickShareService;

    /** Utility class for duplicating content */
    private ContentBinDuplicationUtility contentBinDuplicationUtility;

    /** I18N */
    private static final String MSG_CANNOT_UPDATE_RECORD_CONTENT = "rm.service.update-record-content";

    /**
     * @param extendedSecurityService   extended security service
     */
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    /**
     * @param scriptService script service
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }

    /**
     * @param recordService     record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     *
     * @param quickShareService
     */
    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }

    /**
     * Setter for content duplication utility class
     * @param contentBinDuplicationUtility ContentBinDuplicationUtility
     */
    public void setContentBinDuplicationUtility(ContentBinDuplicationUtility contentBinDuplicationUtility)
    {
        this.contentBinDuplicationUtility = contentBinDuplicationUtility;
    }

    /**
     * Behaviour to ensure renditions have the appropriate extended security.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION,
       assocType = "rn:rendition",
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, boolean bNew)
    {
        authenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                NodeRef thumbnail = childAssocRef.getChildRef();

                if (nodeService.exists(thumbnail))
                {
                    // apply file plan component aspect to thumbnail
                    nodeService.addAspect(thumbnail, ASPECT_FILE_PLAN_COMPONENT, null);

                    // manage any extended readers
                    NodeRef parent = childAssocRef.getParentRef();
                    Set<String> readers = extendedSecurityService.getReaders(parent);
                    Set<String> writers = extendedSecurityService.getWriters(parent);
                    if (readers != null && readers.size() != 0)
                    {
                        extendedSecurityService.set(thumbnail, readers, writers);
                    }
                }

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference#onCreateReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateReference(final NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        // Deal with versioned records
        if (reference.equals(CUSTOM_REF_VERSIONS))
        {
            // run as system, to apply the versioned aspect to the from node
            // as we can't be sure if the user has add aspect rights
            authenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    nodeService.addAspect(fromNodeRef, ASPECT_VERSIONED_RECORD, null);
                    return null;
                }
            });
        }

        // Execute script if for the reference event
        executeReferenceScript("onCreate", reference, fromNodeRef, toNodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRemoveReference#onRemoveReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onRemoveReference(final NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        // Deal with versioned records
        if (reference.equals(CUSTOM_REF_VERSIONS))
        {
            authenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // Apply the versioned aspect to the from node
                    nodeService.removeAspect(fromNodeRef, ASPECT_VERSIONED_RECORD);

                    return null;
                }
            });
        }

        // Execute script if for the reference event
        executeReferenceScript("onRemove", reference, fromNodeRef, toNodeRef);
    }

    /**
     * Record copy callback
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
            public Map<QName, Serializable> getCopyProperties(QName classRef, CopyDetails copyDetails,
                    Map<QName, Serializable> properties)
            {
                Map<QName, Serializable> sourceProperties = super.getCopyProperties(classRef, copyDetails, properties);

                // Remove the Date Filed property from record properties on copy.
                // It will be generated for the copy
                if (sourceProperties.containsKey(PROP_DATE_FILED))
                {
                    sourceProperties.remove(PROP_DATE_FILED);
                }

                return sourceProperties;
            }

        };
    }

    /**
     * Record move behaviour
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
        // check the records parent has actually changed
        if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()) &&
            isFilePlanComponent(oldChildAssocRef.getParentRef()))
        {
            final NodeRef record = newChildAssocRef.getChildRef();
            authenticationUtil.runAsSystem(new RunAsWork<Object>()
            {
                public Object doWork()
                {
                    if (nodeService.exists(record) &&
                        recordService.isFiled(record))
                    {
                        // clean record
                        cleanDisposableItem(nodeService, record);

                        // re-file in the new folder
                        recordService.file(record);
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Executes a reference script if present
     *
     * @param policy        policy
     * @param reference     reference
     * @param from          reference from
     * @param to            reference to
     */
    private void executeReferenceScript(String policy, QName reference, NodeRef from, NodeRef to)
    {
        String referenceId = reference.getLocalName();

        // This is the filename pattern which is assumed.
        // e.g. a script file onCreate_superceded.js for the creation of a superseded reference
        String expectedScriptName = policy + "_" + referenceId + ".js";

        NodeRef scriptNodeRef = nodeService.getChildByName(scriptsFolderNodeRef, ContentModel.ASSOC_CONTAINS, expectedScriptName);
        if (scriptNodeRef != null)
        {
            Map<String, Object> objectModel = new HashMap<>(1);
            objectModel.put("node", from);
            objectModel.put("toNode", to);
            objectModel.put("policy", policy);
            objectModel.put("reference", referenceId);

            scriptService.executeScript(scriptNodeRef, null, objectModel);
        }
    }

    /**
     * On copy complete behaviour for record aspect.
     *
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy#onCopyComplete(QName, NodeRef, NodeRef, boolean, Map)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS
    )
    public void onCopyComplete(QName classRef,
                               NodeRef sourceNodeRef,
                               NodeRef targetNodeRef,
                               boolean copyToNewNode,
                               Map<NodeRef, NodeRef> copyMap)
    {
        // given the node exists and is a record
        if (nodeService.exists(targetNodeRef) &&
            nodeService.hasAspect(targetNodeRef, ASPECT_RECORD))
        {
            // then remove any extended security from the newly copied record
            extendedSecurityService.remove(targetNodeRef);

            //create a new content URL for the copy
            contentBinDuplicationUtility.duplicate(targetNodeRef);
        }
    }

    /**
     * Behaviour to remove the shared link before declare a record
     * and to create new bin if the node is a copy or has copies
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeAddAspectPolicy#beforeAddAspect(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour(kind = BehaviourKind.CLASS, notificationFrequency = NotificationFrequency.FIRST_EVENT)
    public void beforeAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                String sharedId = (String) nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                if (sharedId != null)
                {
                    quickShareService.unshareContent(sharedId);
                }

                // if the node has a copy or is a copy of an existing node
                if (!nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL).isEmpty() ||
                        !nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL).isEmpty())
                {
                    contentBinDuplicationUtility.duplicate(nodeRef);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    @Override
    @Behaviour
    (
        kind = BehaviourKind.CLASS,
        notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue)
    {
        // Allow creation of content but not update
        if (beforeValue != null)
        {
            throw new IntegrityException(I18NUtil.getMessage(MSG_CANNOT_UPDATE_RECORD_CONTENT), null);
        }
    }
}
