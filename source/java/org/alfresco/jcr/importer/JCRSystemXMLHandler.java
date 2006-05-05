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
package org.alfresco.jcr.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.alfresco.jcr.dictionary.DataTypeMap;
import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.exporter.JCRSystemXMLExporter;
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
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Alfresco implementation of a System View Import Content Handler
 * 
 * @author David Caruana
 */
public class JCRSystemXMLHandler implements ImportContentHandler
{
    private Importer importer;
    private SessionImpl session;
    private NamespacePrefixResolver importResolver;
    private Stack<ElementContext> contextStack = new Stack<ElementContext>();
    

    /**
     * Construct
     * 
     * @param session  JCR Session
     * @param importResolver  Namespace Resolver for the Import
     */
    public JCRSystemXMLHandler(SessionImpl session, NamespacePrefixResolver importResolver)
    {
        this.session = session;
        this.importResolver = importResolver;
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
            QName elementName = QName.createQName(qName, importResolver);
    
            if (JCRSystemXMLExporter.NODE_QNAME.equals(elementName))
            {
                processStartNode(elementName, atts);
            }
            else if (JCRSystemXMLExporter.PROPERTY_QNAME.equals(elementName))
            {
                processStartProperty(elementName, atts);
            }
            else if (JCRSystemXMLExporter.VALUE_QNAME.equals(elementName))
            {
                processStartValue(elementName, atts);
            }
        }
        catch(Exception e)
        {
            throw new SAXException("Failed to process element " + qName, e);
        }
    }
    
    /**
     * Process start of Node Element
     *  
     * @param elementName
     * @param atts
     * @throws InvalidSerializedDataException
     */
    private void processStartNode(QName elementName, Attributes atts)
        throws InvalidSerializedDataException
    {
        ParentContext parentContext = null;
        if (contextStack.empty())
        {
            // create root parent context
            parentContext = new ParentContext(elementName, session.getRepositoryImpl().getServiceRegistry().getDictionaryService(), importer);
        }
        else
        {
            NodeContext parentNode = (NodeContext)contextStack.peek();
            
            // if we haven't yet imported the node before its children, do so now
            if (parentNode.getNodeRef() == null)
            {
                NodeRef nodeRef = importer.importNode(parentNode);
                parentNode.setNodeRef(nodeRef);
            }

            // create parent context
            parentContext = new ParentContext(elementName, parentNode);
        }
        
        // create node context
        // NOTE: we don't yet know its type (we have to wait for a property)
        NodeContext nodeContext = new NodeContext(elementName, parentContext, null);

        // establish node child name
        String name = atts.getValue(JCRSystemXMLExporter.NAME_QNAME.toPrefixString(importResolver));
        if (name == null)
        {
            throw new InvalidSerializedDataException("Mandatory sv:name attribute of element sv:node not present.");
        }
        nodeContext.setChildName(name);

        // record new node
        contextStack.push(nodeContext);
    }

    /**
     * Process start of Property element
     * 
     * @param elementName
     * @param atts
     * @throws InvalidSerializedDataException
     */
    private void processStartProperty(QName elementName, Attributes atts)
        throws InvalidSerializedDataException
    {
        // establish correct context
        ElementContext context = contextStack.peek();
        if (!(context instanceof NodeContext))
        {
            throw new InvalidSerializedDataException("Element " + elementName + " not expected.");
        }
        NodeContext parentNode = (NodeContext)context;
    
        // establish property name
        String name = atts.getValue(JCRSystemXMLExporter.NAME_QNAME.toPrefixString(importResolver));
        if (name == null)
        {
            throw new InvalidSerializedDataException("Mandatory sv:name attribute of element sv:node not present.");
        }
        QName propertyName = QName.createQName(name, importResolver);
        
        // establish property type and validate property type
        QName dataType = null; 
        String type = atts.getValue(JCRSystemXMLExporter.TYPE_QNAME.toPrefixString(importResolver));
        if (type == null)
        {
            throw new InvalidSerializedDataException("Mandatory sv:type attribute of element sv:node not present.");
        }
        try
        {
            dataType = DataTypeMap.convertPropertyTypeToDataType(PropertyType.valueFromName(type));
        }
        catch(IllegalArgumentException e)
        {
            throw new ImporterException("Type " + type + " is not known for property " + name);
        }
        
        // construct property context        
        PropertyContext propertyContext = new PropertyContext(elementName, parentNode, propertyName, dataType);
        contextStack.push(propertyContext);
    }

    /**
     * Process start of Value element
     * 
     * @param elementName
     * @param atts
     * @throws InvalidSerializedDataException
     */
    private void processStartValue(QName elementName, Attributes atts)
        throws InvalidSerializedDataException
    {
        // establish correct context
        ElementContext context = contextStack.peek();
        if (!(context instanceof PropertyContext))
        {
            throw new InvalidSerializedDataException("Element " + elementName + " not expected.");
        }
        PropertyContext property = (PropertyContext)context;
        property.startValue();
        
        ValueContext value = new ValueContext(elementName, property);
        contextStack.push(value);
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
            ElementContext context = (ElementContext)contextStack.peek();
            QName elementName = QName.createQName(qName, importResolver);
            if (!context.getElementName().equals(elementName))
            {
                throw new InvalidSerializedDataException("Expected element " + context.getElementName() + " but was " + elementName);
            }
    
            if (JCRSystemXMLExporter.NODE_QNAME.equals(elementName))
            {
                processEndNode((NodeContext)context);
            }
            else if (JCRSystemXMLExporter.PROPERTY_QNAME.equals(elementName))
            {
                processEndProperty((PropertyContext)context);
            }
            else if (JCRSystemXMLExporter.VALUE_QNAME.equals(elementName))
            {
                processEndValue((ValueContext)context);
            }
    
            // cleanup
            contextStack.pop();
        }
        catch(Exception e)
        {
            throw new SAXException("Failed to process element " + qName, e);
        }
    }

    /**
     * Process end of Node element
     *
     * @param node
     */
    private void processEndNode(NodeContext node)
    {
        // import node, if not already imported (this will be the case when no child nodes exist)
        NodeRef nodeRef = node.getNodeRef();
        if (nodeRef == null)
        {
            nodeRef = node.getImporter().importNode(node);
            node.setNodeRef(nodeRef);
        }
        
        // signal end of node
        node.getImporter().childrenImported(nodeRef);
    }
    
    /**
     * Process end of Property element
     * 
     * @param context
     * @throws InvalidSerializedDataException
     * @throws RepositoryException
     */
    private void processEndProperty(PropertyContext context)
        throws InvalidSerializedDataException, RepositoryException
    {
        QName propertyName = context.getName();

        // ensure a value has been provided
        if (context.isNull())
        {
            throw new InvalidSerializedDataException("Property " + propertyName + " must have a value");
        }
        
        //
        // process known properties
        //
        
        if (JCRPrimaryTypeProperty.PROPERTY_NAME.equals(propertyName))
        {
            // apply type definition
            if (!context.isNull())
            {
                QName typeQName = QName.createQName(context.getValues().get(0).toString(), importResolver);
                TypeDefinition typeDef = context.getDictionaryService().getType(typeQName);
                if (typeDef == null)
                {
                    throw new InvalidTypeException(typeQName);
                }
                
                // update node context
                context.getNode().setTypeDefinition(typeDef);
            }
        }
        else if (JCRMixinTypesProperty.PROPERTY_NAME.equals(propertyName))
        {
            // apply aspect definitions
            List<StringBuffer> values = context.getValues();
            for (StringBuffer value : values)
            {
                QName aspectQName = QName.createQName(value.toString(), importResolver);
                
                // ignore JCR specific aspects
                if (!(JCRNamespace.JCR_URI.equals(aspectQName.getNamespaceURI()) ||
                      JCRNamespace.MIX_URI.equals(aspectQName.getNamespaceURI())))
                {
                    AspectDefinition aspectDef = context.getDictionaryService().getAspect(aspectQName);
                    if (aspectDef == null)
                    {
                        throw new InvalidTypeException(aspectQName);
                    }
                    context.getNode().addAspect(aspectDef);
                }
            }
        }
        else if (JCRUUIDProperty.PROPERTY_NAME.equals(propertyName))
        {
            StringBuffer value = context.getValues().get(0);
            context.getNode().setUUID(value.toString());
        }   

        //
        // Note: ignore all other JCR specific properties
        //
        
        else if (JCRNamespace.JCR_URI.equals(propertyName.getNamespaceURI()))
        {
        }
        
        //
        // process all other properties
        //
        
        else
        {
            // apply the property values to the node
            NodeContext node = context.getNode();
            if (node.getTypeDefinition() == null)
            {
                throw new InvalidSerializedDataException("Node jcr:primaryType property has not been specified.");
            }
            
            // determine data type of value
            QName dataType = context.getType();
            DataTypeDefinition dataTypeDef = context.getDictionaryService().getDataType(dataType);
            if (dataTypeDef == null)
            {
                throw new InvalidTypeException(dataType);
            }
            node.addDatatype(propertyName, dataTypeDef);

            // determine if multi-value property
            if (context.isMultiValue())
            {
                node.addPropertyCollection(propertyName);
            }
            
            // add each value to the node
            List<StringBuffer> values = context.getValues();
            for (StringBuffer value : values)
            {
                // first, cast value to appropriate type (using JCR converters)
                Serializable objVal = (Serializable)session.getTypeConverter().convert(dataTypeDef, value.toString());
                String strValue = DefaultTypeConverter.INSTANCE.convert(String.class, objVal);
                node.addProperty(propertyName, strValue);
            }
        }
    }

    /**
     * Process end of value element
     * 
     * @param context
     */    
    private void processEndValue(ValueContext context)
    {
        PropertyContext property = context.getProperty();
        property.endValue();
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        ElementContext context = (ElementContext)contextStack.peek();
        if (context instanceof ValueContext)
        {
            PropertyContext property = ((ValueContext)context).getProperty();
            property.appendCharacters(ch, start, length);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        ElementContext context = (ElementContext)contextStack.peek();
        if (context instanceof ValueContext)
        {
            PropertyContext property = ((ValueContext)context).getProperty();
            property.appendCharacters(ch, start, length);
        }
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

}
