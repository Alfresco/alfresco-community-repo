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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Returns a JSON representation of a transfer report.
 *
 * @author Gavin Cornwell
 */
@Deprecated
public class TransferReportGet extends BaseTransferWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(TransferReportGet.class);

    protected static final String REPORT_FILE_PREFIX = "report_";
    protected static final String REPORT_FILE_SUFFIX = ".json";

    protected DictionaryService ddService;
    protected DispositionService dispositionService;
    protected ContentStreamer contentStreamer;

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
     * @param dispositionService    the disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

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
        // generate the report (will be in JSON format)
        File report = generateJSONTransferReport(transferNode);

        // stream the report back to the client
        contentStreamer.streamContent(req, res, report, null, false, null, null);

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
    File generateJSONTransferReport(NodeRef transferNode) throws IOException
    {
        File report = TempFileProvider.createTempFile(REPORT_FILE_PREFIX, REPORT_FILE_SUFFIX);

        // create the writer
        try (FileOutputStream fileOutputStream = new FileOutputStream(report);
            Writer writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8")))
        {
            // get all 'transferred' nodes
            NodeRef[] itemsToTransfer = getTransferNodes(transferNode);

            if (logger.isDebugEnabled())
            {
                logger.debug("Generating JSON transfer report for " + itemsToTransfer.length +
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

            // write the JSON header
            writer.write("{\n\t\"data\":\n\t{");
            writer.write("\n\t\t\"transferDate\": \"");
            writer.write(ISO8601DateFormat.format(
                        (Date)this.nodeService.getProperty(transferNode, ContentModel.PROP_CREATED)));
            writer.write("\",\n\t\t\"transferPerformedBy\": \"");
            writer.write(AuthenticationUtil.getRunAsUser());
            writer.write("\",\n\t\t\"dispositionAuthority\": \"");
            writer.write(dispositionAuthority != null ? dispositionAuthority : "");
            writer.write("\",\n\t\t\"items\":\n\t\t[");

            // write out JSON representation of items to transfer
            generateTransferItemsJSON(writer, itemsToTransfer);

            // write the JSON footer
            writer.write("\n\t\t]\n\t}\n}");
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
    protected void generateTransferItemsJSON(Writer writer, NodeRef[] itemsToTransfer)
        throws IOException
    {
        boolean first = true;
        for (NodeRef item : itemsToTransfer)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.write(",");
            }

            if (ddService.isSubClass(nodeService.getType(item), ContentModel.TYPE_FOLDER))
            {
                generateTransferFolderJSON(writer, item);
            }
            else
            {
                generateTransferRecordJSON(writer, item);
            }
        }
    }

    /**
     * Generates the JSON to represent the given folder.
     *
     * @param writer Writer to write to
     * @param folderNode Folder being transferred
     * @throws IOException
     */
    protected void generateTransferFolderJSON(Writer writer, NodeRef folderNode)
        throws IOException
    {
        // TODO: Add identation

        writer.write("\n{\n\"type\":\"folder\",\n");
        writer.write("\"name\":\"");
        writer.write((String)nodeService.getProperty(folderNode, ContentModel.PROP_NAME));
        writer.write("\",\n\"nodeRef\":\"");
        writer.write(folderNode.toString());
        writer.write("\",\n\"id\":\"");
        writer.write((String)nodeService.getProperty(folderNode, RecordsManagementModel.PROP_IDENTIFIER));
        writer.write("\",\n\"children\":\n[");

        boolean first = true;
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(folderNode,
                    ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef child : assocs)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.write(",");
            }

            NodeRef childRef = child.getChildRef();
            if (ddService.isSubClass(nodeService.getType(childRef), ContentModel.TYPE_FOLDER))
            {
                generateTransferFolderJSON(writer, childRef);
            }
            else
            {
                generateTransferRecordJSON(writer, childRef);
            }
        }

        writer.write("\n]\n}");
    }

    /**
     * Generates the JSON to represent the given record.
     *
     * @param writer Writer to write to
     * @param recordNode Record being transferred
     * @throws IOException
     */
    protected void generateTransferRecordJSON(Writer writer, NodeRef recordNode)
        throws IOException
    {
        writer.write("\n{\n\"type\":\"record\",\n");
        writer.write("\"name\":\"");
        writer.write((String)nodeService.getProperty(recordNode, ContentModel.PROP_NAME));
        writer.write("\",\n\"nodeRef\":\"");
        writer.write(recordNode.toString());
        writer.write("\",\n\"id\":\"");
        writer.write((String)nodeService.getProperty(recordNode, RecordsManagementModel.PROP_IDENTIFIER));
        writer.write("\"");

        if (this.nodeService.hasAspect(recordNode, RecordsManagementModel.ASPECT_DECLARED_RECORD))
        {
            writer.write(",\n\"declaredBy\":\"");
            writer.write((String)nodeService.getProperty(recordNode, RecordsManagementModel.PROP_DECLARED_BY));
            writer.write("\",\n\"declaredAt\":\"");
            writer.write(ISO8601DateFormat.format(
                        (Date)this.nodeService.getProperty(recordNode, RecordsManagementModel.PROP_DECLARED_AT)));
            writer.write("\"");
        }

        writer.write("\n}");
    }
}
