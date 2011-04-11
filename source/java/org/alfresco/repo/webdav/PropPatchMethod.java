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
package org.alfresco.repo.webdav;

import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

/**
 * Implements the WebDAV PROPPATCH method
 * 
 * @author Ivan Rybnikov
 */
public class PropPatchMethod extends PropFindMethod
{

    // Properties to patch
    protected ArrayList<PropertyAction> m_propertyActions = null;
    
    /**
     * @return          Returns <tt>false</tt> always
     */
    @Override
    protected boolean isReadOnly()
    {
        return false;
    }
    
    @Override
    protected void executeImpl() throws WebDAVServerException, Exception
    {

        m_response.setStatus(WebDAV.WEBDAV_SC_MULTI_STATUS);

        FileInfo pathNodeInfo = null;
        try
        {
            // Check that the path exists
            pathNodeInfo = getNodeForPath(getRootNodeRef(), m_strPath, m_request.getServletPath());
        }
        catch (FileNotFoundException e)
        {
            // The path is not valid - send a 404 error back to the client
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }

        checkNode(pathNodeInfo);

        // Set the response content type
        m_response.setContentType(WebDAV.XML_CONTENT_TYPE);

        // Create multistatus response
        XMLWriter xml = createXMLWriter();

        xml.startDocument();

        String nsdec = generateNamespaceDeclarations(m_namespaces);
        xml.startElement(
                WebDAV.DAV_NS,
                WebDAV.XML_MULTI_STATUS + nsdec,
                WebDAV.XML_NS_MULTI_STATUS + nsdec,
                getDAVHelper().getNullAttributes());

        // Create the path for the current location in the tree
        StringBuilder baseBuild = new StringBuilder(256);
        baseBuild.append(getPath());
        if (baseBuild.length() == 0 || baseBuild.charAt(baseBuild.length() - 1) != WebDAVHelper.PathSeperatorChar)
        {
            baseBuild.append(WebDAVHelper.PathSeperatorChar);
        }
        String basePath = baseBuild.toString();

        // Output the response for the root node, depth zero
        generateResponse(xml, pathNodeInfo, basePath);

        // Close the outer XML element
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_MULTI_STATUS, WebDAV.XML_NS_MULTI_STATUS);

        // Send remaining data
        flushXML(xml);
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    @Override
    protected void parseRequestBody() throws WebDAVServerException
    {
        Document body = getRequestBodyAsDocument();
        if (body != null)
        {
            Element rootElement = body.getDocumentElement();
            NodeList childList = rootElement.getChildNodes();

            m_propertyActions = new ArrayList<PropertyAction>();

            for (int i = 0; i < childList.getLength(); i++)
            {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType())
                {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    if (currentNode.getNodeName().endsWith(WebDAV.XML_SET) || currentNode.getNodeName().endsWith(WebDAV.XML_REMOVE))
                    {
                        NodeList propertiesList = currentNode.getChildNodes();

                        for (int j = 0; j < propertiesList.getLength(); j++)
                        {
                            Node propertiesNode = propertiesList.item(j);
                            switch (propertiesNode.getNodeType())
                            {
                            case Node.TEXT_NODE:
                                break;
                            case Node.ELEMENT_NODE:
                                if (propertiesNode.getNodeName().endsWith(WebDAV.XML_PROP))
                                {
                                    NodeList propList = propertiesNode.getChildNodes();

                                    for (int k = 0; k < propList.getLength(); k++)
                                    {
                                        Node propNode = propList.item(k);
                                        switch (propNode.getNodeType())
                                        {
                                        case Node.TEXT_NODE:
                                            break;
                                        case Node.ELEMENT_NODE:
                                            int action = currentNode.getNodeName().endsWith(WebDAV.XML_SET) ? PropertyAction.SET : PropertyAction.REMOVE;
                                            m_propertyActions.add(new PropertyAction(action, createProperty(propNode)));
                                            break;
                                    }
                                }
                            }
                            break;
                            }
                        }
                    }
                    break;
                }
            }

        }
        
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    @Override
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Parse Lock tokens and ETags, if any
        parseIfHeader();
    }

    /**
     * Creates a WebDAVProperty from the given XML node
     */
    protected WebDAVProperty createProperty(Node node)
    {
        WebDAVProperty property = super.createProperty(node);
        
        String strValue = null;
        Node value = node.getFirstChild();
        if (value != null)
        {
            strValue = value.getNodeValue();
        }
        property.setValue(strValue);

        return property;
    }
    
    
    
    
    /**
     * Generates the required response XML
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param path String
     */
    protected void generateResponse(XMLWriter xml, FileInfo nodeInfo, String path) throws Exception
    {
        boolean isFolder = nodeInfo.isFolder();
        
        // Output the response block for the current node
        xml.startElement(
                WebDAV.DAV_NS,
                WebDAV.XML_RESPONSE,
                WebDAV.XML_NS_RESPONSE,
                getDAVHelper().getNullAttributes());

        // Build the href string for the current node
        String strHRef = getURLForPath(m_request, path, isFolder);

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_HREF, WebDAV.XML_NS_HREF, getDAVHelper().getNullAttributes());
        xml.write(strHRef);
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_HREF, WebDAV.XML_NS_HREF);

        
        boolean failed = false;
        WebDAVProperty failedProperty = null;
        for (PropertyAction action : m_propertyActions)
        {
            if (action.getProperty().isProtected())
            {
                generateError(xml);
                failed = true;
                failedProperty = action.getProperty();
                break;
            }
        }

        for (PropertyAction propertyAction : m_propertyActions)
        {
            int statusCode;
            String statusCodeDescription;
            WebDAVProperty property = propertyAction.getProperty();

            if (!failed)
            {
                if (PropertyAction.SET == propertyAction.getAction())
                {
                    getNodeService().setProperty(nodeInfo.getNodeRef(), property.createQName(), property.getValue());
                }
                else if (PropertyAction.REMOVE == propertyAction.getAction())
                {
                    getNodeService().removeProperty(nodeInfo.getNodeRef(), property.createQName());
                }
                else
                {
                    throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
                }
                statusCode = HttpServletResponse.SC_OK;
                statusCodeDescription = WebDAV.SC_OK_DESC;
            }
            else if (failedProperty == property)
            {
                statusCode = HttpServletResponse.SC_FORBIDDEN;
                statusCodeDescription = WebDAV.SC_FORBIDDEN_DESC;
            }
            else
            {
                statusCode = WebDAV.WEBDAV_SC_FAILED_DEPENDENCY;
                statusCodeDescription = WebDAV.WEBDAV_SC_FAILED_DEPENDENCY_DESC;
            }

            generatePropertyResponse(xml, property, statusCode, statusCodeDescription);
        }

        // Close off the response element
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_RESPONSE, WebDAV.XML_NS_RESPONSE);
    }

    
    
    /**
     * Generates the XML response for a PROPFIND request that asks for a list of
     * all known properties
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param isDir boolean
     */
    protected void generatePropertyResponse(XMLWriter xml, WebDAVProperty property, int status, String description)
    {
        try
        {
            // Output the start of the properties element
            Attributes nullAttr = getDAVHelper().getNullAttributes();

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT, nullAttr);

            // Output property name
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP, nullAttr);
            if (property.hasNamespaceName())
            {
                xml.write(DocumentHelper.createElement(property.getNamespaceName() + WebDAV.NAMESPACE_SEPARATOR + property.getName()));
            }
            else
            {
                xml.write(DocumentHelper.createElement(property.getName()));
            }
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP);

            // Output action result status
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS, nullAttr);
            xml.write(WebDAV.HTTP1_1 + " " + status + " " + description);
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS);

            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT);
        }
        catch (Exception ex)
        {
            // Convert to a runtime exception
            throw new AlfrescoRuntimeException("XML processing error", ex);
        }
    }


    /**
     * Generates the error tag
     * 
     * @param xml XMLWriter
     */
    protected void generateError(XMLWriter xml)
    {
        try
        {
            // Output the start of the error element
            Attributes nullAttr = getDAVHelper().getNullAttributes();

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_ERROR, WebDAV.XML_NS_ERROR, nullAttr);
            // Output error
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_CANNOT_MODIFY_PROTECTED_PROPERTY));

            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_ERROR, WebDAV.XML_NS_ERROR);
        }
        catch (Exception ex)
        {
            // Convert to a runtime exception
            throw new AlfrescoRuntimeException("XML processing error", ex);
        }
    }

    
    /**
     * Stores information about PROPPATCH action(set or remove) an according property.
     * 
     * @author Ivan Rybnikov
     */
    protected class PropertyAction
    {
        public static final int SET = 0;
        public static final int REMOVE = 1;
        
        // Property on which action should be performed
        private WebDAVProperty property;
        
        // Action
        private int action;
        
        
        /**
         * Constructor
         * 
         * @param action
         * @param property
         */
        public PropertyAction(int action, WebDAVProperty property)
        {
            this.action = action;
            this.property = property;
        }
        
        public int getAction()
        {
            return action;
        }

        public WebDAVProperty getProperty()
        {
            return property;
        }

        public String toString()
        {
            StringBuilder str = new StringBuilder();
            
            str.append("[");
            str.append("action=");
            str.append(getAction() == 0 ? "SET" : "REMOVE");
            str.append(",property=");
            str.append(getProperty());
            str.append("]");
            
            return str.toString();
        }
    }
    
    
}
