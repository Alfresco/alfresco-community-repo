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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transfer.Transfer;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

public class TransferReporterImpl implements TransferReporter
{
    private NodeService nodeService;
    private ContentService contentService;
    
    /** Default encoding **/
    private static String DEFAULT_ENCODING = "UTF-8";
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "contentService", contentService);
    }
    
    /**
     * Create a new transfer report
     * @return NodeRef the node ref of the new transfer report
     */
    public NodeRef createTransferReport(Transfer transfer,
                TransferTarget target, 
                TransferDefinition definition, 
                List<TransferEvent> events)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();
        String transferId = transfer.getTransferId();
        String title = "transfer report: success :" + transferId;
        String name = transferId;
        String description = "successful transfer report";
        
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        ChildAssociationRef ref = nodeService.createNode(target.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), TransferModel.TYPE_TRANSFER_REPORT, properties);
        ContentWriter writer = contentService.getWriter(ref.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setLocale(Locale.getDefault());
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding("UTF-8");
        
        //
        XMLTransferReportWriter reportWriter = new XMLTransferReportWriter();
        
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(writer.getContentOutputStream()));

        try
        {
            reportWriter.startTransferReport("UTF-8", bufferedWriter);
            
            // Header
            reportWriter.writeTarget(target);
            
            reportWriter.writeDefinition(definition);
            
            // Detail
            reportWriter.writeTransferEvents(events);
            
            reportWriter.endTransferReport();
            
            return ref.getChildRef();
        }
        
        catch (SAXException se)
        {
            return null;
        }
        finally
        {
            try
            {
                bufferedWriter.close();
            }
            catch (IOException error)
            {
                error.printStackTrace();
            }
        }
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public ContentService getContentService()
    {
        return contentService;
    }

}
