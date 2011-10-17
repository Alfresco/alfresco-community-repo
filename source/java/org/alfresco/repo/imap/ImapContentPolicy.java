/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.imap;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.model.ImapModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;


public class ImapContentPolicy
{
    private ActionService actionService;
    private PolicyComponent policyComponent;
    
    /**
     * Init method.  Registered behaviours.
     */
    public void init()
    {  
        PropertyCheck.mandatory(this, "actionService", getActionService());
        PropertyCheck.mandatory(this, "policyComponent", getPolicyComponent());
        
        /**
         * Bind policies
         */
        this.getPolicyComponent().bindClassBehaviour(OnAddAspectPolicy.QNAME, 
            ImapModel.ASPECT_IMAP_CONTENT, 
            new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
        
        /**
         * Bind policies
         */
        this.getPolicyComponent().bindClassBehaviour(OnCopyNodePolicy.QNAME , 
            ImapModel.ASPECT_IMAP_CONTENT, 
            new JavaBehaviour(this, "getCopyCallback", NotificationFrequency.EVERY_EVENT));
    }
    
    /**
     * Called when the imap:imapContent aspect is applied
     * 
     * @param nodeRef The node the aspect is being applied to
     * @param aspectTypeQName The type of aspect being applied (should be imap:imapContent)
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        // ensure the aspect is the one we expect
        if (aspectTypeQName.equals(ImapModel.ASPECT_IMAP_CONTENT))
        {
            Action action = getActionService().createAction(ContentMetadataExtracter.EXECUTOR_NAME);
            if(action != null)
            {
                getActionService().executeAction(action, nodeRef);
            }
        }
    }
    
    /**
     * Extends the default copy behaviour to prevent copying of the imap attatchments.
     * 
     * @author Mark Rogers
     * @since 3.3
     */
    private static class ImapContentCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new ImapContentCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
        
        /**
         * Don't copy IMAP attachments or IMAP folder assocs since they belong to the "source" message, not the "destination" message.
         * 
         * @return          Returns
         *                  {@link AssocCopySourceAction#IGNORE} and
         *                  {@link AssocCopyTargetAction#USE_COPIED_OTHERWISE_ORIGINAL_TARGET}
         */
        /*
         * Note : MER 30/11/2010 For RM this is the correct action since extract attachments is run by the user.
         *    
         * For non RM use cases, we may be expected to extract the attachments automatically for the target node depending upon 
         * the IMAP configuration and destination of the copy, this is not yet attempted since it depends on a bigger re-factor 
         * of the AlfrescoImapFolder ALF-3153  
         */
        @Override
        public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
                    QName classQName,
                    CopyDetails copyDetails,
                    CopyAssociationDetails assocCopyDetails)
        {
            return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                    AssocCopySourceAction.IGNORE,
                    AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET);
        }      
    }

    
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return ImapContentCopyBehaviourCallback.INSTANCE;
    }


    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public ActionService getActionService()
    {
        return actionService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public PolicyComponent getPolicyComponent()
    {
        return policyComponent;
    }
}
