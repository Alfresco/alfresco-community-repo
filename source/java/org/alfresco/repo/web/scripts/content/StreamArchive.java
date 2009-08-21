/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.web.scripts.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Base class for Java backed webscripts that wish to stream the contents
 * of an archive back to the caller.
 * 
 * @author Gavin Cornwell
 */
public class StreamArchive extends StreamContent
{
    /** Logger */
    private static Log logger = LogFactory.getLog(StreamArchive.class);
    
    protected static final String TEMP_FILE_PREFIX = "export_";
    protected static final String PARAM_NODE_REFS = "nodeRefs";
    protected static final String MULTIPART_FORMDATA = "multipart/form-data";
    
    protected ExporterService exporterService;
    
    /**
     * Sets the ExporterService to use
     * 
     * @param exporterService The ExporterService
     */
    public void setExporterService(ExporterService exporterService) 
    {
        this.exporterService = exporterService;
    }
    
    /**
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        JSONObject json = null;
        File tempArchiveFile = null;
        try
        {
            NodeRef[] nodeRefs = null;
            String contentType = req.getContentType();
            if (MULTIPART_FORMDATA.equals(contentType))
            {
                // get nodeRefs parameter from form
                String nodeRefsParam = req.getParameter(PARAM_NODE_REFS);
             
                // check the list of NodeRefs is present
                if (nodeRefsParam == null)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Mandatory 'nodeRefs' parameter was not provided in form data");
                }
                
                List<NodeRef> listNodeRefs = new ArrayList<NodeRef>(8);
                StringTokenizer tokenizer = new StringTokenizer(nodeRefsParam, ",");
                while (tokenizer.hasMoreTokens())
                {
                    listNodeRefs.add(new NodeRef(tokenizer.nextToken().trim()));
                }
                
                nodeRefs = new NodeRef[listNodeRefs.size()];
                nodeRefs = listNodeRefs.toArray(nodeRefs);
            }
            else
            {
                // presume the request is a JSON request so get JSON body
                json = new JSONObject(new JSONTokener(req.getContent().getContent()));
                
                // check the list of NodeRefs is present
                if (!json.has(PARAM_NODE_REFS))
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Mandatory 'nodeRefs' parameter was not provided in request body");
                }
    
                JSONArray jsonArray = json.getJSONArray(PARAM_NODE_REFS);
                if (jsonArray.length() != 0)
                {
                    // build the list of NodeRefs
                    nodeRefs = new NodeRef[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        NodeRef nodeRef = new NodeRef(jsonArray.getString(i));
                        nodeRefs[i] = nodeRef;
                    }
                }                
            }
            
            // create an archive of the nodes
            tempArchiveFile = createArchive(nodeRefs);
                
            // stream the archive back to the client as an attachment (forcing save as)
            streamContent(req, res, tempArchiveFile, true, tempArchiveFile.getName());
        } 
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
        finally
        {
           // try and delete the temporary file
           if (tempArchiveFile != null)
           {
               tempArchiveFile.delete();
               
               if (logger.isDebugEnabled())
                   logger.debug("Deleted temporary archive: " + tempArchiveFile.getAbsolutePath());
           }
        }
    }
    
    /**
     * Returns an Alfresco archive file containing the nodes represented
     * by the given list of NodeRefs.
     * 
     * @param nodeRefs List of nodes to create archive from
     * @return File object representing the created archive
     */
    protected File createArchive(List<NodeRef> nodeRefs)
    {
        NodeRef[] nodeRefArr = new NodeRef[nodeRefs.size()];
        return createArchive(nodeRefs.toArray(nodeRefArr));
    }
    
    /**
     * Returns an Alfresco archive file containing the nodes represented
     * by the given list of NodeRefs.
     * 
     * @param nodeRefs Array of nodes to create archive from
     * @return File object representing the created archive
     */
    protected File createArchive(NodeRef[] nodeRefs)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder builder = new StringBuilder("Creating archive for ");
                builder.append(nodeRefs.length);
                builder.append(" nodes: ");
                for (int idx = 0; idx < nodeRefs.length; idx++)
                {
                    if (idx != 0) builder.append(", ");
                    builder.append(nodeRefs[idx]);
                }
                
                logger.debug(builder.toString());
            }
            
            // generate temp file and folder name
            File dataFile = new File(GUID.generate());
            File contentDir = new File(GUID.generate());
            
            // setup export package handler
            File acpFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, "." + ACPExportPackageHandler.ACP_EXTENSION);
            ACPExportPackageHandler handler = new ACPExportPackageHandler(new FileOutputStream(acpFile), 
                 dataFile, contentDir, this.mimetypeService);
           
            // setup parameters for export
            ExporterCrawlerParameters params = new ExporterCrawlerParameters();
            params.setCrawlSelf(true);
            params.setCrawlChildNodes(true);
            params.setCrawlAssociations(false);
            params.setExportFrom(new Location(nodeRefs));
            
            // perform the actual export
            this.exporterService.exportView(handler, params, null);
            
            if (logger.isDebugEnabled())
                logger.debug("Created temporary archive: " + acpFile.getAbsolutePath());
            
            return acpFile;
        }
        catch (FileNotFoundException fnfe)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Failed to create archive", fnfe);
        }
    }
}