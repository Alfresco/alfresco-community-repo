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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.metadata.MetadataEmbedder;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Embed metadata in any content.
 * <p>
 * The metadata is embedded in the content from the current
 * property values.
 *
 * @author Jesper Steen Møller, Ray Gauss II
 */
public class ContentMetadataEmbedder extends ActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(ContentMetadataEmbedder.class);

    public static final String EXECUTOR_NAME = "embed-metadata";

    private NodeService nodeService;
    private ContentService contentService;
    private MetadataExtracterRegistry metadataExtracterRegistry;

    /**
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService The contentService to set.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param metadataExtracterRegistry The metadataExtracterRegistry to set.
     */
    public void setMetadataExtracterRegistry(MetadataExtracterRegistry metadataExtracterRegistry)
    {
        this.metadataExtracterRegistry = metadataExtracterRegistry;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef,
     *      NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (!nodeService.exists(actionedUponNodeRef))
        {
            // Node is gone
            return;
        }
        ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        // The reader may be null, e.g. for folders and the like
        if (reader == null || reader.getMimetype() == null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("no content or mimetype - do nothing");
            }
            // No content to extract data from
            return;
        }
        String mimetype = reader.getMimetype();
        MetadataEmbedder embedder = metadataExtracterRegistry.getEmbedder(mimetype);
        if (embedder == null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("no embedder for mimetype:" + mimetype);
            }
            // There is no embedder to use
            return;
        }
        
        ContentWriter writer = contentService.getWriter(actionedUponNodeRef, ContentModel.PROP_CONTENT, true);
        // The writer may be null, e.g. for folders and the like
        if (writer == null || writer.getMimetype() == null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("no content or mimetype - do nothing");
            }
            // No content to embed data in
            return;
        }

        // Get all the node's properties
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(actionedUponNodeRef);

        try
        {
        	embedder.embed(nodeProperties, reader, writer);
        }
        catch (Throwable e)
        {
            // Extracters should attempt to handle all error conditions and embed
            // as much as they can.  If, however, one should fail, we don't want the
            // action itself to fail.  We absorb and report the exception here.
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Metadata embedding failed: \n" +
                        "   Extracter: " + this + "\n" +
                        "   Node:      " + actionedUponNodeRef + "\n" +
                        "   Content:   " + writer,
                        e);
            }
            else
            {
                logger.warn(
                        "Metadata embedding failed (turn on DEBUG for full error): \n" +
                        "   Extracter: " + this + "\n" +
                        "   Node:      " + actionedUponNodeRef + "\n" +
                        "   Content:   " + writer + "\n" +
                        "   Failure:   " + e.getMessage());
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> arg0)
    {
        // None!
    }
}
