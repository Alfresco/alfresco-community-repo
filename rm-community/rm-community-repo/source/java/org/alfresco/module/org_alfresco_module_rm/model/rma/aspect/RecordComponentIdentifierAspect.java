/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:recordComponentIdentifier behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:recordComponentIdentifier"
)
public class RecordComponentIdentifierAspect extends    BaseBehaviourBean
                                             implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                                        NodeServicePolicies.BeforeDeleteNodePolicy,
                                                        NodeServicePolicies.OnCreateNodePolicy,
                                                        CopyServicePolicies.OnCopyCompletePolicy
{
    /** I18N */
    private static final String MSG_SET_ID = "rm.service.set-id";

    /** attribute context value */
    private static final String CONTEXT_VALUE = "rma:identifier";

    /** file plan service */
    private FilePlanService filePlanService;

    /** attribute service */
    private AttributeService attributeService;

    /** identifier service */
    private IdentifierService identifierService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param attributeService  attribute service
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * Ensures that the {@link RecordsManagementModel#PROP_IDENTIFIER rma:identifier} property remains
     * unique within the context of the parent node.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.EVERY_EVENT
    )
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                String newIdValue = (String)after.get(PROP_IDENTIFIER);
                if (newIdValue != null)
                {
                    String oldIdValue = (String)before.get(PROP_IDENTIFIER);
                    if (oldIdValue != null && !oldIdValue.equals(newIdValue) && !isRecordIdentifierEditable(nodeRef))
                    {
                        throw new IntegrityException(I18NUtil.getMessage(MSG_SET_ID, nodeRef.toString()), null);
                    }

                    // update uniqueness
                    updateUniqueness(nodeRef, oldIdValue, newIdValue);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Utility method that checks if a record's identifier is temporarily editable
     * @param record the record to check
     * @return true if it is editable false otherwise
     */
    private boolean isRecordIdentifierEditable(NodeRef record)
    {
        Boolean isEditableProperty = (Boolean)nodeService.getProperty(record, RecordsManagementModel.PROP_ID_IS_TEMPORARILY_EDITABLE);
        return isEditableProperty == null ? false : isEditableProperty;
    }

    /**
     * Cleans up the {@link RecordsManagementModel#PROP_IDENTIFIER rma:identifier} property unique triplet.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.EVERY_EVENT
    )
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                String beforeId = (String) nodeService.getProperty(nodeRef, PROP_IDENTIFIER);
                updateUniqueness(nodeRef, beforeId, null);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Record component identifier aspect copy callback
     */
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new DoNothingCopyBehaviourCallback();
    }

    @SuppressWarnings("rawtypes")
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map copyMap)
    {
        //Generate the id for the copy
        String id = identifierService.generateIdentifier(
                                            nodeService.getType(nodeService.getPrimaryParent(targetNodeRef).getParentRef()),
                                            nodeService.getPrimaryParent(targetNodeRef).getParentRef());

        //We need to allow the id to be overwritten disable the policy protecting changes to the id
        behaviourFilter.disableBehaviour();
        try
        {
            nodeService.setProperty(targetNodeRef, PROP_IDENTIFIER, id);
        }
        finally
        {
            behaviourFilter.enableBehaviour();
        }
    }

    /**
     * Updates the uniqueness check using the values provided.  If the after value is <tt>null</tt>
     * then this is considered to be a removal.
     *
     * @param nodeRef   node reference
     * @param beforeId  id before
     * @param afterId   id after
     */
    private void updateUniqueness(NodeRef nodeRef, String beforeId, String afterId)
    {
        NodeRef contextNodeRef = filePlanService.getFilePlan(nodeRef);

        if (beforeId == null)
        {
            if (afterId != null)
            {
                // Just create it
                attributeService.createAttribute(null, CONTEXT_VALUE, contextNodeRef, afterId);
            }
        }
        else if (afterId == null)
        {
            // The before value was not null, so remove it
            attributeService.removeAttribute(CONTEXT_VALUE, contextNodeRef, beforeId);
            // Do a blanket removal in case this is a contextual nodes
            attributeService.removeAttributes(CONTEXT_VALUE, nodeRef);
        }
        else
        {
            // This is a full update
            attributeService.updateOrCreateAttribute(
                    CONTEXT_VALUE, contextNodeRef, beforeId,
                    CONTEXT_VALUE, contextNodeRef, afterId);
        }
    }

    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    @Override
    public void onCreateNode(final ChildAssociationRef childAssocRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                /*
                 * When creating a new record the identifier is writable to allow the upload in multiple steps.
                 * On transaction commit make the identifier read only (remove the editable aspect).
                 */
                nodeService.setProperty(childAssocRef.getChildRef(), RecordsManagementModel.PROP_ID_IS_TEMPORARILY_EDITABLE, false);
                return null;
            }
        });
    }
}
