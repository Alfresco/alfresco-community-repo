/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.jcr.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.dictionary.PropertyDefinitionImpl;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.jcr.item.property.JCRMixinTypesProperty;
import org.alfresco.jcr.item.property.JCRPrimaryTypeProperty;
import org.alfresco.jcr.item.property.JCRUUIDProperty;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.Base64;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Alfresco Implementation of JCR System XML Exporter
 * 
 * @author David Caruana
 */
public class JCRSystemXMLExporter implements Exporter
{
    public final static String NODE_LOCALNAME = "node";
    public final static String NAME_LOCALNAME = "name";
    public final static String PROPERTY_LOCALNAME = "property";
    public final static String TYPE_LOCALNAME = "type";
    public final static String VALUE_LOCALNAME = "value";
    
    public final static QName NODE_QNAME = QName.createQName(JCRNamespace.SV_URI, NODE_LOCALNAME);
    public final static QName NAME_QNAME = QName.createQName(JCRNamespace.SV_URI, NAME_LOCALNAME);
    public final static QName PROPERTY_QNAME = QName.createQName(JCRNamespace.SV_URI, PROPERTY_LOCALNAME);
    public final static QName TYPE_QNAME = QName.createQName(JCRNamespace.SV_URI, TYPE_LOCALNAME);
    public final static QName VALUE_QNAME = QName.createQName(JCRNamespace.SV_URI, VALUE_LOCALNAME);
    
    private SessionImpl session;
    private ContentHandler contentHandler;
    
    private final static AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
    

    /**
     * Construct
     * 
     * @param namespaceService  namespace service
     * @param nodeService  node service
     * @param contentHandler  content handler
     */
    public JCRSystemXMLExporter(SessionImpl session, ContentHandler contentHandler)
    {
        this.session = session;
        this.contentHandler = contentHandler;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#start()
     */
    public void start(ExporterContext exportNodeRef)
    {
        try
        {
            contentHandler.startDocument();
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process export start event", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startNamespace(java.lang.String, java.lang.String)
     */
    public void startNamespace(String prefix, String uri)
    {
        try
        {
            contentHandler.startPrefixMapping(prefix, uri);
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process start namespace event - prefix " + prefix + " uri " + uri, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNamespace(java.lang.String)
     */
    public void endNamespace(String prefix)
    {
        try
        {
            contentHandler.endPrefixMapping(prefix);
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process end namespace event - prefix " + prefix, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startNode(NodeRef nodeRef)
    {
        try
        {
            // establish name of node
            String childName;
            NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
            NodeRef rootNode = nodeService.getRootNode(nodeRef.getStoreRef());
            if (rootNode.equals(nodeRef))
            {
                childName = "jcr:root";
            }
            else
            {
                Path path = nodeService.getPath(nodeRef);
                childName = path.last().getElementString();
            }
            QName childQName = QName.createQName(childName);

            // create jcr node attributes
            AttributesImpl attrs = new AttributesImpl(); 
            attrs.addAttribute(NAME_QNAME.getNamespaceURI(), NAME_LOCALNAME, toPrefixString(NAME_QNAME), null, toPrefixString(childQName));
            
            // emit node element
            contentHandler.startElement(NODE_QNAME.getNamespaceURI(), NODE_LOCALNAME, toPrefixString(NODE_QNAME), attrs);

            //
            // emit jcr specifics
            //
            NodeImpl nodeImpl = new NodeImpl(session, nodeRef);
            
            // primary type
            PropertyImpl primaryType = new JCRPrimaryTypeProperty(nodeImpl);
            startProperty(nodeRef, JCRPrimaryTypeProperty.PROPERTY_NAME);
            value(nodeRef, JCRPrimaryTypeProperty.PROPERTY_NAME, primaryType.getValue().getString(), -1);
            endProperty(nodeRef, JCRPrimaryTypeProperty.PROPERTY_NAME);
            
            // mixin type
            PropertyImpl mixinTypes = new JCRMixinTypesProperty(nodeImpl);
            startProperty(nodeRef, JCRMixinTypesProperty.PROPERTY_NAME);
            Value[] mixinValues = mixinTypes.getValues();
            for (int i = 0; i < mixinValues.length; i++)
            {
                value(nodeRef, JCRMixinTypesProperty.PROPERTY_NAME, mixinValues[i].getString(), i);
            }
            endProperty(nodeRef, JCRMixinTypesProperty.PROPERTY_NAME);
            
            // uuid (for mix:referencable)
            startProperty(nodeRef, JCRUUIDProperty.PROPERTY_NAME);
            value(nodeRef, JCRUUIDProperty.PROPERTY_NAME, nodeRef.getId(), -1);
            endProperty(nodeRef, JCRUUIDProperty.PROPERTY_NAME);
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process start node event - node ref " + nodeRef.toString(), e);
        }
        catch (RepositoryException e)
        {
            throw new ExporterException("Failed to process start node event - node ref " + nodeRef.toString(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endNode(NodeRef nodeRef)
    {
        try
        {
            contentHandler.endElement(NODE_QNAME.getNamespaceURI(), NODE_LOCALNAME, toPrefixString(NODE_QNAME));
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process end node event - node ref " + nodeRef.toString(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startAspects(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endAspects(NodeRef nodeRef)
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startAspect(NodeRef nodeRef, QName aspect)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endAspect(NodeRef nodeRef, QName aspect)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startACL(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startACL(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#permission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission)
     */
    public void permission(NodeRef nodeRef, AccessPermission permission)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endACL(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endACL(NodeRef nodeRef)
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startProperties(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endProperties(NodeRef nodeRef)
    {
    }    

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startProperty(NodeRef nodeRef, QName property)
    {
        try
        {
            // create jcr node attributes
            DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
            PropertyDefinition propDef = dictionaryService.getProperty(property);
            PropertyDefinitionImpl propDefImpl = new PropertyDefinitionImpl(session.getTypeManager(), propDef);
            String datatype = PropertyType.nameFromValue(propDefImpl.getRequiredType());
            AttributesImpl attrs = new AttributesImpl(); 
            attrs.addAttribute(NAME_QNAME.getNamespaceURI(), NAME_LOCALNAME, toPrefixString(NAME_QNAME), null, toPrefixString(property));
            attrs.addAttribute(TYPE_QNAME.getNamespaceURI(), TYPE_LOCALNAME, toPrefixString(TYPE_QNAME), null, datatype);
            
            // emit property element
            contentHandler.startElement(PROPERTY_QNAME.getNamespaceURI(), PROPERTY_LOCALNAME, toPrefixString(PROPERTY_QNAME), attrs);
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process start property event - nodeRef " + nodeRef + "; property " + toPrefixString(property), e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endProperty(NodeRef nodeRef, QName property)
    {
        try
        {
            // emit property element
            contentHandler.endElement(PROPERTY_QNAME.getNamespaceURI(), PROPERTY_LOCALNAME, toPrefixString(PROPERTY_QNAME));
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process end property event - nodeRef " + nodeRef + "; property " + toPrefixString(property), e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startValueCollection(NodeRef nodeRef, QName property)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endValueCollection(NodeRef nodeRef, QName property)
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    public void value(NodeRef nodeRef, QName property, Object value, int index)
    {
        try
        {
            if (value != null)
            {
                // emit value element
                contentHandler.startElement(VALUE_QNAME.getNamespaceURI(), VALUE_LOCALNAME, toPrefixString(VALUE_QNAME), EMPTY_ATTRIBUTES);
                String strValue = session.getTypeConverter().convert(String.class, value);
                contentHandler.characters(strValue.toCharArray(), 0, strValue.length());
                contentHandler.endElement(VALUE_QNAME.getNamespaceURI(), VALUE_LOCALNAME, toPrefixString(VALUE_QNAME));
            }
        }
        catch (RepositoryException e)
        {
            throw new ExporterException("Failed to process value event - nodeRef " + nodeRef + "; property " + toPrefixString(property) + "; value " + value, e);
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process value event - nodeRef " + nodeRef + "; property " + toPrefixString(property) + "; value " + value, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream)
     */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
        try
        {
            contentHandler.startElement(VALUE_QNAME.getNamespaceURI(), VALUE_LOCALNAME, toPrefixString(VALUE_QNAME), EMPTY_ATTRIBUTES);
            
            if (content != null)
            {
                // emit base64 encoded content
                InputStream base64content = new Base64.InputStream(content, Base64.ENCODE | Base64.DONT_BREAK_LINES);
                byte[] buffer = new byte[9 * 1024];
                int read;
                while ((read = base64content.read(buffer, 0, buffer.length)) > 0)
                {
                    String characters = new String(buffer, 0, read);
                    contentHandler.characters(characters.toCharArray(), 0, characters.length());
                }
            }
            
            contentHandler.endElement(VALUE_QNAME.getNamespaceURI(), VALUE_LOCALNAME, toPrefixString(VALUE_QNAME));
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process content event - nodeRef " + nodeRef + "; property " + toPrefixString(property));
        }
        catch (IOException e)
        {
            throw new ExporterException("Failed to process content event - nodeRef " + nodeRef + "; property " + toPrefixString(property));
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startAssoc(NodeRef nodeRef, QName assoc)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endAssoc(NodeRef nodeRef, QName assoc)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAssocs(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startAssocs(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAssocs(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endAssocs(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startReference(NodeRef nodeRef, QName childName)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endReference(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endReference(NodeRef nodeRef)
    {
    }

    public void endValueMLText(NodeRef nodeRef)
    {
    }

    public void startValueMLText(NodeRef nodeRef, Locale locale)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#warning(java.lang.String)
     */
    public void warning(String warning)
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#end()
     */
    public void end()
    {
        try
        {
            contentHandler.endDocument();
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process end export event", e);
        }
    }

    /**
     * Get the prefix for the specified URI
     * @param uri  the URI
     * @return  the prefix (or null, if one is not registered)
     */
    private String toPrefixString(QName qname)
    {
        return qname.toPrefixString(session.getNamespaceResolver());
    }

}
