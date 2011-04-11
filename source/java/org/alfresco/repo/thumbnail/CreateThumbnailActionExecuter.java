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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create thumbnail action executer.
 * 
 * NOTE:  This action is used to facilitate the async creation of thumbnails.  It is not intended for genereral useage.
 * 
 * @author Roy Wetherall
 */
public class CreateThumbnailActionExecuter extends ActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(CreateThumbnailActionExecuter.class);
    
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
            ThumbnailDefinition details = registry.getThumbnailDefinition(thumbnailName);
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
            
            // If there isn't a currently active transformer for this, log and skip
            Serializable contentProp = nodeService.getProperty(actionedUponNodeRef, contentProperty);
            if(contentProp != null && contentProp instanceof ContentData)
            {
                String mimetype = ((ContentData)contentProp).getMimetype();
                if (!registry.isThumbnailDefinitionAvailable(mimetype, details))
                {
                    logger.info("Unable to create thumbnail '" + details.getName() + "' for " +
                            mimetype + " as no transformer is currently available");
                    return;
                }
            }
            
            // Create the thumbnail
            try
            {
                this.thumbnailService.createThumbnail(actionedUponNodeRef, contentProperty, details.getMimetype(), details.getTransformationOptions(), thumbnailName, null);
            }
            catch (Exception exception)
            {
                final String msg = "Creation of thumbnail '" + details.getName() + "' failed";
                logger.info(msg);
                
                  // We need to rethrow in order to trigger the compensating action.
                  // See AddFailedThumbnailActionExecuter
                  throw new AlfrescoRuntimeException(msg, exception);
            }
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
