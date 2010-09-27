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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transfer.Transfer;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Implementation of TransferReporter
 *
 */
public class TransferReporterImpl implements TransferReporter
{
    private NodeService nodeService;
    private ContentService contentService;
    
    private static Log logger = LogFactory.getLog(TransferReporterImpl.class);
    
    /** Default encoding **/
    private static String DEFAULT_ENCODING = "UTF-8";
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "contentService", contentService);
    }
    
    public NodeRef createDestinationTransferReport(TransferTarget target)
    {
        return null;
        
    }
    
    /**
     * Write exception transfer report
     * 
     * @return NodeRef the node ref of the new transfer report
     */
    public NodeRef createTransferReport(String transferName,
                Exception e, 
                TransferTarget target,
                TransferDefinition definition, 
                List<TransferEvent> events, 
                File snapshotFile)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();
        
        String title = transferName;
        String description = "Transfer Report - target: " + target.getName();
        String name = transferName + ".xml";
        
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        ChildAssociationRef ref = nodeService.createNode(target.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), TransferModel.TYPE_TRANSFER_REPORT, properties);
        ContentWriter writer = contentService.getWriter(ref.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setLocale(Locale.getDefault());
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding(DEFAULT_ENCODING);
        
        XMLTransferReportWriter reportWriter = new XMLTransferReportWriter();
        
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(writer.getContentOutputStream()));

        try
        {
            reportWriter.startTransferReport(DEFAULT_ENCODING, bufferedWriter);
            
            reportWriter.writeTarget(target);
            
            reportWriter.writeDefinition(definition);
            
            reportWriter.writeException(e);
            
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
    public NodeRef createTransferReport(String transferName,
                Transfer transfer, 
                TransferTarget target,
                TransferDefinition definition, 
                List<TransferEvent> events, 
                File snapshotFile)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();
               
        String title = transferName;
        String description = "Transfer Report - target: " + target.getName();
        String name = transferName + ".xml";
        
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
            
            // Definition of transfer
            reportWriter.writeDefinition(definition);
            
            // Events of transfer
            reportWriter.writeTransferEvents(events);
            
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
    
    /*
     */
    public NodeRef writeDestinationReport(String transferName,
            TransferTarget target,
            File tempFile)
    {
       
        String title = transferName + "_destination";
        String description = "Transfer Destination Report - target: " + target.getName();
        String name = title + ".xml";
        
        logger.debug("writing destination transfer report " + title);
        logger.debug("parent node ref " + target.getNodeRef());
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        ChildAssociationRef ref = nodeService.createNode(target.getNodeRef(), 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                TransferModel.TYPE_TRANSFER_REPORT_DEST, 
                properties);
        
        ContentWriter writer = contentService.getWriter(ref.getChildRef(), 
                ContentModel.PROP_CONTENT, true);
        writer.setLocale(Locale.getDefault());
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding(DEFAULT_ENCODING);
        writer.putContent(tempFile);
        
        logger.debug("written " + name + ", " + ref.getChildRef());
        
        return ref.getChildRef();
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
