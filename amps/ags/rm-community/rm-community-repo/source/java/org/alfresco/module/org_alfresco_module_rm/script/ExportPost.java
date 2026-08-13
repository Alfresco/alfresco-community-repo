/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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

/**
 * Creates an RM specific ACP file of nodes to export then streams it back to the client.
 *
 * @author Gavin Cornwell
 */
public class ExportPost extends StreamACP
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ExportPost.class);

    protected static final String PARAM_TRANSFER_FORMAT = "transferFormat";

    /** Optional tabular search-result data to embed as a CSV file inside the exported archive. */
    protected static final String PARAM_ITEMS = "items";

    /** Content Streamer */
    private ContentStreamer contentStreamer;

    /** Writes the displayed search-result table to a CSV file. */
    private SearchResultsCSVWriter searchResultsCSVWriter = new SearchResultsCSVWriter();

    /**
     * @param contentStreamer
     */
    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }

    /**
     * @param searchResultsCSVWriter
     *            the writer used to build the CSV embedded in the export archive
     */
    public void setSearchResultsCSVWriter(SearchResultsCSVWriter searchResultsCSVWriter)
    {
        this.searchResultsCSVWriter = searchResultsCSVWriter;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
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
            JSONObject csvItems = null;
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

                // look for the optional displayed search-result table (sent as a JSON string)
                String itemsParam = req.getParameter(PARAM_ITEMS);
                if (itemsParam != null && itemsParam.length() > 0)
                {
                    csvItems = new JSONObject(new JSONTokener(itemsParam));
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

                // look for the optional displayed search-result table
                if (json.has(PARAM_ITEMS))
                {
                    csvItems = json.getJSONObject(PARAM_ITEMS);
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
                QName[] excludedAspects = new QName[]{
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
                QName[] excludedAspects = new QName[]{RecordsManagementModel.ASPECT_EXTENDED_SECURITY};
                params.setExcludeAspects(excludedAspects);
            }

            // create an ACP of the nodes
            tempACPFile = createACP(params,
                    transferFormat ? ZIP_EXTENSION : ACPExportPackageHandler.ACP_EXTENSION,
                    transferFormat);

            // if the displayed search-result table was supplied, embed it as a CSV inside the archive
            if (csvItems != null)
            {
                addSearchResultsCsv(tempACPFile, csvItems);
            }

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
        catch (Exception e)
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

    /**
     * Builds a CSV of the displayed search-result table and adds it as an entry inside the given export archive.
     *
     * @param archive
     *            the ACP (or transfer ZIP) archive to add the CSV to
     * @param csvItems
     *            the object holding the {@code headers} and {@code rows} of the displayed table
     */
    protected void addSearchResultsCsv(File archive, JSONObject csvItems)
    {
        File tempCSVFile = null;
        try
        {
            tempCSVFile = searchResultsCSVWriter.createCSVFile(csvItems);
            addFileToArchive(archive, tempCSVFile, SearchResultsCSVWriter.buildCsvFileName());
        }
        catch (IOException ioe)
        {
            // failing to write the CSV into the archive is a server-side error, not a bad request
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Failed to embed search results CSV into the export archive.", ioe);
        }
        finally
        {
            if (tempCSVFile != null)
            {
                tempCSVFile.delete();
            }
        }
    }

    /**
     * Adds the given file as a new entry at the root of an existing ZIP based archive.
     *
     * @param archive
     *            the existing archive to add to
     * @param file
     *            the file whose contents become the archive entry
     * @param entryName
     *            the name of the entry to create inside the archive
     * @throws IOException
     *             if the entry cannot be written
     */
    protected void addFileToArchive(File archive, File file, String entryName) throws IOException
    {
        URI uri = URI.create("jar:" + archive.toURI());
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap()))
        {
            Path target = zipfs.getPath("/" + entryName);
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Added entry '" + entryName + "' to archive: " + archive.getAbsolutePath());
        }
    }
}
