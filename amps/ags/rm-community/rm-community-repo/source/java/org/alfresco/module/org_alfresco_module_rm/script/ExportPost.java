/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;

/**
 * Creates an RM specific ACP file of nodes to export then streams it back to the client.
 *
 * @author Gavin Cornwell
 */
@Setter
public class ExportPost extends StreamACP
{
    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(ExportPost.class);

    protected static final String PARAM_TRANSFER_FORMAT = "transferFormat";

    /**
     * Content Streamer -- SETTER --
     *
     * @param contentStreamer
     */
    private ContentStreamer contentStreamer;

    protected static final String TEMP_FILE_PREFIX = "export_";

    protected static final String ZIP_EXTENSION = "zip";

    protected static final String CSV_EXTENSION = "csv";

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File tempACPFile = null;
        File tempCSVFile = null;
        File tempZIPFile = null;
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
                JSONObject json = new JSONObject(new JSONTokener(req.getContent()
                        .getContent()));
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
                QName[] excludedAspects = new QName[]{RenditionModel.ASPECT_RENDITIONED, ContentModel.ASPECT_THUMBNAILED,
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
            tempACPFile = createACP(params, transferFormat ? ZIP_EXTENSION : ACPExportPackageHandler.ACP_EXTENSION,
                    transferFormat);

            // // stream the ACP back to the client as an attachment (forcing save as)
            // contentStreamer.streamContent(req, res, tempACPFile, null, true, tempACPFile.getName(), null);

            // Map<String, Object> model = dynamicAuthoritiesGet.buildModel(req, res);
            // create a CSV file with the same nodeRefs data
            tempCSVFile = createCSV(nodeRefs);

            tempZIPFile = createZipFile(tempACPFile, tempCSVFile);

            // Stream the file to the browser
            res.setContentType("application/zip");
            res.setHeader("Content-Disposition", "attachment; filename=\"exported_files.zip\"");
            res.setHeader("Content-Encoding", "UTF-8");

            // stream the CSV file back to the client
            contentStreamer.streamContent(req, res, tempZIPFile, null, true, tempZIPFile.getName(), null);
        }
        catch (IOException ioe)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", ioe);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
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

            // Try and delete the temporary CSV file
            if (tempCSVFile != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Deleting temporary CSV file: " + tempCSVFile.getAbsolutePath());
                }
                tempCSVFile.delete();
            }

            // Try and delete the temporary ZIP file
            if (tempZIPFile != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Deleting temporary ZIP file: " + tempZIPFile.getAbsolutePath());
                }
                tempZIPFile.delete();
            }
        }
    }

    private File createZipFile(File tempACPFile, File tempCSVFile) throws IOException
    {
        File tempZipFile = File.createTempFile("exported_files", "." + ZIP_EXTENSION);

        try (FileOutputStream fos = new FileOutputStream(tempZipFile);
                ZipOutputStream zipOut = new ZipOutputStream(fos))
        {

            // Add ACP file to the ZIP
            addFileToZip(zipOut, tempACPFile, tempACPFile.getName());

            // Add CSV file to the ZIP
            addFileToZip(zipOut, tempCSVFile, tempCSVFile.getName());

            // Finalize the ZIP archive
            zipOut.close();

            return tempZipFile;

        }
        catch (IOException e)
        {
            // Handle error while creating zip file
            throw new IOException("Error creating zip file", e);
        }
    }

    private void addFileToZip(ZipOutputStream zipOut, File file, String fileName) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(file))
        {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0)
            {
                zipOut.write(buffer, 0, length);
            }

            zipOut.closeEntry();
        }
    }

    // Helper method to create a CSV file from nodeRefs or other data
    public File createCSV(NodeRef[] nodeRefs) throws IOException
    {
        // Create temporary file in the system's temp directory
        File csvFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, "." + CSV_EXTENSION);

        // Use try-with-resources to automatically close FileWriter and CSVPrinter
        try (Writer writer = new FileWriter(csvFile);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                        .setHeader("NodeRef", "Node Name", "Description", "Title", "Folder Name", "Created On",
                                "Modified On", "Created By")
                        .build()))
        {
            // Loop through each NodeRef and write data
            for (NodeRef nodeRef : nodeRefs)
            {
                ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
                NodeRef primaryNodeRef = primaryParent.getParentRef();
                String nodeName = getNodeName(nodeRef);
                String description = getDescription(primaryNodeRef);
                String title = getTitle(primaryNodeRef);
                String folderName = getFolderName(primaryNodeRef);
                String createdOn = getCreatedOn(primaryNodeRef);
                String modifiedOn = getModifiedOn(primaryNodeRef);
                String createdBy = getCreatedBy(primaryNodeRef);

                csvPrinter.printRecord(nodeRef.toString(), nodeName, description, title, folderName, createdOn,
                        modifiedOn, createdBy);
            }
        }
        return csvFile;
    }

    private String getNodeName(NodeRef nodeRef)
    {
        String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        return nodeName != null ? nodeName : "Unknown Node Name";
    }

    private String getDescription(NodeRef nodeRef)
    {
        String description = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
        return description != null ? description : "No Description";
    }

    private String getFolderName(NodeRef nodeRef)
    {
        String folderName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        return folderName != null ? folderName : "No Parent folder";
    }

    private String getCreatedOn(NodeRef nodeRef)
    {
        Date createdDate = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
        return createdDate != null ? formatDate(createdDate) : "No Creation date";
    }

    private String formatDate(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    private String getModifiedOn(NodeRef nodeRef)
    {
        Date modifiedDate = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        return modifiedDate != null ? String.valueOf(modifiedDate) : "No Modified date";
    }

    private String getCreatedBy(NodeRef nodeRef)
    {
        String createdBy = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
        return createdBy != null ? createdBy : "Unknown Creater";
    }

    private String getTitle(NodeRef nodeRef)
    {
        String createdBy = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
        return createdBy != null ? createdBy : "No Title";
    }

}
