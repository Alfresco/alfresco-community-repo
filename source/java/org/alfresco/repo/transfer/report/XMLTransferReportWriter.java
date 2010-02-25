package org.alfresco.repo.transfer.report;

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.repo.transfer.manifest.ManifestModel;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
        
        this.writer.startPrefixMapping(PREFIX, TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI);
    
        // Start Transfer Manifest  // uri, name, prefix
        this.writer.startElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_REPORT, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_REPORT, EMPTY_ATTRIBUTES);
    }
    
    /**
     * End the transfer report 
     */
    public void endTransferReport() throws SAXException
    {
        // End Transfer Manifest
        writer.endElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_REPORT, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_REPORT);
        writer.endPrefixMapping(PREFIX);
        writer.endDocument();
    }
    
    /**
     * Write the target to the report
     */
    public void writeTarget(TransferTarget target) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "name", "name", "String", target.getName());
        attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "endpointHost", "endpointHost", "String",  target.getEndpointHost());
        attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "endpointPort", "endpointPort", "int",  String.valueOf(target.getEndpointPort()));
 
        writer.startElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_TARGET, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_TARGET, attributes);        
        writer.endElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_TARGET, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_TARGET);
    
    }
    
    /**
     * Write the definition to the report
     */
    public void writeDefinition(TransferDefinition definition) throws SAXException
    {
        writer.startElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_DEFINITION, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_DEFINITION, EMPTY_ATTRIBUTES);
        writer.endElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_DEFINITION, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_DEFINITION);
    
    }
    
    /**
     * Write the transfer manifest header
     */
    public void writeTransferEvents(List<TransferEvent> events) throws SAXException
    {
        writer.startElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_EVENTS, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_EVENTS, EMPTY_ATTRIBUTES);
        
        for(TransferEvent event : events)
        {
            writeTransferEvent(event);
        }
        
        writer.endElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_EVENTS, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_EVENTS);
    
    }
    
    /**
     * Write the transfer manifest header
     */
    public void writeTransferEvent(TransferEvent event) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, "date", "date", "dateTime", ISO8601DateFormat.format(event.getTime()));
        
        writer.startElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_EVENT, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_EVENT, attributes);
        
        String message = event.getMessage();
        if(message != null)
        {
            writer.characters(message.toCharArray(), 0, message.length());
        }
        writer.endElement(TransferReportModel.TRANSFER_REPORT_MODEL_1_0_URI, TransferReportModel.LOCALNAME_TRANSFER_EVENT, PREFIX + ":" + TransferReportModel.LOCALNAME_TRANSFER_EVENT);
    }
}
