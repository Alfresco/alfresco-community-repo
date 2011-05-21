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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.jcr.item.property.JCRMixinTypesProperty;
import org.alfresco.jcr.item.property.JCRPrimaryTypeProperty;
import org.alfresco.jcr.item.property.JCRUUIDProperty;
import org.alfresco.jcr.session.SessionImpl;
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
import org.alfresco.util.ISO9075;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Alfresco Implementation of JCR Document XML Exporter
 * 
 * @author David Caruana
 */
public class JCRDocumentXMLExporter implements Exporter
{

    private SessionImpl session;
    private ContentHandler contentHandler;
    private List<QName> currentProperties = new ArrayList<QName>();
    private List<Object> currentValues = new ArrayList<Object>();
    

    /**
     * Construct
     * 
     * @param namespaceService  namespace service
     * @param nodeService  node service
     * @param contentHandler  content handler
     */
    public JCRDocumentXMLExporter(SessionImpl session, ContentHandler contentHandler)
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
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endNode(NodeRef nodeRef)
    {
        try
        {
            QName nodeName = getNodeName(nodeRef);
            contentHandler.endElement(nodeName.getNamespaceURI(), nodeName.getLocalName(), toPrefixString(nodeName));
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
        currentProperties.clear();
        currentValues.clear();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endProperties(NodeRef nodeRef)
    {
        try
        {
            // create node attributes
            AttributesImpl attrs = new AttributesImpl(); 
            
            // primary type
            NodeImpl nodeImpl = new NodeImpl(session, nodeRef);
            PropertyImpl primaryType = new JCRPrimaryTypeProperty(nodeImpl);
            attrs.addAttribute(JCRPrimaryTypeProperty.PROPERTY_NAME.getNamespaceURI(), JCRPrimaryTypeProperty.PROPERTY_NAME.getLocalName(), 
                toPrefixString(JCRPrimaryTypeProperty.PROPERTY_NAME), null, getValue(primaryType.getValue().getString()));
            
            // mixin type
            PropertyImpl mixinTypes = new JCRMixinTypesProperty(nodeImpl);
            Collection<String> mixins = new ArrayList<String>();
            for (Value value : mixinTypes.getValues())
            {
                mixins.add(value.getString());
            }
            attrs.addAttribute(JCRMixinTypesProperty.PROPERTY_NAME.getNamespaceURI(), JCRMixinTypesProperty.PROPERTY_NAME.getLocalName(), 
                toPrefixString(JCRMixinTypesProperty.PROPERTY_NAME), null, getCollectionValue(mixins));

            // uuid (for mix:referencable)
            attrs.addAttribute(JCRUUIDProperty.PROPERTY_NAME.getNamespaceURI(), JCRUUIDProperty.PROPERTY_NAME.getLocalName(), 
                toPrefixString(JCRUUIDProperty.PROPERTY_NAME), null, getValue(nodeRef.getId()));
            
            // node properties
            for (int i = 0; i < currentProperties.size(); i++)
            {
                Object value = currentValues.get(i);
                String strValue = (value instanceof Collection) ? getCollectionValue((Collection)value) : getValue(value);
                QName propName = currentProperties.get(i);
                propName = encodeQName(propName);
                attrs.addAttribute(propName.getNamespaceURI(), propName.getLocalName(), toPrefixString(propName), null, strValue);
            }
            
            // emit node element
            QName nodeName = getNodeName(nodeRef);
            contentHandler.startElement(nodeName.getNamespaceURI(), nodeName.getLocalName(), toPrefixString(nodeName), attrs);
        }
        catch (ValueFormatException e)
        {
            throw new ExporterException("Failed to process properties event - nodeRef " + nodeRef);
        }
        catch (RepositoryException e)
        {
            throw new ExporterException("Failed to process properties event - nodeRef " + nodeRef);
        }
        catch (SAXException e)
        {
            throw new ExporterException("Failed to process properties event - nodeRef " + nodeRef);
        }
    }    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startProperty(NodeRef nodeRef, QName property)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endProperty(NodeRef nodeRef, QName property)
    {
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
        if (value != null)
        {
            currentProperties.add(property);
            currentValues.add(value);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream)
     */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
        try
        {
            StringBuffer strValue = new StringBuffer(9 * 1024);
            if (content != null)
            {
                // emit base64 encoded content
                InputStream base64content = new Base64.InputStream(content, Base64.ENCODE | Base64.DONT_BREAK_LINES);
                byte[] buffer = new byte[9 * 1024];
                int read;
                while ((read = base64content.read(buffer, 0, buffer.length)) > 0)
                {
                    String characters = new String(buffer, 0, read);
                    strValue.append(characters);
                }
            }
            currentProperties.add(property);
            currentValues.add(strValue.toString());
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
    
    /**
     * Get name of Node
     * 
     * @param nodeRef  node reference
     * @return  node name
     */
    private QName getNodeName(NodeRef nodeRef)
    {
        // establish name of node
        QName childQName = null;
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        NodeRef rootNode = nodeService.getRootNode(nodeRef.getStoreRef());
        if (rootNode.equals(nodeRef))
        {
            childQName = QName.createQName(JCRNamespace.JCR_URI, "root");
        }
        else
        {
            Path path = nodeService.getPath(nodeRef);
            String childName = path.last().getElementString();
            childQName = QName.createQName(childName);
            childQName = encodeQName(childQName);
        }
        
        return childQName;
    }
    
    /**
     * Get single-valued property
     * 
     * @param value
     * @return
     */
    private String getValue(Object value)
        throws RepositoryException
    {
        String strValue = session.getTypeConverter().convert(String.class, value);
        return encodeBlanks(strValue);
    }
    
    
    /**
     * Get multi-valued property
     * 
     * @param values
     * @return
     */
    private String getCollectionValue(Collection values)
    {
        Collection<String> strValues = session.getTypeConverter().getConverter().convert(String.class, values);
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (String strValue : strValues)
        {
            buffer.append(encodeBlanks(strValue));
            i++;
            if (i < strValues.size())
            {
                buffer.append(" ");
            }
        }
        return buffer.toString();
    }

    /**
     * Encode Name for Document View Output
     * 
     * @param name  name to encode
     * @return  encoded name
     */
    private QName encodeQName(QName name)
    {
        return QName.createQName(name.getNamespaceURI(), ISO9075.encode(name.getLocalName()));
    }

    /**
     * Encode blanks in value
     * 
     * @param value
     * @return
     */
    private String encodeBlanks(String value)
    {
        return value.replaceAll(" ", "_x0020_");    
    }


}
