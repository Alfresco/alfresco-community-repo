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
package org.alfresco.repo.transfer.reportd;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import org.alfresco.repo.transfer.TransferDestinationReportWriter;
import org.alfresco.repo.transfer.report.TransferReportModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Writes the Client Side Transfer Report out as XML.
 *
 * @author Mark Rogers
 */
public class XMLTransferDestinationReportWriter implements TransferDestinationReportWriter
{
    public XMLTransferDestinationReportWriter()
    {
    }
    
    private XMLWriter writer;
    
    final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
    
    final String PREFIX = TransferDestinationReportModel.REPORT_PREFIX;
    
    /**
     * Start the transfer report
     */
    public void startTransferReport(String encoding, Writer writer)
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding(encoding);
        
        try 
        {
        
        this.writer = new XMLWriter(writer, format);
        this.writer.startDocument();
        
        this.writer.startPrefixMapping(PREFIX, TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI);
    
        // Start Transfer Manifest  // uri, name, prefix
        this.writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_REPORT,  PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_REPORT, EMPTY_ATTRIBUTES);
        
        } 
        catch (SAXException se)
        {
            se.printStackTrace();
        }
    }
    
    /**
     * End the transfer report 
     */
    public void endTransferReport()
    {
        try 
        {
            // End Transfer Manifest
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_REPORT,  PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_REPORT);
            writer.endPrefixMapping(PREFIX);
            writer.endDocument();
            writer.flush();
            writer.close();
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        } 
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void writeChangeState(String state)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "state", "state", "String", state);
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(new Date()));
        
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_STATE, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_STATE, attributes);        
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_STATE, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_STATE);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        } 
    }
     
    /**
     * Write the exception to the report
     */
    public void writeException(Throwable e)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "type", "type", "String", e.getClass().getName());
            attributes.addAttribute(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "message", "message", "String", e.getMessage());
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_EXCEPTION, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_EXCEPTION, attributes);
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_EXCEPTION, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_EXCEPTION);
        }
        catch(SAXException se)
        {
        }
    }

    @Override
    public void writeComment(String comment)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(new Date()));
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_COMMENT, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_COMMENT, attributes);
            writer.characters(comment.toCharArray(), 0, comment.length());
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_COMMENT, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_COMMENT);
        }
        catch(SAXException se)
        {
        }
    }

    @Override
    public void writeCreated(NodeRef sourceNodeRef, NodeRef newNode, NodeRef newParentNodeRef, Path newPath)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(new Date()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "sourceNodeRef", "sourceNodeRef", "string", sourceNodeRef.toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "destinationNodeRef", "destinationNodeRef", "string", newNode.toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "parentNodeRef", "parentNodeRef", "string", newParentNodeRef.toString());
             
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_CREATED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_CREATED, attributes);        
            writeDestinationPath(newPath);
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_CREATED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_CREATED);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        }     
    }

    @Override
    public void writeDeleted(NodeRef sourceNodeRef, NodeRef deletedNode, Path oldPath)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(new Date()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "sourceNodeRef", "sourceNodeRef", "string", sourceNodeRef.toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "destinationNodeRef", "destinationNodeRef", "string", deletedNode.toString());
        
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_DELETED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_DELETED, attributes);        
            writeDestinationPath(oldPath);
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_DELETED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_DELETED);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        }            
    }

    @Override
    public void writeMoved(NodeRef sourceNodeRef, NodeRef updatedNode, Path oldPath, NodeRef newParentNodeRef, Path newPath)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(new Date()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "sourceNodeRef", "sourceNodeRef", "string", sourceNodeRef.toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "destinationNodeRef", "destinationNodeRef", "string", updatedNode.toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "newParentNodeRef", "newParentNodeRef", "string", newParentNodeRef.toString());      
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_MOVED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_MOVED, attributes);        
            writeDestinationPath(newPath);
            writeOldPath(oldPath);
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_MOVED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_MOVED);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        }         
    }

    @Override
    public void writeUpdated(NodeRef sourceNodeRef, NodeRef updatedNode, Path updatedPath)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(new Date()));
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "sourceNodeRef", "sourceNodeRef", "string", sourceNodeRef.toString());
            attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "destinationNodeRef", "destinationNodeRef", "string", updatedNode.toString());
        
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_UPDATED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_UPDATED, attributes);        
            writeDestinationPath(updatedPath);
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_UPDATED, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_UPDATED);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        } 
    }
    
    
    public void writeOldPath(Path path)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
           
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_OLD_PATH, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_OLD_PATH, attributes);        
            String sPath = path.toString();
            writer.characters(sPath.toCharArray(), 0, sPath.length());
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_OLD_PATH, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_OLD_PATH);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        }    
    }
    
    public void writeDestinationPath(Path path)
    {
        try
        {
            AttributesImpl attributes = new AttributesImpl();
           
            writer.startElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_PATH, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_PATH, attributes);        
            String sPath = path.toString();
            writer.characters(sPath.toCharArray(), 0, sPath.length());
            writer.endElement(TransferDestinationReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_PATH, PREFIX + ":" + TransferDestinationReportModel.LOCALNAME_TRANSFER_DEST_PATH);        
        }
        catch (SAXException se)
        {
            // TODO Auto-generated catch block
            se.printStackTrace();
        }    
    }
}
