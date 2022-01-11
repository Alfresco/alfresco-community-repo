/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.File;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Streams the nodes of a transfer object to the client in the form of an
 * ACP file.
 *
 * @author Gavin Cornwell
 */
@Deprecated
public class TransferGet extends BaseTransferWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(TransferGet.class);

    /** Content Streamer */
    private ContentStreamer contentStreamer;

    /**
     * @param contentStreamer
     */
    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }

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
        contentStreamer.streamContent(req, res, tempFile, null, true, tempFile.getName(), null);

        // return the temp file for deletion
        return tempFile;
    }
}
