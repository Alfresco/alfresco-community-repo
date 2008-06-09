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

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;

/**
 * Create thumbnail action executer.
 * 
 * NOTE:  This action is used to facilitate the async creation of thumbnails.  It is not intended for genereral useage.
 * 
 * @author Roy Wetherall
 */
public class CreateThumbnailActionExecuter extends ActionExecuterAbstractBase
{
    /** Thumbnail Service */
    private ThumbnailService thumbnailService;
    
    /** Node Service */
    private NodeService nodeService;
    
    /** Action name and parameters */
    public static final String NAME = "create-thumbnail";
    public static final String PARAM_CONTENT_PROPERTY = "content-property";
    public static final String PARAM_THUMBANIL_NAME = "thumbnail-name";
    
    /**
     * Set the thumbnail service
     * 
     * @param thumbnailService  the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
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
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
            // Get the thumbnail Name
            String thumbnailName = (String)action.getParameterValue(PARAM_THUMBANIL_NAME);
            
            // Get the details of the thumbnail
            ThumbnailRegistry registry = this.thumbnailService.getThumbnailRegistry();
            ThumbnailDetails details = registry.getThumbnailDetails(thumbnailName);
            if (details == null)
            {
                // Throw exception 
                throw new AlfrescoRuntimeException("The thumbnail name '" + thumbnailName + "' is not registered");
            }
            
            // Get the content property
            QName contentProperty = (QName)action.getParameterValue(PARAM_CONTENT_PROPERTY);
            if (contentProperty == null)
            {
                contentProperty = ContentModel.PROP_CONTENT;
            }
            
            // Create the thumbnail
            this.thumbnailService.createThumbnail(actionedUponNodeRef, contentProperty, details.getMimetype(), details.getTransformationOptions(), thumbnailName, null);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_THUMBANIL_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_THUMBANIL_NAME)));      
        paramList.add(new ParameterDefinitionImpl(PARAM_CONTENT_PROPERTY, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_CONTENT_PROPERTY)));        
    }

}
