/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.importer;

import java.io.InputStream;

import org.springframework.extensions.surf.util.ParameterCheck;
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
     *            ImportContentHandler
     */
    public DefaultContentHandler(ImportContentHandler targetHandler)
    {
        ParameterCheck.mandatory("targetHandler", targetHandler);
        this.targetHandler = targetHandler;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.importer.ImportContentHandler#setImporter(org.alfresco.repo.importer.Importer) */
    public void setImporter(Importer importer)
    {
        this.importer = importer;
        this.targetHandler.setImporter(importer);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.importer.ImportContentHandler#importStream(java.lang.String) */
    public InputStream importStream(String content)
    {
        return targetHandler.importStream(content);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator) */
    public void setDocumentLocator(Locator locator)
    {
        targetHandler.setDocumentLocator(locator);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument() */
    public void startDocument() throws SAXException
    {
        importer.start();
        targetHandler.startDocument();
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument() */
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

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String) */
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        targetHandler.startPrefixMapping(prefix, uri);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String) */
    public void endPrefixMapping(String prefix) throws SAXException
    {
        targetHandler.endPrefixMapping(prefix);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes) */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        targetHandler.startElement(uri, localName, qName, atts);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String) */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        targetHandler.endElement(uri, localName, qName);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int) */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        targetHandler.characters(ch, start, length);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int) */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        targetHandler.ignorableWhitespace(ch, start, length);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String) */
    public void processingInstruction(String target, String data) throws SAXException
    {
        targetHandler.processingInstruction(target, data);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String) */
    public void skippedEntity(String name) throws SAXException
    {
        targetHandler.skippedEntity(name);
    }

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException) */
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

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException) */
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

    /* (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException) */
    public void warning(SAXParseException exception) throws SAXException
    {
        targetHandler.warning(exception);
    }

}
