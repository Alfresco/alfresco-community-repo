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

import java.io.Writer;
import java.util.List;

import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.repo.transfer.manifest.ManifestModel;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventEndState;
import org.alfresco.service.cmr.transfer.TransferEventEnterState;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.namespace.QName;
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
public class XMLTransferReportWriter
{
    public XMLTransferReportWriter()
    {
    }
    
    private XMLWriter writer;
    
    final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
    
    final String PREFIX = TransferReportModel.REPORT_PREFIX;
    
    /**
     * Start the transfer report
     */
    public void startTransferReport(String encoding, Writer writer) throws SAXException
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding(encoding);
        
        this.writer = new XMLWriter(writer, format);
        this.writer.startDocument();
        
        this.writer.startPrefixMapping(PREFIX, TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI);
    
        // Start Transfer Manifest  // uri, name, prefix
        this.writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_REPORT,  PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_REPORT, EMPTY_ATTRIBUTES);
    }
    
    /**
     * End the transfer report 
     */
    public void endTransferReport() throws SAXException
    {
        // End Transfer Manifest
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_REPORT,  PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_REPORT);
        writer.endPrefixMapping(PREFIX);
        writer.endDocument();
    }
    
    /**
     * Write the target to the report
     */
    public void writeTarget(TransferTarget target) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "name", "name", "String", target.getName());
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "endpointHost", "endpointHost", "String",  target.getEndpointHost());
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "endpointPort", "endpointPort", "int",  String.valueOf(target.getEndpointPort()));
 
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_TARGET, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_TARGET, attributes);        
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_TARGET, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_TARGET);
    
    }
    
    /**
     * Write the definition to the report
     */
    public void writeDefinition(TransferDefinition definition) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "isReadOnly", "isReadOnly", "boolean", definition.isReadOnly()?"true":"false");
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "isSync", "isSync", "boolean", definition.isSync()?"true":"false");    
        
        if(definition.getNodes() != null)
        {
            attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "numberOfNodes", "numberOfNodes", "string", String.valueOf(definition.getNodes().size()));  
        }
        
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_DEFINITION, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_DEFINITION, attributes);
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_DEFINITION, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_DEFINITION);
    
    }
    
    /**
     * Write the definition to the report
     */
    public void writeException(Exception e) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "type", "type", "String", e.getClass().getName());
        attributes.addAttribute(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, "message", "message", "String", e.getMessage());
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_EXCEPTION, PREFIX + ":" + TransferReportModel.LOCALNAME_EXCEPTION, attributes);
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_EXCEPTION, PREFIX + ":" + TransferReportModel.LOCALNAME_EXCEPTION);
    }
    
    /**
     * Write the transfer manifest header
     */
    public void writeTransferEvents(List<TransferEvent> events) throws SAXException
    {
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_EVENTS, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_EVENTS, EMPTY_ATTRIBUTES);
        
        for(TransferEvent event : events)
        {
            writeTransferEvent(event);
        }
        
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_EVENTS, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_EVENTS);
    
    }
    
    /**
     * Write the transfer manifest header
     */
    public void writeNodeSummary(TransferManifestNode node) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("uri", "nodeRef", "nodeRef", "String", node.getNodeRef().toString());
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_NODE, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_NODE, attributes);
        
        if(node.getPrimaryParentAssoc() != null)
        {
            writePrimaryParent(node.getPrimaryParentAssoc(), node.getParentPath());
        }  
                   
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_NODE, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_NODE);
    }
    
    private void writePrimaryParent(ChildAssociationRef parentAssoc, Path parentPath) throws SAXException
    {   
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_PRIMARY_PARENT, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_PRIMARY_PARENT,  EMPTY_ATTRIBUTES);

        writeParentAssoc(parentAssoc);
        
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_PRIMARY_PATH, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_PRIMARY_PATH,  EMPTY_ATTRIBUTES);
        if(parentPath != null)
        {  
            String path = parentPath.toString();
            writer.characters(path.toCharArray(), 0, path.length()); 
        }
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_PRIMARY_PATH, PREFIX + ":" + ManifestModel.LOCALNAME_ELEMENT_PRIMARY_PATH); 

        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, TransferReportModel.LOCALNAME_TRANSFER_PRIMARY_PARENT, PREFIX + ":" + ManifestModel.LOCALNAME_ELEMENT_PRIMARY_PARENT); 
    }
    
    private void writeParentAssoc(ChildAssociationRef assoc) throws SAXException
    {
        if(assoc != null)
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(TransferModel.TRANSFER_MODEL_1_0_URI, "from", "from", "String", assoc.getParentRef().toString());     
            attributes.addAttribute(TransferModel.TRANSFER_MODEL_1_0_URI, "type", "type", "String", formatQName(assoc.getTypeQName()));
            attributes.addAttribute(TransferModel.TRANSFER_MODEL_1_0_URI, "type", "isPrimary", "Boolean", assoc.isPrimary()?"true":"false");
            writer.startElement(TransferModel.TRANSFER_MODEL_1_0_URI, ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOC, PREFIX + ":" + ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOC,  attributes);
            String name= formatQName(assoc.getQName());
            writer.characters(name.toCharArray(), 0, name.length());            
            assoc.isPrimary();

            writer.endElement(TransferModel.TRANSFER_MODEL_1_0_URI, ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOC, PREFIX + ":" + ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOC); 
        }
    }
    
    
    /**
     * Write the transfer event
     */
    public void writeTransferEvent(TransferEvent event) throws SAXException
    {
        
        XMLTransferEventFormatter formatter = XMLTransferEventFormatterFactory.getFormatter(event);

        AttributesImpl attributes = formatter.getAttributes(event);
        String elementName = formatter.getElementName(event);
        String message = formatter.getMessage(event);
        
        writer.startElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, elementName, PREFIX + ":" + elementName, attributes);
        if(message != null)
        {
                writer.characters(message.toCharArray(), 0, message.length());
        }
        writer.endElement(TransferReportModel2.TRANSFER_REPORT_MODEL_2_0_URI, elementName, PREFIX + ":" + elementName);
    }
    
    private String formatQName(QName qname)
    {
        return qname.toString();
    }
    
}
