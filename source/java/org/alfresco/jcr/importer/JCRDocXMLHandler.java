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
package org.alfresco.jcr.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import javax.jcr.InvalidSerializedDataException;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.item.property.JCRMixinTypesProperty;
import org.alfresco.jcr.item.property.JCRPrimaryTypeProperty;
import org.alfresco.jcr.item.property.JCRUUIDProperty;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.repo.importer.ImportContentHandler;
import org.alfresco.repo.importer.Importer;
import org.alfresco.repo.importer.view.ElementContext;
import org.alfresco.repo.importer.view.NodeContext;
import org.alfresco.repo.importer.view.ParentContext;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Base64;
import org.alfresco.util.ISO9075;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Alfresco implementation of a Doc View Import Content Handler
 * 
 * @author David Caruana
 */
public class JCRDocXMLHandler implements ImportContentHandler
{
    private Importer importer;
    private SessionImpl session;
    private DictionaryService dictionaryService;
    private NamespacePrefixResolver importResolver;
    private Stack<ElementContext> contextStack = new Stack<ElementContext>();
    

    /**
     * Construct
     * 
     * @param session  JCR Session
     * @param importResolver  Namespace Resolver for the Import
     */
    public JCRDocXMLHandler(SessionImpl session, NamespacePrefixResolver importResolver)
    {
        this.session = session;
        this.importResolver = importResolver;
        this.dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
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
        File contentFile = new File(content);
        try
        {
            FileInputStream contentStream = new FileInputStream(contentFile);
            return new Base64.InputStream(contentStream, Base64.DECODE | Base64.DONT_BREAK_LINES);
        }
        catch (FileNotFoundException e)
        {
            throw new ImporterException("Failed to retrieve import input stream on temporary content file " + content);
        }
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
        // NOOP
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException
    {
        // NOOP
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        // NOOP
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException
    {
        // NOOP
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        try
        {
            // construct qname for element
            QName elementName = decodeQName(QName.createQName(qName, importResolver));

            // setup parent context            
            ParentContext parentContext = null;
            if (contextStack.empty())
            {
                // create root parent context
                parentContext = new ParentContext(elementName, dictionaryService, importer);
            }
            else
            {
                // create parent context
                NodeContext parentNode = (NodeContext)contextStack.peek();
                parentContext = new ParentContext(elementName, parentNode);
            }

            // create node context
            NodeContext node = new NodeContext(elementName, parentContext, null);
            node.setChildName(elementName.toPrefixString(importResolver));
            contextStack.push(node);
            
            // process node properties
            for (int i = 0; i < atts.getLength(); i++)
            {
                QName propName = decodeQName(QName.createQName(atts.getURI(i), atts.getLocalName(i)));
                String value = atts.getValue(i);
                
                //
                // process "well-known" properties
                //
                
                if (propName.equals(JCRPrimaryTypeProperty.PROPERTY_NAME))
                {
                    // primary type
                    QName primaryTypeQName = QName.createQName(value, importResolver);
                    TypeDefinition typeDef = dictionaryService.getType(primaryTypeQName);
                    if (typeDef == null)
                    {
                        throw new InvalidTypeException(primaryTypeQName);
                    }
                    node.setTypeDefinition(typeDef);
                }
                else if (propName.equals(JCRMixinTypesProperty.PROPERTY_NAME))
                {
                    // aspects
                    String[] aspects = value.split(" ");
                    for (String aspect : aspects)
                    {
                        // ignore JCR specific aspects
                        QName aspectQName = QName.createQName(aspect, importResolver);
                        if (!(JCRNamespace.JCR_URI.equals(aspectQName.getNamespaceURI()) ||
                              JCRNamespace.MIX_URI.equals(aspectQName.getNamespaceURI())))
                        {
                            AspectDefinition aspectDef = dictionaryService.getAspect(aspectQName);
                            if (aspectDef == null)
                            {
                                throw new InvalidTypeException(aspectQName);
                            }
                            node.addAspect(aspectDef);
                        }
                    }
                }
                else if (JCRUUIDProperty.PROPERTY_NAME.equals(propName))
                {
                    node.setUUID(value);
                }   

                //
                // Note: ignore JCR specific properties
                //
                
                else if (JCRNamespace.JCR_URI.equals(propName.getNamespaceURI()))
                {
                }
                
                //
                // process all other properties
                //
                
                else
                {
                    // determine type of property
                    PropertyDefinition propDef = dictionaryService.getProperty(propName);
                    if (propDef == null)
                    {
                        throw new ImporterException("Property " + propName + " is not known to the repository data dictionary");
                    }
                    DataTypeDefinition dataTypeDef = propDef.getDataType();

                    // extract values from node xml attribute
                    String[] propValues = null;
                    PropertyContext propertyContext = new PropertyContext(elementName, node, propName, dataTypeDef.getName());
                    if (dataTypeDef.getName().equals(DataTypeDefinition.CONTENT))
                    {
                        // Note: we only support single valued content properties
                        propValues = new String[] { value };
                    }
                    else
                    {
                        // attempt to split multi-value properties
                        propValues = value.split(" ");
                    }
                    
                    // extract values appropriately
                    for (String propValue : propValues)
                    {
                        propertyContext.startValue();
                        propertyContext.appendCharacters(propValue.toCharArray(), 0, propValue.length());
                        propertyContext.endValue();
                    }
                    
                    // add each value to the node
                    if (propertyContext.isMultiValue())
                    {
                        node.addPropertyCollection(propName);
                    }
                    List<StringBuffer> nodeValues = propertyContext.getValues();
                    for (StringBuffer nodeValue : nodeValues)
                    {
                        // first, cast value to appropriate type (using JCR converters)
                        Serializable objVal = (Serializable)session.getTypeConverter().convert(dataTypeDef, nodeValue.toString());
                        String strValue = DefaultTypeConverter.INSTANCE.convert(String.class, objVal);
                        node.addProperty(propName, strValue);
                    }
                }
            }

            // import node
            NodeRef nodeRef = node.getImporter().importNode(node);
            node.setNodeRef(nodeRef);
        }
        catch(Exception e)
        {
            throw new SAXException("Failed to process element " + qName, e);
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        try
        {
            // ensure context matches parse
            ElementContext context = (ElementContext)contextStack.pop();
            QName elementName = QName.createQName(qName, importResolver);
            if (!context.getElementName().equals(elementName))
            {
                throw new InvalidSerializedDataException("Expected element " + context.getElementName() + " but was " + elementName);
            }

            // signal end of node
            NodeContext nodeContext = (NodeContext)context;
            nodeContext.getImporter().childrenImported(nodeContext.getNodeRef());
        }
        catch(Exception e)
        {
            throw new SAXException("Failed to process element " + qName, e);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
    }

    /**
     * Decode QName
     * 
     * @param name  name to decode
     * @return  the decoded name
     */
    private QName decodeQName(QName name)
    {
        return QName.createQName(name.getNamespaceURI(), ISO9075.decode(name.getLocalName()));
    }

}
