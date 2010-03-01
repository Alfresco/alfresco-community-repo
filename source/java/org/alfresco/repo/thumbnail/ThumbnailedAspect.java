/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Thumbnailed aspect behaviour bean
 * 
 * @author Roy Wetherall
 */
public class ThumbnailedAspect implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          CopyServicePolicies.OnCopyNodePolicy
{
    /** Services */
    private PolicyComponent policyComponent;
    private ThumbnailService thumbnailService;
    private ActionService actionService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    
    /**
     * Set the policy component
     * 
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the action service
     * 
     * @param actionService     action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the thumbnail service
     * 
     * @param thumbnailService  thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
    /**
     * Set the dictionary service
     * 
     * @param dictionaryService     dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                ContentModel.ASPECT_THUMBNAILED, 
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), 
                ContentModel.ASPECT_THUMBNAILED, 
                new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        // Ignore working copies
        if (this.nodeService.exists(nodeRef) == true &&
            this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {        
            // check if any of the content properties have changed
            for (QName propertyQName : after.keySet())
            {
                // is this a content property?
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                if (propertyDef == null)
                {
                    // the property is not recognised
                    continue;
                }
                if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                {
                    // not a content type
                    continue;
                }
                
                try
                {
                    ContentData beforeValue = (ContentData) before.get(propertyQName);
                    ContentData afterValue = (ContentData) after.get(propertyQName);
                    
                    // Figure out if the content is new or not
                    boolean newContent = false;
                    String beforeContentUrl = null;
                    if (beforeValue != null)
                    {
                        beforeContentUrl = beforeValue.getContentUrl();
                    }
                    String afterContentUrl = null;
                    if (afterValue != null)
                    {
                        afterContentUrl = afterValue.getContentUrl();
                    }
                    if (beforeContentUrl == null && afterContentUrl != null)
                    {
                        newContent = true;
                    }                
                    
                    if (afterValue != null && afterValue.getContentUrl() == null)
                    {
                        // no URL - ignore
                    }
                    else if (newContent == false && EqualsHelper.nullSafeEquals(beforeValue, afterValue) == false)
                    {                    
                        // Queue the update
                        queueUpdate(nodeRef, propertyQName);
                    }
                }
                catch (ClassCastException e)
                {
                    // properties don't conform to model
                    continue;
                }
            }
        }
    }

    /**
     * Queue the update to happen asynchronously
     * 
     * @param nodeRef           node reference
     * @param contentProperty   content property
     */
    private void queueUpdate(NodeRef nodeRef, QName contentProperty)
    {        
        Boolean automaticUpdate = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_AUTOMATIC_UPDATE);
        if (automaticUpdate != null && automaticUpdate.booleanValue() == true)
        {
            CompositeAction compositeAction = actionService.createCompositeAction();
            List<NodeRef> thumbnails = this.thumbnailService.getThumbnails(nodeRef, contentProperty, null, null);
            
            for (NodeRef thumbnail : thumbnails)
            {
                // Execute the update thumbnail action async for each thumbnail
                Action action = actionService.createAction(UpdateThumbnailActionExecuter.NAME);
                action.setParameterValue(UpdateThumbnailActionExecuter.PARAM_CONTENT_PROPERTY, contentProperty);
                action.setParameterValue(UpdateThumbnailActionExecuter.PARAM_THUMBNAIL_NODE, thumbnail);
                compositeAction.addAction(action);
            }
            
            actionService.executeAction(compositeAction, nodeRef, false, true);
        }
    }

    /**
     * @return              Returns {@link ThumbnailedAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return ThumbnailedAspectCopyBehaviourCallback.INSTANCE;
    }

    /**
     * Behaviour for the {@link ContentModel#ASPECT_THUMBNAILED <b>cm:thumbnailed</b>} aspect.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class ThumbnailedAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new ThumbnailedAspectCopyBehaviourCallback();
        
        /**
         * @return              Returns <tt>true</tt> always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            return true;
        }

        /**
         * Copy thumbnail-related associations, {@link ContentModel#ASSOC_THUMBNAILS} regardless of
         * cascade options.
         */
        @Override
        public ChildAssocCopyAction getChildAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyChildAssociationDetails childAssocCopyDetails)
        {
            ChildAssociationRef childAssocRef = childAssocCopyDetails.getChildAssocRef();
            if (childAssocRef.getTypeQName().equals(ContentModel.ASSOC_THUMBNAILS))
            {
                return ChildAssocCopyAction.COPY_CHILD;
            }
            else
            {
                throw new IllegalStateException(
                        "Behaviour should have been invoked: \n" +
                        "   Aspect: " + this.getClass().getName() + "\n" +
                        "   " + childAssocCopyDetails + "\n" +
                        "   " + copyDetails);
            }
        }
        
        /**
         * Copy only the {@link ContentModel#PROP_AUTOMATIC_UPDATE}
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName,
                CopyDetails copyDetails,
                Map<QName, Serializable> properties)
        {
            Map<QName, Serializable> newProperties = new HashMap<QName, Serializable>(5);
            Serializable value = properties.get(ContentModel.PROP_AUTOMATIC_UPDATE);
            newProperties.put(ContentModel.PROP_AUTOMATIC_UPDATE, value);
            return newProperties;
        }
    }
}
