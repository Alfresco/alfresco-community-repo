/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.requisite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.repo.transfer.RepoRequsiteManifestProcessorImpl;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
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

            this.writer.startPrefixMapping(PREFIX, TransferModel.TRANSFER_MODEL_1_0_URI);
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
