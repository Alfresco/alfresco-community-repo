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
package org.alfresco.repo.transfer.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transfer.Transfer;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.dom4j.io.XMLWriter;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.xml.sax.SAXException;

/**
 * Implementation of TransferReporter
 *
 */
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
     * Write exception transfer report
     * 
     * @return NodeRef the node ref of the new transfer report
     */
    public NodeRef createTransferReport(Exception e, 
                TransferTarget target,
                TransferDefinition definition, 
                List<TransferEvent> events, 
                File snapshotFile)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();
        
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssSSSZ");
        String timeNow = format.format(new Date());
     
        String title = "Transfer report, error,  " + timeNow;
        String description = "Transfer error report";
        String name = "Transfer error report, " + timeNow;
        
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        ChildAssociationRef ref = nodeService.createNode(target.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), TransferModel.TYPE_TRANSFER_REPORT, properties);
        ContentWriter writer = contentService.getWriter(ref.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setLocale(Locale.getDefault());
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding(DEFAULT_ENCODING);
        
        //
        XMLTransferReportWriter reportWriter = new XMLTransferReportWriter();
        
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(writer.getContentOutputStream()));

        try
        {
            reportWriter.startTransferReport(DEFAULT_ENCODING, bufferedWriter);
            
            // Header
            reportWriter.writeTarget(target);
            
            reportWriter.writeDefinition(definition);
            
            reportWriter.writeException(e);
            
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
    
    /**
     * Create a new transfer report of success
     * 
     * @return NodeRef the node ref of the new transfer report
     */
    public NodeRef createTransferReport(Transfer transfer, 
                TransferTarget target,
                TransferDefinition definition, 
                List<TransferEvent> events, 
                File snapshotFile)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();
        
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssSSSZ");
        String timeNow = format.format(new Date());
        
        String title = "Transfer report, " + timeNow + "success";
        String description = "Transfer report success targetName : " + target.getName();
        String name = "Transfer report, " + timeNow;
        
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        ChildAssociationRef ref = nodeService.createNode(target.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), TransferModel.TYPE_TRANSFER_REPORT, properties);
        ContentWriter writer = contentService.getWriter(ref.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setLocale(Locale.getDefault());
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding(DEFAULT_ENCODING);
        
        //
        final XMLTransferReportWriter reportWriter = new XMLTransferReportWriter();
        
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(writer.getContentOutputStream()));

        try
        {
            reportWriter.startTransferReport(DEFAULT_ENCODING, bufferedWriter);
            
            // Header
            reportWriter.writeTarget(target);
            
            reportWriter.writeDefinition(definition);
            
            /**
             * Write the node summary details to the transfer report
             */
            TransferManifestProcessor processor = new TransferManifestProcessor()
            {
                public void processTransferManifestNode(TransferManifestNormalNode node) 
                {
                    
                    try
                    {
                        reportWriter.writeNodeSummary(node);
                    }
                    catch (SAXException error)
                    {
                        error.printStackTrace();
                    }
                }
                
                public void processTransferManifestNode(TransferManifestDeletedNode node)
                { 
                    try
                    {
                        reportWriter.writeNodeSummary(node);
                    }
                    catch (SAXException error)
                    {
                        error.printStackTrace();
                    }
                }

                public void processTransferManifiestHeader(TransferManifestHeader header){/* NO-OP */ }
                public void startTransferManifest(){ /* NO-OP */ }
                public void endTransferManifest(){ /* NO-OP */ }
            };
            
            /**
             * Step 3: wire up the manifest reader to a manifest processor
             */
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser parser;
            parser = saxParserFactory.newSAXParser();                   
            XMLTransferManifestReader reader = new XMLTransferManifestReader(processor);

            /**
             * Step 4: start the magic Give the manifest file to the manifest reader
             */
            try
            {
                parser.parse(snapshotFile, reader);
            }
            catch (IOException error)
            {
                //TODO temp code
                error.printStackTrace();
                return null;
            }
              
            // Detail Events
            reportWriter.writeTransferEvents(events);
            
            reportWriter.endTransferReport();
            
            return ref.getChildRef();
        }
        
        catch (SAXException se)
        {
            //TODO Temp code
            return null;
        }
        catch (ParserConfigurationException error)
        {
            // TODO temp code
            error.printStackTrace();
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
