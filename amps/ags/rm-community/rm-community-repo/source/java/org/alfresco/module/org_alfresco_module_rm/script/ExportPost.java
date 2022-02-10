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
import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.repo.web.scripts.content.StreamACP;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Creates an RM specific ACP file of nodes to export then streams it back
 * to the client.
 *
 * @author Gavin Cornwell
 */
public class ExportPost extends StreamACP
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ExportPost.class);

    protected static final String PARAM_TRANSFER_FORMAT = "transferFormat";

    /** Content Streamer */
    private ContentStreamer contentStreamer;

    /**
     * @param contentStreamer
     */
    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File tempACPFile = null;
        try
        {
            NodeRef[] nodeRefs = null;
            boolean transferFormat = false;
            String contentType = req.getContentType();
            if (MULTIPART_FORMDATA.equals(contentType))
            {
                // get nodeRefs parameter from form
                nodeRefs = getNodeRefs(req.getParameter(PARAM_NODE_REFS));

                // look for the transfer format
                String transferFormatParam = req.getParameter(PARAM_TRANSFER_FORMAT);
                if (transferFormatParam != null && transferFormatParam.length() > 0)
                {
                    transferFormat = Boolean.parseBoolean(transferFormatParam);
                }
            }
            else
            {
                // presume the request is a JSON request so get nodeRefs from JSON body
                JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
                nodeRefs = getNodeRefs(json);

                if (json.has(PARAM_TRANSFER_FORMAT))
                {
                    transferFormat = json.getBoolean(PARAM_TRANSFER_FORMAT);
                }
            }

            // setup the ACP parameters
            ExporterCrawlerParameters params = new ExporterCrawlerParameters();
            params.setCrawlSelf(true);
            params.setCrawlChildNodes(true);
            params.setExportFrom(new Location(nodeRefs));

            // if transfer format has been requested we need to exclude certain aspects
            if (transferFormat)
            {
                // restrict specific aspects from being returned
                QName[] excludedAspects = new QName[] {
                            RenditionModel.ASPECT_RENDITIONED,
                            ContentModel.ASPECT_THUMBNAILED,
                            RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE,
                            RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH,
                            RecordsManagementModel.ASPECT_EXTENDED_SECURITY};
                params.setExcludeAspects(excludedAspects);
            }
            else
            {
                // restrict specific aspects from being returned
                QName[] excludedAspects = new QName[] {RecordsManagementModel.ASPECT_EXTENDED_SECURITY};
                params.setExcludeAspects(excludedAspects);
            }

            // create an ACP of the nodes
            tempACPFile = createACP(params,
                        transferFormat ? ZIP_EXTENSION : ACPExportPackageHandler.ACP_EXTENSION,
                        transferFormat);

            // stream the ACP back to the client as an attachment (forcing save as)
            contentStreamer.streamContent(req, res, tempACPFile, null, true, tempACPFile.getName(), null);
        }
        catch (IOException ioe)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", ioe);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
        catch(Exception e)
        {
            if (logger.isDebugEnabled())
            {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                logger.debug("Caught exception; decorating with appropriate status template : " + stack.toString());
            }

            throw createStatusException(e, req, res);
        }
        finally
        {
           // try and delete the temporary file
           if (tempACPFile != null)
           {
               if (logger.isDebugEnabled())
               {
                   logger.debug("Deleting temporary archive: " + tempACPFile.getAbsolutePath());
               }

               tempACPFile.delete();
           }
        }
    }
}
