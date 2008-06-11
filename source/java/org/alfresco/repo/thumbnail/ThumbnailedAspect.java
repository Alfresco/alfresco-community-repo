/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
public class ThumbnailedAspect implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private PolicyComponent policyComponent;
    private ThumbnailService thumbnailService;
    private ActionService actionService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                ContentModel.ASPECT_THUMBNAILED, 
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
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

    private void queueUpdate(NodeRef nodeRef, QName contentProperty)
    {
        if (this.nodeService.exists(nodeRef) == true)
        {
            Boolean automaticUpdate = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_AUTOMATIC_UPDATE);
            if (automaticUpdate != null && automaticUpdate.booleanValue() == true)
            {
                List<NodeRef> thumbnails = this.thumbnailService.getThumbnails(nodeRef, contentProperty, null, null);
                
                for (NodeRef thumbnail : thumbnails)
                {
                    // Execute the update thumbnail action async for each thumbnail
                    Action action = actionService.createAction(UpdateThumbnailActionExecuter.NAME);
                    action.setParameterValue(CreateThumbnailActionExecuter.PARAM_CONTENT_PROPERTY, contentProperty);
                    actionService.executeAction(action, thumbnail, false, true);
                }
            }
        }
    }
}
