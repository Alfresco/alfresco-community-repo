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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Base class for Java backed webscripts that wish to generate an ACP and 
 * stream the contents back to the caller.
 * <p>
 * The default implementation generates an ACP file containing the provided
 * NodeRefs and all their respective children.
 * 
 * @author Gavin Cornwell
 */
public class StreamACP extends StreamContent
{
    /** Logger */
    private static Log logger = LogFactory.getLog(StreamACP.class);
    
    protected static final String TEMP_FILE_PREFIX = "export_";
    protected static final String MULTIPART_FORMDATA = "multipart/form-data";
    protected static final String ZIP_EXTENSION = "zip";
    
    protected static final String PARAM_NODE_REFS = "nodeRefs";
    
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
        File tempACPFile = null;
        try
        {
            NodeRef[] nodeRefs = null;
            String contentType = req.getContentType();
            if (MULTIPART_FORMDATA.equals(contentType))
            {
                // get nodeRefs parameter from form
                nodeRefs = getNodeRefs(req.getParameter(PARAM_NODE_REFS));
            }
            else
            {
                // presume the request is a JSON request so get nodeRefs from JSON body
                nodeRefs = getNodeRefs(new JSONObject(new JSONTokener(req.getContent().getContent())));
            }
            
            // setup the ACP parameters
            ExporterCrawlerParameters params = new ExporterCrawlerParameters();
            params.setCrawlSelf(true);
            params.setCrawlChildNodes(true);
            params.setExportFrom(new Location(nodeRefs));
            
            // create an ACP of the nodes
            tempACPFile = createACP(params, ACPExportPackageHandler.ACP_EXTENSION, false);
                
            // stream the ACP back to the client as an attachment (forcing save as)
            streamContent(req, res, tempACPFile, true, tempACPFile.getName(), null);
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
        finally
        {
           // try and delete the temporary file
           if (tempACPFile != null)
           {
               if (logger.isDebugEnabled())
                   logger.debug("Deleting temporary archive: " + tempACPFile.getAbsolutePath());
               
               tempACPFile.delete();
           }
        }
    }
    
    /**
     * Converts the given comma delimited string of NodeRefs to an array
     * of NodeRefs. If the string is null a WebScriptException is thrown.
     * 
     * @param nodeRefsParam Comma delimited string of NodeRefs
     * @return Array of NodeRef objects
     */
    protected NodeRef[] getNodeRefs(String nodeRefsParam)
    {
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
        
        NodeRef[] nodeRefs = new NodeRef[listNodeRefs.size()];
        nodeRefs = listNodeRefs.toArray(nodeRefs);
        
        return nodeRefs;
    }
    
    /**
     * Attempts to retrieve and convert a JSON array of
     * NodeRefs from the given JSON object. If the nodeRefs
     * property is not present a WebScriptException is thrown.
     * 
     * @param nodeRefs Comma delimited string of NodeRefs
     * @return Array of NodeRef objects
     */
    protected NodeRef[] getNodeRefs(JSONObject json) throws JSONException
    {
        // check the list of NodeRefs is present
        if (!json.has(PARAM_NODE_REFS))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                "Mandatory 'nodeRefs' parameter was not provided in request body");
        }

        NodeRef[] nodeRefs = new NodeRef[0];
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
        
        return nodeRefs;
    }
    
    /**
     * Returns an ACP file containing the nodes represented by the given list of NodeRefs.
     * 
     * @param params The parameters for the ACP exporter
     * @param extension The file extenstion to use for the ACP file
     * @param keepFolderStructure Determines whether the folder structure is maintained for
     *        the content inside the ACP file
     * @return File object representing the created ACP
     */
    protected File createACP(ExporterCrawlerParameters params, String extension, boolean keepFolderStructure)
    {
        try
        {
            // generate temp file and folder name
            File dataFile = new File(GUID.generate());
            File contentDir = new File(GUID.generate());
            
            // setup export package handler
            File acpFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, "." + extension);
            ACPExportPackageHandler handler = new ACPExportPackageHandler(new FileOutputStream(acpFile), 
                 dataFile, contentDir, this.mimetypeService);
            handler.setExportAsFolders(keepFolderStructure);
            handler.setNodeService(this.nodeService);

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