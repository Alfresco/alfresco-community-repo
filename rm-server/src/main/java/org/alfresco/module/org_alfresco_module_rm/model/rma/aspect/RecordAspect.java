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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelAccessDeniedException;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
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
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

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
public class RecordAspect extends    BaseBehaviourBean
                          implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                     RecordsManagementPolicies.OnCreateReference,
                                     RecordsManagementPolicies.OnRemoveReference,
                                     NodeServicePolicies.OnMoveNodePolicy,
                                     NodeServicePolicies.OnUpdatePropertiesPolicy
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
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
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
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef thumbnail = childAssocRef.getChildRef();

                if (nodeService.exists(thumbnail) == true)
                {
                    // apply file plan component aspect to thumbnail
                    nodeService.addAspect(thumbnail, ASPECT_FILE_PLAN_COMPONENT, null);

                    // manage any extended readers
                    NodeRef parent = childAssocRef.getParentRef();
                    Set<String> readers = extendedSecurityService.getExtendedReaders(parent);
                    Set<String> writers = extendedSecurityService.getExtendedWriters(parent);
                    if (readers != null && readers.size() != 0)
                    {
                        extendedSecurityService.addExtendedSecurity(thumbnail, readers, writers, false);
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
    public void onCreateReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        // Deal with versioned records
        if (reference.equals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions")) == true)
        {
            // Apply the versioned aspect to the from node
            nodeService.addAspect(fromNodeRef, ASPECT_VERSIONED_RECORD, null);
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
    public void onRemoveReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        // Deal with versioned records
        if (reference.equals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions")) == true)
        {
            // Apply the versioned aspect to the from node
            nodeService.removeAspect(fromNodeRef, ASPECT_VERSIONED_RECORD);
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
        if (oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()) == false)
        {
            final NodeRef newNodeRef = newChildAssocRef.getChildRef();
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    if (nodeService.exists(newNodeRef) == true)
                    {
                        // only remove the search details .. the rest will be resolved automatically
                        nodeService.removeAspect(newNodeRef, RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH);
                    }

                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
    }
    
    /**
     * Ensure that the user only updates record properties that they have permission to.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            name = "onUpdateProperties",
            kind = BehaviourKind.CLASS
    )
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        if (AuthenticationUtil.getFullyAuthenticatedUser() != null &&
            AuthenticationUtil.isRunAsUserTheSystemUser() == false &&
            nodeService.exists(nodeRef) == true)
        {
            if (recordService.isRecord(nodeRef) == true)
            {
                for (QName property : after.keySet())
                {
                    Serializable beforeValue = null;
                    if (before != null)
                    {
                        beforeValue = before.get(property);
                    }

                    Serializable afterValue = null;
                    if (after != null)
                    {
                        afterValue = after.get(property);
                    }

                    boolean propertyUnchanged = false;
                    if (beforeValue != null && afterValue != null &&
                        beforeValue instanceof Date && afterValue instanceof Date)
                    {
                        // deal with date values
                        propertyUnchanged = (((Date)beforeValue).compareTo((Date)afterValue) == 0);
                    }
                    else
                    {
                        // otherwise
                        propertyUnchanged = EqualsHelper.nullSafeEquals(beforeValue, afterValue);
                    }

                    if (propertyUnchanged == false &&
                        recordService.isPropertyEditable(nodeRef, property) == false)
                    {
                        // the user can't edit the record property
                        throw new ModelAccessDeniedException(
                            "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                            " does not have the permission to edit the record property " + property.toString() +
                            " on the node " + nodeRef.toString());
                    }
                }
            }
        }
    }
    
    /**
     * Executes a reference script if present
     *
     * @param policy
     * @param reference
     * @param from
     * @param to
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
            Map<String, Object> objectModel = new HashMap<String, Object>(1);
            objectModel.put("node", from);
            objectModel.put("toNode", to);
            objectModel.put("policy", policy);
            objectModel.put("reference", referenceId);

            scriptService.executeScript(scriptNodeRef, null, objectModel);
        }
    }
}
