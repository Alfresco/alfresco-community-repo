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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.File;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementSearchBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Streams the nodes of a transfer object to the client in the form of an
 * ACP file.
 * 
 * @author Gavin Cornwell
 */
public class TransferGet extends BaseTransferWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(TransferGet.class);
    
    @SuppressWarnings("deprecation")
    @Override
    protected File executeTransfer(NodeRef transferNode,
                WebScriptRequest req, WebScriptResponse res, 
                Status status, Cache cache) throws IOException
    {
        // get all 'transferred' nodes
        NodeRef[] itemsToTransfer = getTransferNodes(transferNode);
        
        // setup the ACP parameters
        ExporterCrawlerParameters params = new ExporterCrawlerParameters();
        params.setCrawlSelf(true);
        params.setCrawlChildNodes(true);
        params.setExportFrom(new Location(itemsToTransfer));
        QName[] excludedAspects = new QName[] {
                    RenditionModel.ASPECT_RENDITIONED,
                    ContentModel.ASPECT_THUMBNAILED,
                    RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE,
                    RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH};
        params.setExcludeAspects(excludedAspects);
        
        // create an archive of all the nodes to transfer
        File tempFile = createACP(params, ZIP_EXTENSION, true);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Creating transfer archive for " + itemsToTransfer.length + 
                        " items into file: " + tempFile.getAbsolutePath());
        }
        
        // stream the archive back to the client as an attachment (forcing save as)
        streamContent(req, res, tempFile, true, tempFile.getName());
        
        // return the temp file for deletion
        return tempFile;
    }
}