/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
 * Base Import Content Handler
 */
public class DefaultContentHandler
    implements ImportContentHandler, ErrorHandler
{
    private ImportContentHandler targetHandler = null;
    private Importer importer = null;

    
    public DefaultContentHandler(ImportContentHandler targetHandler)
    {
        ParameterCheck.mandatory("targetHandler", targetHandler);
        this.targetHandler = targetHandler;
    }

    public void setImporter(Importer importer)
    {
        this.importer = importer;
        this.targetHandler.setImporter(importer);
    }

    public InputStream importStream(String content)
    {
        return targetHandler.importStream(content);
    }

    public void setDocumentLocator(Locator locator)
    {
        targetHandler.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException
    {
        importer.start();
        targetHandler.startDocument();
    }

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

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        targetHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
        targetHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        targetHandler.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        targetHandler.endElement(uri, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        targetHandler.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        targetHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException
    {
        targetHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException
    {
        targetHandler.skippedEntity(name);
    }


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


    public void warning(SAXParseException exception) throws SAXException
    {
        targetHandler.warning(exception);
    }


}
