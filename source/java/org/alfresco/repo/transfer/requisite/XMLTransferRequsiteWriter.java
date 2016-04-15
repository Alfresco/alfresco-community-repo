package org.alfresco.repo.transfer.requisite;

import java.io.Writer;

import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Writes the transfer requsite out in XML format to the specified writer.
 *  
 * XMLTransferRequsiteWriter is a statefull object used for writing out a single transfer requsite 
 * file in XML format to the writer passed in via startTransferRequsite.
 *
 * @author Mark Rogers
 */
public class XMLTransferRequsiteWriter implements TransferRequsiteWriter
{
    private static final Log log = LogFactory.getLog(XMLTransferRequsiteWriter.class);
    
    public XMLTransferRequsiteWriter(Writer out)
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding("UTF-8");

        this.writer = new XMLWriter(out, format);
    }

    private XMLWriter writer;

    final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    final String PREFIX = RequsiteModel.REQUSITE_PREFIX;

    /**
     * Start the transfer manifest
     */
    public void startTransferRequsite()
    {
        try
        {
            this.writer.startDocument();

            this.writer.startPrefixMapping(PREFIX, RequsiteModel.TRANSFER_MODEL_1_0_URI);
            this.writer.startPrefixMapping("cm", NamespaceService.CONTENT_MODEL_1_0_URI);

            // Start Transfer Manifest // uri, name, prefix
            this.writer.startElement(TransferModel.TRANSFER_MODEL_1_0_URI,
                    RequsiteModel.LOCALNAME_TRANSFER_REQUSITE, PREFIX + ":"
                                + RequsiteModel.LOCALNAME_TRANSFER_REQUSITE, EMPTY_ATTRIBUTES);
        } 
        catch (SAXException se)
        {
            log.debug("error", se);
        }
    }

    /**
     * End the transfer manifest
     */
    public void endTransferRequsite()
    {
        try
        {
            // End Transfer Manifest
            writer.endElement(TransferModel.TRANSFER_MODEL_1_0_URI,
                    RequsiteModel.LOCALNAME_TRANSFER_REQUSITE, PREFIX + ":"
                                + RequsiteModel.LOCALNAME_TRANSFER_REQUSITE);
            writer.endPrefixMapping(PREFIX);

            writer.endDocument();
        }
        catch (SAXException se)
        {
            log.debug("error", se);
        }
    }

    public void missingContent(NodeRef node, QName qname, String name)
    {
        log.debug("write missing content");
        try
        {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("uri", "nodeRef", "nodeRef", "String", node.toString());
            attributes.addAttribute("uri", "qname", "qname", "String", qname.toString());
            attributes.addAttribute("uri", "name", "name", "String", name.toString());

            // Start Missing Content
            this.writer.startElement(TransferModel.TRANSFER_MODEL_1_0_URI,
                RequsiteModel.LOCALNAME_ELEMENT_CONTENT, PREFIX + ":"
                            + RequsiteModel.LOCALNAME_ELEMENT_CONTENT, attributes);
        
            // Missing Content
            writer.endElement(TransferModel.TRANSFER_MODEL_1_0_URI,
                RequsiteModel.LOCALNAME_ELEMENT_CONTENT, PREFIX + ":"
                            + RequsiteModel.LOCALNAME_ELEMENT_CONTENT);
        }
        catch (SAXException se)
        {
            log.debug("error", se);
        }
    }

 
}
