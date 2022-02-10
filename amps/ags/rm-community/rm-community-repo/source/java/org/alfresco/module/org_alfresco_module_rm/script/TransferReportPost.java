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

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Files a transfer report as a record.
 *
 * @author Gavin Cornwell
 */
@Deprecated
public class TransferReportPost extends BaseTransferWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(TransferReportPost.class);

    protected static final String REPORT_FILE_PREFIX = "report_";
    protected static final String REPORT_FILE_SUFFIX = ".html";
    protected static final String PARAM_DESTINATION = "destination";
    protected static final String RESPONSE_SUCCESS = "success";
    protected static final String RESPONSE_RECORD = "record";
    protected static final String RESPONSE_RECORD_NAME = "recordName";

    protected DictionaryService ddService;
    protected RecordsManagementActionService rmActionService;
    protected DispositionService dispositionService;
    protected ContentService contentService;

    /**
     * Sets the DictionaryService instance
     *
     * @param ddService The DictionaryService instance
     */
    public void setDictionaryService(DictionaryService ddService)
    {
        this.ddService = ddService;
    }

    /**
     * Sets the disposition service
     *
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Sets the RecordsManagementActionService instance
     *
     * @param rmActionService RecordsManagementActionService instance
     */
    public void setRecordsManagementActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }

    /**
     * Sets the ContentSerivce instance
     *
     * @param contentService ContentService instance
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    protected File executeTransfer(NodeRef transferNode,
                WebScriptRequest req, WebScriptResponse res,
                Status status, Cache cache) throws IOException
    {
        File report = null;

        // retrieve requested format
        String format = req.getFormat();
        Map<String, Object> model = new HashMap<>();
        model.put("status", status);
        model.put("cache", cache);

        try
        {
            // extract the destination parameter, ensure it's present and it is
            // a record folder
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            if (!json.has(PARAM_DESTINATION))
            {
                status.setCode(HttpServletResponse.SC_BAD_REQUEST,
                            "Mandatory '" + PARAM_DESTINATION + "' parameter has not been supplied");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return null;
            }

            String destinationParam = json.getString(PARAM_DESTINATION);
            NodeRef destination = new NodeRef(destinationParam);

            if (!this.nodeService.exists(destination))
            {
                status.setCode(HttpServletResponse.SC_NOT_FOUND,
                            "Node " + destination.toString() + " does not exist");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return null;
            }

            // ensure the node is a filePlan object
            if (!RecordsManagementModel.TYPE_RECORD_FOLDER.equals(this.nodeService.getType(destination)))
            {
                status.setCode(HttpServletResponse.SC_BAD_REQUEST,
                            "Node " + destination.toString() + " is not a record folder");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return null;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Filing transfer report as record in record folder: " + destination);
            }

            // generate the report (will be in JSON format)
            report = generateHTMLTransferReport(transferNode);

            // file the report as a record
            NodeRef record = fileTransferReport(report, destination);

            if (logger.isDebugEnabled())
            {
                logger.debug("Filed transfer report as new record: " + record);
            }

            // return success flag and record noderef as JSON
            JSONObject responseJSON = new JSONObject();
            responseJSON.put(RESPONSE_SUCCESS, (record != null));
            if (record != null)
            {
                responseJSON.put(RESPONSE_RECORD, record.toString());
                responseJSON.put(RESPONSE_RECORD_NAME,
                            (String)nodeService.getProperty(record, ContentModel.PROP_NAME));
            }

            // setup response
            String jsonString = responseJSON.toString();
            res.setContentType(MimetypeMap.MIMETYPE_JSON);
            res.setContentEncoding("UTF-8");
            res.setHeader("Content-Length", Long.toString(jsonString.length()));

            // write the JSON response
            res.getWriter().write(jsonString);
        }
        catch (JSONException je)
        {
            throw createStatusException(je, req, res);
        }

        // return the file for deletion
        return report;
    }

    /**
     * Generates a File containing the JSON representation of a transfer report.
     *
     * @param transferNode The transfer node
     * @return File containing JSON representation of a transfer report
     * @throws IOException
     */
    File generateHTMLTransferReport(NodeRef transferNode) throws IOException
    {
        File report = TempFileProvider.createTempFile(REPORT_FILE_PREFIX, REPORT_FILE_SUFFIX);

        // create the writer
        try (FileOutputStream fileOutputStream = new FileOutputStream(report) ;
            Writer writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8")))
        {
            // get all 'transferred' nodes
            NodeRef[] itemsToTransfer = getTransferNodes(transferNode);

            if (logger.isDebugEnabled())
            {
                logger.debug("Generating HTML transfer report for " + itemsToTransfer.length +
                            " items into file: " + report.getAbsolutePath());
            }

            // use RMService to get disposition authority
            String dispositionAuthority = null;
            if (itemsToTransfer.length > 0)
            {
                // use the first transfer item to get to disposition schedule
                DispositionSchedule ds = dispositionService.getDispositionSchedule(itemsToTransfer[0]);
                if (ds != null)
                {
                    dispositionAuthority = ds.getDispositionAuthority();
                }
            }

            // write the HTML header
            writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
            writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
            Boolean isAccession = (Boolean)this.nodeService.getProperty(transferNode, PROP_TRANSFER_ACCESSION_INDICATOR);
            if (isAccession)
            {
                writer.write("<title>Accession Report</title></head>\n");
            }
            else
            {
                writer.write("<title>Transfer Report</title></head>\n");
            }
            writer.write("<style>\n");
            writer.write("body { font-family: arial,verdana; font-size: 81%; color: #333; }\n");
            writer.write(".records { margin-left: 20px; margin-top: 10px; }\n");
            writer.write(".record { padding: 5px; }\n");
            writer.write(".label { color: #111; }\n");
            writer.write(".nodeName { font-weight: bold; }\n");
            writer.write(".transferred-item { background-color: #eee; padding: 10px; margin-bottom: 15px; }\n");
            writer.write("</style>\n");
            if (isAccession)
            {
                writer.write("<body>\n<h1>Accession Report</h1>\n");
            }
            else
            {
                writer.write("<body>\n<h1>Transfer Report</h1>\n");
            }

            writer.write("<table cellpadding=\"3\" cellspacing=\"3\">");
            writer.write("<tr><td class=\"label\">Transfer Date:</td><td>");
            Date transferDate = (Date)this.nodeService.getProperty(transferNode, ContentModel.PROP_CREATED);
            writer.write(StringEscapeUtils.escapeHtml4(transferDate.toString()));
            writer.write("</td></tr>");
            writer.write("<tr><td class=\"label\">Transfer Location:</td><td>");
            if (isAccession)
            {
                writer.write("NARA");
            }
            else
            {
                writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(transferNode,
                        RecordsManagementModel.PROP_TRANSFER_LOCATION)));
            }
            writer.write("</td></tr>");
            writer.write("<tr><td class=\"label\">Performed By:</td><td>");
            writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(transferNode,
                        ContentModel.PROP_CREATOR)));
            writer.write("</td></tr>");
            writer.write("<tr><td class=\"label\">Disposition Authority:</td><td>");
            writer.write(dispositionAuthority != null ? StringEscapeUtils.escapeHtml4(dispositionAuthority) : "");
            writer.write("</td></tr></table>\n");

            writer.write("<h2>Transferred Items</h2>\n");

            // write out HTML representation of items to transfer
            generateTransferItemsHTML(writer, itemsToTransfer);

            // write the HTML footer
            writer.write("</body></html>");
        }

        return report;
    }

    /**
     * Generates the JSON to represent the given NodeRefs
     *
     * @param writer Writer to write to
     * @param itemsToTransfer NodeRefs being transferred
     * @throws IOException
     */
    protected void generateTransferItemsHTML(Writer writer, NodeRef[] itemsToTransfer)
        throws IOException
    {
        for (NodeRef item : itemsToTransfer)
        {
            writer.write("<div class=\"transferred-item\">\n");
            if (ddService.isSubClass(nodeService.getType(item), ContentModel.TYPE_FOLDER))
            {
                generateTransferFolderHTML(writer, item);
            }
            else
            {
                generateTransferRecordHTML(writer, item);
            }
            writer.write("</div>\n");
        }
    }

    /**
     * Generates the JSON to represent the given folder.
     *
     * @param writer Writer to write to
     * @param folderNode Folder being transferred
     * @throws IOException
     */
    protected void generateTransferFolderHTML(Writer writer, NodeRef folderNode)
        throws IOException
    {
        writer.write("<span class=\"nodeName\">");
        writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(folderNode,
                    ContentModel.PROP_NAME)));
        writer.write("</span>&nbsp;(Unique Folder Identifier:&nbsp;");
        writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(folderNode,
                    RecordsManagementModel.PROP_IDENTIFIER)));
        writer.write(")\n");

        writer.write("<div class=\"records\">\n");

        // NOTE: we don't expect any nested folder structures so just render
        //       the records contained in the folder.

        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(folderNode,
                    ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef child : assocs)
        {
            NodeRef childRef = child.getChildRef();
            if (this.nodeService.hasAspect(childRef, RecordsManagementModel.ASPECT_RECORD))
            {
                generateTransferRecordHTML(writer, childRef);
            }
        }

        writer.write("\n</div>\n");
    }

    /**
     * Generates the JSON to represent the given record.
     *
     * @param writer Writer to write to
     * @param recordNode Record being transferred
     * @throws IOException
     */
    protected void generateTransferRecordHTML(Writer writer, NodeRef recordNode)
        throws IOException
    {
        writer.write("<div class=\"record\">\n");
        writer.write("  <span class=\"nodeName\">");
        writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(recordNode,
                    ContentModel.PROP_NAME)));
        writer.write("</span>&nbsp;(Unique Record Identifier:&nbsp;");
        writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(recordNode,
                    RecordsManagementModel.PROP_IDENTIFIER)));
        writer.write(")");

        if (this.nodeService.hasAspect(recordNode, RecordsManagementModel.ASPECT_DECLARED_RECORD))
        {
            Date declaredOn = (Date)this.nodeService.getProperty(recordNode, RecordsManagementModel.PROP_DECLARED_AT);
            writer.write(" declared by ");
            writer.write(StringEscapeUtils.escapeHtml4((String)this.nodeService.getProperty(recordNode,
                        RecordsManagementModel.PROP_DECLARED_BY)));
            writer.write(" on ");
            writer.write(StringEscapeUtils.escapeHtml4(declaredOn.toString()));
        }

        writer.write("\n</div>\n");
    }

    /**
     * Files the given transfer report as a record in the given record folder.
     *
     * @param report Report to file
     * @param destination The destination record folder
     * @return NodeRef of the created record
     */
    protected NodeRef fileTransferReport(File report, NodeRef destination)
    {
        ParameterCheck.mandatory("report", report);
        ParameterCheck.mandatory("destination", destination);

        NodeRef record = null;

        Map<QName, Serializable> properties = new HashMap<>(1);
        properties.put(ContentModel.PROP_NAME, report.getName());

        // file the transfer report as an undeclared record
        record = this.nodeService.createNode(destination,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                QName.createValidLocalName(report.getName())),
                    ContentModel.TYPE_CONTENT, properties).getChildRef();

        // Set the content
        ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.setEncoding("UTF-8");
        writer.putContent(report);

        return record;
    }
}
