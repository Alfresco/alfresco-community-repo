/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content.metadata.xml;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.repo.content.ContentWorkerSelector;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A selector that looks at the root node of an XML document to determine which worker to provide.
 * There are many ways to identify XML documents and this is probably the simplest.  Alternate
 * implementations might execute a series of xpath statements or look for specific namespace
 * declarations in the document.  The net result is the same, i.e. given an XML document, an
 * extracter is provided to the caller.
 * <p>
 * In this selector, there is no guarantee that the different extracters will generate the same
 * (or even nearly the same) metadata.  It is up to the configurer to ensure that if it is a
 * requirement, but otherwise each extracter is responsible for its own mappings.  Mostly, though,
 * a root node match will imply a structure that has the necessary metadata.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class RootElementNameMetadataExtracterSelector
        extends DefaultHandler
        implements ContentWorkerSelector<MetadataExtracter>
{
    private static Log logger = LogFactory.getLog(RootElementNameMetadataExtracterSelector.class);
    
    private SAXParserFactory saxParserFactory;
    private Set<String> supportedMimetypes;
    private Map<String, MetadataExtracter> extractersByRootElementName;
    
    public RootElementNameMetadataExtracterSelector()
    {
        saxParserFactory = SAXParserFactory.newInstance();
        supportedMimetypes = new HashSet<String>();
        supportedMimetypes.add(MimetypeMap.MIMETYPE_XML);
        extractersByRootElementName = Collections.emptyMap();
    }

    /**
     * Optionally set the mimetypes supported.  They must be XML formats that the chosen
     * parser will be able to handle.
     * 
     * @param supportedMimetypes        the list of mimetypes.  The default is <b>text/xml</b>.
     */
    public void setSupportedMimetypes(Set<String> supportedMimetypes)
    {
        this.supportedMimetypes = supportedMimetypes;
    }

    /**
     * Set the extractors to use.
     * 
     * @param extracters            a map of {@linkplain MetadataExtracter} instances
     *                              keyed by root element name
     */
    public void setExtracters(Map<String, MetadataExtracter> extracters)
    {
        this.extractersByRootElementName = extracters;
    }

    /**
     * Performs a match of the root element name to find the correct extracter.
     */
    public MetadataExtracter getWorker(ContentReader reader)
    {
        if (!supportedMimetypes.contains(reader.getMimetype()))
        {
            return null;
        }
        MetadataExtracter extracter = null;
        InputStream is = null;
        String rootElementName = null;
        try
        {
            is = reader.getContentInputStream();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(is, this);
            // No match possible
        }
        catch (RootElementFoundException e)
        {
            rootElementName = e.getElementName();
            extracter = extractersByRootElementName.get(rootElementName);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to extract root element from XML document", e);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("\n" +
                    "Chosen metadata extracter for reader: \n" +
                    "   Reader:       " + reader + "\n" +
                    "   Root Element: " + rootElementName + "\n" +
                    "   Extracter:    " + extracter);
        }
        return extracter;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        throw new RootElementFoundException(localName);
    }

    /**
     * An exception to break out of the XML parsing early
     */
    private static class RootElementFoundException extends SAXException
    {
        private static final long serialVersionUID = 6845880422947198814L;
        private String elementName;
        public RootElementFoundException(String elementName)
        {
            super(elementName);
            this.elementName = elementName;
        }
        public String getElementName()
        {
            return elementName;
        }
    }
}
