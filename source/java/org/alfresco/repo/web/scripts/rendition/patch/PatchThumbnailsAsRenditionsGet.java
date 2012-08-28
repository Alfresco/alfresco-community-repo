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
package org.alfresco.repo.web.scripts.rendition.patch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PatchThumbnailsAsRenditionsGet extends DeclarativeWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(PatchThumbnailsAsRenditionsGet.class);

    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private static final String QUERY = "TYPE:\"" + ContentModel.TYPE_THUMBNAIL +
                                        "\" AND NOT ASPECT:\"" + RenditionModel.ASPECT_VISIBLE_RENDITION +
                                        "\" AND NOT ASPECT:\"" + RenditionModel.ASPECT_HIDDEN_RENDITION + "\"";
    
    /** Spring-injected services */
    private NodeService nodeService;
    private RenditionService renditionService;
    private SearchService searchService;
    
    /**
     * Sets the nodeService.
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Sets the renditionService.
     * 
     * @param renditionService
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }
    
    /**
     * Sets the searchService.
     * 
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (logger.isInfoEnabled())
        {
            logger.debug("Patching legacy thumbnails by applying appropriate rendition aspect");
        }
        List<NodeRef> resultNodeRefs = null; 
        ResultSet types = null;

        try
        {
            types = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, QUERY);
            resultNodeRefs = types.getNodeRefs();
        }
        finally
        {
            if (types != null) {types.close();}
        }
        
        long patchedNodeRefs = 0;
        
        for (NodeRef nodeRef : resultNodeRefs)
        {
            if (nodeService.exists(nodeRef) == false ||
                    renditionService.isRendition(nodeRef))
            {
                continue;
            }
            
            // Now add one of the two aspects depending on parent location.
            ChildAssociationRef sourceNode = renditionService.getSourceNode(nodeRef);
            ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
            QName aspectToApply;
            if (primaryParent.getParentRef().equals(sourceNode.getParentRef()))
            {
                aspectToApply = RenditionModel.ASPECT_HIDDEN_RENDITION;
            }
            else
            {
                aspectToApply = RenditionModel.ASPECT_VISIBLE_RENDITION;
            }

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Applying aspect ")
                    .append(aspectToApply)
                    .append(" to node ")
                    .append(nodeRef);
                logger.debug(msg.toString());
            }
            nodeService.addAspect(nodeRef, aspectToApply, null);
            patchedNodeRefs++;
        }

        Map<String, Object> model = new HashMap<String, Object>();
    	model.put("patchedNodeCount", new Long(patchedNodeRefs));
    	
        return model;
    }
}