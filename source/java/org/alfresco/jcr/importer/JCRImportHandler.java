/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.jcr.importer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.repo.importer.ImportContentHandler;
import org.alfresco.repo.importer.Importer;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.NamespaceSupport;


/**
 * Import Handler that is sensitive to Document and System View XML schemas.
 *
 * @author David Caruana
 */
public class JCRImportHandler implements ImportContentHandler
{
    private Importer importer;
    private SessionImpl session;
    private NamespaceContext namespaceContext;
    private ImportContentHandler targetHandler = null;
    
    
    /**
     * Construct
     * 
     * @param session
     */    
    public JCRImportHandler(SessionImpl session)
    {
        this.session = session;
        this.namespaceContext = new NamespaceContext();
    }
    
    /*
     *  (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportContentHandler#setImporter(org.alfresco.repo.importer.Importer)
     */
    public void setImporter(Importer importer)
    {
        this.importer = importer;
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
        // NOOP
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException
    {
        namespaceContext.reset();
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException
    {
        targetHandler.endDocument();
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        // ensure uri has been registered
        NamespacePrefixResolver resolver = session.getNamespaceResolver();
        Collection<String> uris = resolver.getURIs();
        if (!uris.contains(uri))
        {
            throw new ImporterException("Namespace URI " + uri + " has not been registered with the repository");
        }
        
        // register prefix within this namespace context
        namespaceContext.registerPrefix(prefix, uri);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        namespaceContext.pushContext();

        // determine content handler based on first element of document
        if (targetHandler == null)
        {
            if (JCRNamespace.SV_URI.equals(uri))
            {
                targetHandler = new JCRSystemXMLHandler(session, namespaceContext);
            }
            else
            {
                targetHandler = new JCRDocXMLHandler(session, namespaceContext);
            }
            targetHandler.setImporter(importer);
            targetHandler.startDocument();
        }
        
        targetHandler.startElement(uri, localName, qName, atts);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        targetHandler.endElement(uri, localName, qName);
        namespaceContext.popContext();
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
        targetHandler.characters(ch, start, length);
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
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException
    {
        targetHandler.warning(exception);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException
    {
        targetHandler.error(exception);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
        targetHandler.fatalError(exception);
    }

    
    /**
     * Namespace Context
     *
     * Implementation supported by NamespaceSupport which itself does not
     * handle empty uri registration.
     */
    private static class NamespaceContext implements NamespacePrefixResolver
    {
        private final NamespaceSupport context;
        private static final String REMAPPED_DEFAULT_URI = " ";


        /**
         * Construct
         */
        private NamespaceContext()
        {
            context = new NamespaceSupport();
        }

        /**
         * Clear namespace declarations
         */
        private void reset()
        {
            context.reset();
        }

        /**
         * Push a new Namespace Context
         */
        private void pushContext()
        {
            context.pushContext();
        }

        /**
         * Pop a Namespace Context
         */
        private void popContext()
        {
            context.popContext();
        }

        /**
         * Register a namespace prefix
         * 
         * @param prefix
         * @param uri
         * @return  true => legal prefix; false => illegal prefix
         */
        private boolean registerPrefix(String prefix, String uri)
        {
            if (NamespaceService.DEFAULT_URI.equals(uri))
            {
                uri = REMAPPED_DEFAULT_URI;
            }
            return context.declarePrefix(prefix, uri);
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.namespace.NamespacePrefixResolver#getNamespaceURI(java.lang.String)
         */
        public String getNamespaceURI(String prefix) throws org.alfresco.service.namespace.NamespaceException
        {
            String uri = context.getURI(prefix);
            if (uri == null)
            {
                throw new org.alfresco.service.namespace.NamespaceException("Namespace prefix " + prefix + " not registered.");
            }
            if (REMAPPED_DEFAULT_URI.equals(uri))
            {
                return NamespaceService.DEFAULT_URI;
            }
            return uri;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.namespace.NamespacePrefixResolver#getPrefixes(java.lang.String)
         */
        public Collection<String> getPrefixes(String namespaceURI) throws org.alfresco.service.namespace.NamespaceException
        {
            if (NamespaceService.DEFAULT_URI.equals(namespaceURI))
            {
                namespaceURI = REMAPPED_DEFAULT_URI;
            }
            String prefix = context.getPrefix(namespaceURI);
            if (prefix == null)
            {
                if (namespaceURI.equals(context.getURI(NamespaceService.DEFAULT_PREFIX)))
                {
                    prefix = NamespaceService.DEFAULT_PREFIX;
                }
                else
                {
                    throw new org.alfresco.service.namespace.NamespaceException("Namespace URI " + namespaceURI + " not registered.");
                }
            }
            List<String> prefixes = new ArrayList<String>(1);
            prefixes.add(prefix);
            return prefixes;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.namespace.NamespacePrefixResolver#getPrefixes()
         */
        public Collection<String> getPrefixes()
        {
            // NOTE: not required in this context
            return null;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.namespace.NamespacePrefixResolver#getURIs()
         */
        public Collection<String> getURIs()
        {
            // NOTE: not required in this context
            return null;
        }
    }
    
}
