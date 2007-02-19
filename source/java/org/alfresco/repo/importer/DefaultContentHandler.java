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
package org.alfresco.repo.importer;

import java.io.InputStream;

import org.alfresco.util.ParameterCheck;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Default Import Content Handler
 * 
 * Responsible for interacting with an Alfresco Importer.
 */
public class DefaultContentHandler
    implements ImportContentHandler, ErrorHandler
{
    private ImportContentHandler targetHandler = null;
    private Importer importer = null;

    
    /**
     * Construct
     * 
     * @param targetHandler
     */
    public DefaultContentHandler(ImportContentHandler targetHandler)
    {
        ParameterCheck.mandatory("targetHandler", targetHandler);
        this.targetHandler = targetHandler;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportContentHandler#setImporter(org.alfresco.repo.importer.Importer)
     */
    public void setImporter(Importer importer)
    {
        this.importer = importer;
        this.targetHandler.setImporter(importer);
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportContentHandler#importStream(java.lang.String)
     */
    public InputStream importStream(String content)
    {
        return targetHandler.importStream(content);
    }
    
    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator)
    {
        targetHandler.setDocumentLocator(locator);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException
    {
        importer.start();
        targetHandler.startDocument();
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException
    {
        try
        {
            targetHandler.endDocument();
        }
        finally
        {
            importer.end();
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        targetHandler.startPrefixMapping(prefix, uri);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException
    {
        targetHandler.endPrefixMapping(prefix);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        targetHandler.startElement(uri, localName, qName, atts);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        targetHandler.endElement(uri, localName, qName);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        targetHandler.characters(ch, start, length);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        targetHandler.ignorableWhitespace(ch, start, length);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException
    {
        targetHandler.processingInstruction(target, data);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException
    {
        targetHandler.skippedEntity(name);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException
    {
        try
        {
            targetHandler.error(exception);
        }
        finally
        {
            importer.error(exception);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
        try
        {
            targetHandler.error(exception);
        }
        finally
        {
            importer.error(exception);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException
    {
        targetHandler.warning(exception);
    }

}
