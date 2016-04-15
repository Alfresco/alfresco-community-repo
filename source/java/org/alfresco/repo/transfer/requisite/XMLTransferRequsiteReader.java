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
package org.alfresco.repo.transfer.requisite;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX XML Content Handler to read a transfer manifest XML Stream and 
 * delegate processing of the manifest to the specified TransferRequsiteProcessor
 *
 * @author Mark Rogers
 */
public class XMLTransferRequsiteReader extends DefaultHandler implements ContentHandler, NamespacePrefixResolver 
{
    private  TransferRequsiteProcessor processor;
    
    /**
     * These are the namespaces used within the document - there may be a different mapping to 
     * the namespaces of the Data Dictionary.
     */ 
    LinkedList<HashMap<String, String>> namespaces = new LinkedList<HashMap<String, String>>();

    final String REQUSITE_URI = RequsiteModel.REQUSITE_MODEL_1_0_URI;
    final String XMLNS_URI = "http://www.w3.org/XML/1998/namespace"; 
    
    /*
     *  Current State of the parser
     */
    private StringBuffer buffer;
    private Map<String, Object>props = new HashMap<String, Object>();
    
    /**
     * Constructor
     * @param processor
     */
    public XMLTransferRequsiteReader(TransferRequsiteProcessor processor)
    {
        this.processor = processor;
        
        // prefix to uri map
        HashMap<String, String> namespace = new HashMap<String, String>();
        namespace.put("xmlns", XMLNS_URI);
        namespaces.add(namespace); 
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        HashMap<String, String> namespace = namespaces.get(0);
        // prefix is key, URI is value
        namespace.put(prefix, uri);
    }
    
    public void endPrefixMapping(String prefix) throws SAXException
    {
        HashMap<String, String> namespace = namespaces.get(0);
        // prefix is key, URI is value
        namespace.remove(prefix);
    }
    
    // Namespace Prefix Resolver implementation below
    
    /**
    * lookup the prefix for a URI e.g. TRANSFER_URI for xfer
    */
    public String getNamespaceURI(String prefix) throws NamespaceException
    { 
        for(HashMap<String, String> namespace : namespaces)
        {
            String uri = namespace.get(prefix);
            if(uri != null)
            {
                return uri;
            }
        }
        return null;
    }
    
    /**
     * @param namespaceURI
     * @return the prefix
     */
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        Collection<String> prefixes = new HashSet<String>();
        
        for(HashMap<String, String> namespace : namespaces)
        {
            for(Entry<String, String> entry : namespace.entrySet())
            {
                if (namespaceURI.equals(entry.getValue()))
                {
                    prefixes.add(entry.getKey());
                }           
            }
        }
 
        return prefixes;
    }

    public Collection<String> getPrefixes()
    {
        Collection<String> prefixes = new HashSet<String>();
        
        for(HashMap<String, String> namespace : namespaces)
        {
            prefixes.addAll(namespace.keySet());
        }            
        
        return prefixes;
    }

    public Collection<String> getURIs()
    {
        Collection<String> uris = new HashSet<String>();
        
        for(HashMap<String, String> namespace : namespaces)
        {
            uris.addAll(namespace.values());
        }            
        
        return uris;
    }  

     
    public void startDocument() throws SAXException
    {
        processor.startTransferRequsite();
    }
    
    public void endDocument() throws SAXException
    {
        processor.endTransferRequsite();
    } 
    
    /**
     * Start Element
     */
    public void startElement(String uri, String localName, String prefixName, Attributes atts)
    throws SAXException
    {
        QName elementQName = QName.resolveToQName(this, prefixName);
        
        HashMap<String, String> namespace = new HashMap<String, String>();
        namespaces.addFirst(namespace);
                
        /**
         * Look for any namespace attributes
         */
        for(int i = 0; i < atts.getLength(); i++)
        {
            QName attributeQName = QName.resolveToQName(this, atts.getQName(i));
            if(attributeQName.getNamespaceURI().equals(XMLNS_URI))
            {
                namespace.put(attributeQName.getLocalName(), atts.getValue(i));
            }
        }
        
        if(elementQName == null)
        {
            return;
        }
        
        if(elementQName.getNamespaceURI().equals(REQUSITE_URI));
        {
            // This is one of the transfer manifest elements
            String elementName = elementQName.getLocalName();
            
            // Simple and stupid parser for now
            if(elementName.equals(RequsiteModel.LOCALNAME_TRANSFER_REQUSITE))
            {
                // Good we got this
            }
            else if(elementName.equals(RequsiteModel.LOCALNAME_ELEMENT_CONTENT))
            {
                NodeRef nodeRef = new NodeRef(atts.getValue("", "nodeRef"));
                QName qname = QName.createQName(atts.getValue("", "qname"));
                String name = atts.getValue("", "name");
                
                processor.missingContent(nodeRef, qname, name);
            }
        } // if transfer URI       
    } // startElement
    
    /**
     * End Element
     */
    @SuppressWarnings("unchecked")
    public void endElement(String uri, String localName, String prefixName) throws SAXException
    {
        namespaces.removeFirst();
    
    } // end element



    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
      //NO-OP
    }

    public void processingInstruction(String target, String data) throws SAXException
    {
      //NO-OP
    }

    public void setDocumentLocator(Locator locator)
    {
      //NO-OP
    }

    public void skippedEntity(String name) throws SAXException
    {
      //NO-OP
    }

 }
