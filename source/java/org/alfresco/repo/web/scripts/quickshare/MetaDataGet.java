/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.quickshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * QuickShare/PublicView
 * 
 * GET web script to get limited metadata (including thumbnail defs) => authenticated web script (using a nodeRef)
 * 
 * Note: authenticated web script (equivalent to unauthenticated version - see QuickShareMetaDataGet)
 * 
 * @author janv
 */
public class MetaDataGet extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(QuickShareMetaDataGet.class);
    
    private PersonService personService;
    private ThumbnailService thumbnailService;
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
    
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, Status status, Cache cache)
    {
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final NodeRef nodeRef = WebScriptUtil.getNodeRef(params);
        if (nodeRef == null)
        {
            String msg = "A valid NodeRef must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        
        try
        {
            Map<String, Object> model = getMetaDataModel(nodeRef);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Retrieved limited metadata: "+nodeRef+" ["+model+"]");
            }
            
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find node: "+inre.getNodeRef());
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find nodeRef: "+inre.getNodeRef());
        }
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getMetaDataModel(NodeRef nodeRef)
    {
        QName typeQName = nodeService.getType(nodeRef);
        if (! typeQName.equals(ContentModel.TYPE_CONTENT))
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        
        Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
        ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        
        String modifierUserName = (String)nodeProps.get(ContentModel.PROP_MODIFIER);
        Map<QName, Serializable> personProps = null;
        if (modifierUserName != null)
        {
            try
            {
                NodeRef personRef = personService.getPerson(modifierUserName);
                if (personRef != null)
                {
                    personProps = nodeService.getProperties(personRef);
                }
            }
            catch (NoSuchPersonException nspe)
            {
                // absorb this exception - eg. System (or maybe the user has been deleted)
                if (logger.isInfoEnabled())
                {
                    logger.info("MetaDataGet - no such person: "+modifierUserName);
                }
            }
        }
        
        Map<String, Object> metadata = new HashMap<String, Object>(8);
        
        metadata.put("name", nodeProps.get(ContentModel.PROP_NAME));
        metadata.put("title", nodeProps.get(ContentModel.PROP_TITLE));
        
        if (contentData != null)
        {
            metadata.put("mimetype", contentData.getMimetype());
            metadata.put("size", contentData.getSize());
        }
        else
        {
            metadata.put("size", 0L);
        }
        
        metadata.put("modified", nodeProps.get(ContentModel.PROP_MODIFIED));
        
        if (personProps != null)
        {
            metadata.put("modifierFirstName", personProps.get(ContentModel.PROP_FIRSTNAME));
            metadata.put("modifierLastName", personProps.get(ContentModel.PROP_LASTNAME));
        }
        
        // thumbnail defs for this nodeRef
        List<String> thumbnailDefs = new ArrayList<String>(7);
        if (contentData != null)
        {
            // Note: thumbnail defs only appear in this list if they can produce a thumbnail for the content
            // found in the content property of this node. This will be determined by looking at the mimetype of the content
            // and the destination mimetype of the thumbnail.
            List<ThumbnailDefinition> thumbnailDefinitions = thumbnailService.getThumbnailRegistry().getThumbnailDefinitions(contentData.getMimetype(), contentData.getSize());
            for (ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
            {
                thumbnailDefs.add(thumbnailDefinition.getName());
            }
        }
        metadata.put("thumbnailDefinitions", thumbnailDefs);
        
        // thumbnail instances for this nodeRef
        List<NodeRef> thumbnailRefs = thumbnailService.getThumbnails(nodeRef, ContentModel.PROP_CONTENT, null, null);
        List<String> thumbnailNames = new ArrayList<String>(thumbnailRefs.size());
        for (NodeRef thumbnailRef : thumbnailRefs)
        {
            thumbnailNames.add((String)nodeService.getProperty(thumbnailRef, ContentModel.PROP_NAME));
        }
        metadata.put("thumbnailNames", thumbnailNames);
        
        metadata.put("lastThumbnailModificationData", (List<String>)nodeProps.get(ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA));
             
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("item", metadata);
        return model;
    }
}