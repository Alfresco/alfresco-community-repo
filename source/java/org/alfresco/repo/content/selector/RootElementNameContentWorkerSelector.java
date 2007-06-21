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
package org.alfresco.repo.content.selector;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.repo.content.ContentWorker;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A selector that looks at the root node of an XML document to determine which worker to provide.
 * There are many ways to identify XML documents and this is probably the simplest.  Alternate
 * implementations might execute a series of xpath statements or look for specific namespace
 * declarations in the document.  The net result is the same, i.e. given an XML document, a
 * worker is provided to the caller.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class RootElementNameContentWorkerSelector<W extends ContentWorker>
        extends DefaultHandler
        implements ContentWorkerSelector<ContentWorker>
{
    private static Log logger = LogFactory.getLog(RootElementNameContentWorkerSelector.class);
    
    private SAXParserFactory saxParserFactory;
    private Set<String> supportedMimetypes;
    private Map<String, W> workersByRootElementName;
    
    public RootElementNameContentWorkerSelector()
    {
        saxParserFactory = SAXParserFactory.newInstance();
        supportedMimetypes = new HashSet<String>();
        supportedMimetypes.add(MimetypeMap.MIMETYPE_XML);
        workersByRootElementName = Collections.emptyMap();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("RootElementNameContentWorkerSelector")
          .append("[ workers=").append(workersByRootElementName)
          .append("]");
        return sb.toString();
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
     * Set the workers to choose from.
     * 
     * @param workers               a map of {@linkplain ContentWorker} instances
     *                              keyed by root element name
     */
    public void setWorkers(Map<String, W> workers)
    {
        this.workersByRootElementName = workers;
    }

    /**
     * Checks the configuration.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "workers", workersByRootElementName);
        PropertyCheck.mandatory(this, "supportedMimetypes", supportedMimetypes);
    }
    
    /**
     * Performs a match of the root element name to find the correct content worker.
     */
    public W getWorker(ContentReader reader)
    {
        if (!supportedMimetypes.contains(reader.getMimetype()))
        {
            return null;
        }
        W worker = null;
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
            worker = workersByRootElementName.get(rootElementName);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("\n" +
                    "Failed to extract root element from XML document: \n" +
                    "   Reader:   " + reader + "\n" +
                    "   Selector: " + this,
                    e);
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
                    "Chosen content worker for reader: \n" +
                    "   Reader:       " + reader + "\n" +
                    "   Root Element: " + rootElementName + "\n" +
                    "   Worker:       " + worker);
        }
        return worker;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        throw new RootElementFoundException(qName);
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
