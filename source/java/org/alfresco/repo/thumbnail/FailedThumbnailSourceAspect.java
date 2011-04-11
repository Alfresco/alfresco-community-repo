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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Behaviour/Policies for the {@link ContentModel#ASPECT_FAILED_THUMBNAIL_SOURCE} aspect.
 * When the last {@link ContentModel#TYPE_FAILED_THUMBNAIL} child is deleted from under
 * a source node, then all failures are considered removed and the {@link ContentModel#ASPECT_FAILED_THUMBNAIL_SOURCE}
 * aspect can be removed.
 * <p/>
 * Also, any {@link ContentModel#TYPE_FAILED_THUMBNAIL failed thumbnails} should be
 * removed from the model onUpdateProperties as the new content may have become thumbnailable.

 * @author Neil Mc Erlean
 * @since 3.5.0
 */
public class FailedThumbnailSourceAspect implements NodeServicePolicies.OnDeleteNodePolicy,
                                                    NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private static final Log log = LogFactory.getLog(FailedThumbnailSourceAspect.class);

    /** Services */
    private BehaviourFilter behaviourFilter;
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private ThumbnailService thumbnailService;
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                OnDeleteNodePolicy.QNAME, 
                ContentModel.TYPE_FAILED_THUMBNAIL, 
                new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE, 
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }
    
    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        // When a failedThumbnail node has been deleted, we should check if there are any other
        // failedThumbnail peer nodes left.
        // If there are not, then we can remove the failedThumbnailSource aspect.

        Map<String, FailedThumbnailInfo> failures = thumbnailService.getFailedThumbnails(childAssocRef.getParentRef());

        if (failures.isEmpty())
        {
            if (log.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("No remaining failedThumbnail children of ")
                   .append(childAssocRef.getParentRef())
                   .append(" therefore removing aspect ").append(ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE);
                log.debug(msg.toString());
            }
            behaviourFilter.disableBehaviour(childAssocRef.getParentRef(), ContentModel.ASPECT_AUDITABLE);
            try
            {
                nodeService.removeAspect(childAssocRef.getParentRef(), ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE);
            }
            finally
            {
                behaviourFilter.enableBehaviour(childAssocRef.getParentRef(), ContentModel.ASPECT_AUDITABLE);
            }
        }
    }
    
    @Override
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if (this.nodeService.exists(nodeRef))
        {
            deleteFailedThumbnailChildren(nodeRef);
        }
    }

    /**
     * Delete all cm:failedThumbnail children as they represent a failure to thumbnail
     * the old content. By deleting all cm:failedThumbnail children, the cm:failedThumbnailSource
     * aspect will be automatically removed by a policy/behaviour in the ThumbnailService.
     *
     * This is necessary so that if a new version of a 'broken' document is uploaded, then
     * it will be thumbnailed in the normal way.
     */
    private void deleteFailedThumbnailChildren(NodeRef nodeRef)
    {
        Map<String, FailedThumbnailInfo> failedThumbnails = thumbnailService.getFailedThumbnails(nodeRef);
        
        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Deleting " + failedThumbnails.size() + " " + ContentModel.TYPE_FAILED_THUMBNAIL + " nodes");
            }
            for (Entry<String, FailedThumbnailInfo> entry : failedThumbnails.entrySet())
            {
                FailedThumbnailInfo info = entry.getValue();
                nodeService.deleteNode(info.getFailedThumbnailNode());
            }
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        }
    }
}
