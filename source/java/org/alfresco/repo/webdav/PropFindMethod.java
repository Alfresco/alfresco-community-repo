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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.webdav.auth.AuthenticationFilter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Implements the WebDAV PROPFIND method
 * 
 * @author Gavin Cornwell
 */
public class PropFindMethod extends WebDAVMethod
{
    // Request types
	protected static final int GET_ALL_PROPS = 0;
	protected static final int GET_NAMED_PROPS = 1;
	protected static final int FIND_PROPS = 2;

    // Find request type
    protected int m_mode = GET_ALL_PROPS;

    // Requested properties
    protected ArrayList<WebDAVProperty> m_properties = null;

    // Available namespaces list
    protected HashMap<String, String> m_namespaces = null;

    /**
     * Default constructor
     */
    public PropFindMethod()
    {
        m_namespaces = new HashMap<String, String>();
    }

    /**
     * Return the find mode
     * 
     * @return int
     */
    public final int getMode()
    {
        return m_mode;
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Store the Depth header as this is used by several WebDAV methods

        parseDepthHeader();
        
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        Document body = getRequestBodyAsDocument();
        if (body != null)
        {
            Element rootElement = body.getDocumentElement();
            NodeList childList = rootElement.getChildNodes();
            Node node = null;

            for (int i = 0; i < childList.getLength(); i++)
            {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType())
                {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    if (currentNode.getNodeName().endsWith(WebDAV.XML_ALLPROP))
                    {
                        m_mode = GET_ALL_PROPS;
                    }
                    else if (currentNode.getNodeName().endsWith(WebDAV.XML_PROP))
                    {
                        m_mode = GET_NAMED_PROPS;
                        node = currentNode;
                    }
                    else if (currentNode.getNodeName().endsWith(WebDAV.XML_PROPNAME))
                    {
                        m_mode = FIND_PROPS;
                    }

                    break;
                }
            }

            if (m_mode == GET_NAMED_PROPS)
            {
                m_properties = new ArrayList<WebDAVProperty>();
                childList = node.getChildNodes();

                for (int i = 0; i < childList.getLength(); i++)
                {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType())
                    {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        m_properties.add(createProperty(currentNode));
                        break;
                    }
                }
            }
        }
    }

    /**
     * @return          Returns <tt>true</tt> always
     */
    @Override
    protected boolean isReadOnly()
    {
        return true;
    }
    
    /**
     * Execute the main WebDAV request processing
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        m_response.setStatus(WebDAV.WEBDAV_SC_MULTI_STATUS);

        FileFolderService fileFolderService = getFileFolderService();

        FileInfo pathNodeInfo = null;
        try
        {
            // Check that the path exists
            pathNodeInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), m_strPath, m_request.getServletPath());
        }
        catch (FileNotFoundException e)
        {
            // The path is not valid - send a 404 error back to the client
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }

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
        generateResponseForNode(xml, pathNodeInfo, basePath);

        // If additional levels are required and the root node is a folder then recurse to the required
        // level and output node details a level at a time
        if (getDepth() != WebDAV.DEPTH_0 && pathNodeInfo.isFolder())
        {
            // Create the initial list of nodes to report
            List<FileInfo> nodeInfos = new ArrayList<FileInfo>(10);
            nodeInfos.add(pathNodeInfo);

            int curDepth = WebDAV.DEPTH_1;

            // Save the base path length
            int baseLen = baseBuild.length();

            // List of next level of nodes to report
            List<FileInfo> nextNodeInfos = null;
            if (getDepth() > WebDAV.DEPTH_1)
            {
                nextNodeInfos = new ArrayList<FileInfo>(10);
            }

            // Loop reporting each level of nodes to the requested depth
            while (curDepth <= getDepth() && nodeInfos != null)
            {
                // Clear out the next level of nodes, if required
                if (nextNodeInfos != null)
                {
                    nextNodeInfos.clear();
                }

                // Output the current level of node(s), the node list should
                // only contain folder nodes

                for (FileInfo curNodeInfo : nodeInfos)
                {
                    // Get the list of child nodes for the current node
                    List<FileInfo> childNodeInfos = fileFolderService.list(curNodeInfo.getNodeRef());

                    // can skip the current node if it doesn't have children
                    if (childNodeInfos.size() == 0)
                    {
                        continue;
                    }
                    
                    // Output the child node details
                    // Generate the base path for the current parent node

                    baseBuild.setLength(baseLen);
                    try
                    {
                        String pathSnippet = getDAVHelper().getPathFromNode(pathNodeInfo.getNodeRef(), curNodeInfo.getNodeRef());
                        baseBuild.append(pathSnippet);
                    }
                    catch (FileNotFoundException e)
                    {
                        // move to the next node
                        continue;
                    }

                    int curBaseLen = baseBuild.length();

                    // Output the child node details
                    for (FileInfo curChildInfo : childNodeInfos)
                    {
	                // Build the path for the current child node
	                baseBuild.setLength(curBaseLen);
	
	                baseBuild.append(curChildInfo.getName());
	
	                // Output the current child node details
	                generateResponseForNode(xml, curChildInfo, baseBuild.toString());
	
	                // If the child is a folder add it to the list of next level nodes
	                if (nextNodeInfos != null && curChildInfo.isFolder())
	                {
	                    nextNodeInfos.add(curChildInfo);
	                }
                    }
                }

                // Update the current tree depth
                curDepth++;

                // Move the next level of nodes to the current node list
                nodeInfos = nextNodeInfos;
            }
        }

        // Close the outer XML element
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_MULTI_STATUS, WebDAV.XML_NS_MULTI_STATUS);

        // Send remaining data
        xml.flush();
    }

    /**
     * Creates a WebDAVProperty from the given XML node
     */
    protected WebDAVProperty createProperty(Node node)
    {
        WebDAVProperty property = null;

        String strName = node.getLocalName();
        String strNamespaceUri = node.getNamespaceURI();

        if (WebDAV.DEFAULT_NAMESPACE_URI.equals(strNamespaceUri))
        {
            property = new WebDAVProperty(strName);
        }
        else
        {
            property = new WebDAVProperty(strName, strNamespaceUri, getNamespaceName(strNamespaceUri));
        }

        return property;
    }

    /**
     * Retrieves the namespace name for the given namespace URI, one is
     * generated if it doesn't exist
     */
    private String getNamespaceName(String strNamespaceUri)
    {
        if (strNamespaceUri == null)
        {
            return null;
        }
        String strNamespaceName = m_namespaces.get(strNamespaceUri);
        if (strNamespaceName == null)
        {
            strNamespaceName = "ns" + m_namespaces.size();
            m_namespaces.put(strNamespaceUri, strNamespaceName);
        }

        return strNamespaceName;
    }

    /**
     * Generates the required response XML for the current node
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param path String
     */
    protected void generateResponseForNode(XMLWriter xml, FileInfo nodeInfo, String path) throws Exception
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();
        boolean isFolder = nodeInfo.isFolder();
        
        // Output the response block for the current node
        xml.startElement(
                WebDAV.DAV_NS,
                WebDAV.XML_RESPONSE,
                WebDAV.XML_NS_RESPONSE,
                getDAVHelper().getNullAttributes());

        // Build the href string for the current node
        String strHRef = getURLForPath(m_request, path, isFolder);

        if (nodeInfo.isLink())
        {
            Path pathToNode = getNodeService().getPath(nodeInfo.getLinkNodeRef());
            if (pathToNode.size() > 2)
            {
                pathToNode = pathToNode.subPath(2, pathToNode.size() - 1);
            }

            String rootURL = getURLForPath(m_request, pathToNode.toDisplayPath(getNodeService(), getPermissionService()), true);
            if (rootURL.endsWith(WebDAVHelper.PathSeperator) == false)
            {
                rootURL = rootURL + WebDAVHelper.PathSeperator;
            }

            String fname = (String) getNodeService().getProperty(nodeInfo.getLinkNodeRef(), ContentModel.PROP_NAME);
            strHRef = rootURL + WebDAVHelper.encodeURL(fname, m_userAgent) + WebDAVHelper.PathSeperator;
            isFolder = getFileFolderService().getFileInfo(nodeInfo.getLinkNodeRef()).isFolder();
        }
        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_HREF, WebDAV.XML_NS_HREF, getDAVHelper().getNullAttributes());
        xml.write(strHRef);
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_HREF, WebDAV.XML_NS_HREF);

        switch (m_mode)
        {
        case GET_NAMED_PROPS:
            generateNamedPropertiesResponse(xml, nodeInfo, isFolder);
            break;
        case GET_ALL_PROPS:
            generateAllPropertiesResponse(xml, nodeRef, isFolder);
            break;
        case FIND_PROPS:
            generateFindPropertiesResponse(xml, nodeRef, isFolder);
            break;
        }

        // Close off the response element
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_RESPONSE, WebDAV.XML_NS_RESPONSE);
    }

    /**
     * Generates the XML response for a PROPFIND request that asks for a
     * specific set of properties
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param isDir boolean
     */
    private void generateNamedPropertiesResponse(XMLWriter xml, FileInfo nodeInfo, boolean isDir) throws Exception
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();
        
        // Get the properties for the node
        Map<QName, Serializable> props = getNodeService().getProperties(nodeRef);

        // Output the start of the properties element
        Attributes nullAttr = getDAVHelper().getNullAttributes();

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT, nullAttr);
        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP, nullAttr);

        ArrayList<WebDAVProperty> propertiesNotFound = new ArrayList<WebDAVProperty>();

        TypeConverter typeConv = DefaultTypeConverter.INSTANCE;

        // Loop through the requested property list
        for (WebDAVProperty property : m_properties)
        {
            // Get the requested property details

            String propName = property.getName();
            String propNamespaceUri = property.getNamespaceUri();
//            String propNamespaceName = property.getNamespaceName();

            // Check if the property is a standard WebDAV property

            Object davValue = null;

            if (WebDAV.DEFAULT_NAMESPACE_URI.equals(propNamespaceUri))
            {
                // Check if the client is requesting lock information
                if (propName.equals(WebDAV.XML_LOCK_DISCOVERY)) // && metaData.isLocked())
                {
                    generateLockDiscoveryResponse(xml, nodeRef, isDir);
                }
                else if (propName.equals(WebDAV.XML_SUPPORTED_LOCK))
                {
                    // Output the supported lock types
                    writeLockTypes(xml);
                }

                // Check if the client is requesting the resource type

                else if (propName.equals(WebDAV.XML_RESOURCE_TYPE))
                {
                    // If the node is a folder then return as a collection type

                    xml.startElement(WebDAV.DAV_NS, WebDAV.XML_RESOURCE_TYPE, WebDAV.XML_NS_RESOURCE_TYPE, nullAttr);
                    if (isDir)
                    {
                        xml.write(DocumentHelper.createElement(WebDAV.XML_NS_COLLECTION));
                    }
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_RESOURCE_TYPE, WebDAV.XML_NS_RESOURCE_TYPE);
                }
                else if (propName.equals(WebDAV.XML_DISPLAYNAME))
                {
                    // Get the node name
                    if (getRootNodeRef().equals(nodeRef))
                    {
                        // Output an empty name for the root node
                        xml.write(DocumentHelper.createElement(WebDAV.XML_NS_SOURCE));
                    }
                    else
                    {
                        // Get the node name
                        davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_DISPLAYNAME);

                        // Output the node name
                        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_DISPLAYNAME, WebDAV.XML_NS_DISPLAYNAME, nullAttr);
                        if (davValue != null)
                        {
                            String name = typeConv.convert(String.class, davValue);
                            if (name == null || name.length() == 0)
                            {
                                logger.error("WebDAV name is null, value=" + davValue.getClass().getName() + ", node=" + nodeRef);
                            }
                            xml.write(name);
                        }
                        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_DISPLAYNAME, WebDAV.XML_NS_DISPLAYNAME);
                    }
                }
                else if (propName.equals(WebDAV.XML_SOURCE))
                {
                    // NOTE: source is always a no content element in our
                    // implementation

                    xml.write(DocumentHelper.createElement(WebDAV.XML_NS_SOURCE));
                }
                else if (propName.equals(WebDAV.XML_GET_LAST_MODIFIED))
                {
                    // Get the modifed date/time

                    davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_GET_LAST_MODIFIED);

                    // Output the last modified date of the node

                    xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_LAST_MODIFIED, WebDAV.XML_NS_GET_LAST_MODIFIED,
                            nullAttr);
                    if (davValue != null)
                        xml.write(WebDAV.formatModifiedDate(typeConv.convert(Date.class, davValue)));
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_LAST_MODIFIED, WebDAV.XML_NS_GET_LAST_MODIFIED);
                }
                else if (propName.equals(WebDAV.XML_GET_CONTENT_LANGUAGE) && !isDir)
                {
                    // Get the content language
                    // TODO:
                    // Output the content language
                    xml.startElement(
                            WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LANGUAGE,
                            WebDAV.XML_NS_GET_CONTENT_LANGUAGE, nullAttr);
                    // TODO:
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LANGUAGE, WebDAV.XML_NS_GET_CONTENT_LANGUAGE);
                }
                else if (propName.equals(WebDAV.XML_GET_CONTENT_TYPE) && !isDir)
                {
                    // Get the content type
                    davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_GET_CONTENT_TYPE);

                    // Output the content type
                    xml.startElement(
                            WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_TYPE,
                            WebDAV.XML_NS_GET_CONTENT_TYPE, nullAttr);
                    if (davValue != null)
                        xml.write(typeConv.convert(String.class, davValue));
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_TYPE, WebDAV.XML_NS_GET_CONTENT_TYPE);
                }
                else if (propName.equals(WebDAV.XML_GET_ETAG) && !isDir)
                {
                    // Output the etag

                    xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_ETAG, WebDAV.XML_NS_GET_ETAG, nullAttr);
                    xml.write(getDAVHelper().makeETag(nodeRef));
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_ETAG, WebDAV.XML_NS_GET_ETAG);
                }
                else if (propName.equals(WebDAV.XML_GET_CONTENT_LENGTH))
                {
                    // Get the content length, if it's not a folder
                    long len = 0;

                    if (!isDir)
                    {
                        ContentData contentData = (ContentData) props.get(ContentModel.PROP_CONTENT);
                        if (contentData != null)
                            len = contentData.getSize();
                    }

                    // Output the content length
                    xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LENGTH, WebDAV.XML_NS_GET_CONTENT_LENGTH,
                            nullAttr);
                    xml.write("" + len);
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LENGTH, WebDAV.XML_NS_GET_CONTENT_LENGTH);
                }
                else if (propName.equals(WebDAV.XML_CREATION_DATE))
                {
                    // Get the creation date
                    davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_CREATION_DATE);

                    // Output the creation date
                    xml.startElement(WebDAV.DAV_NS, WebDAV.XML_CREATION_DATE, WebDAV.XML_NS_CREATION_DATE, nullAttr);
                    if (davValue != null)
                        xml.write(WebDAV.formatCreationDate(typeConv.convert(Date.class, davValue)));
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_CREATION_DATE, WebDAV.XML_NS_CREATION_DATE);
                }
                else if ( propName.equals( WebDAV.XML_ALF_AUTHTICKET))
                {
                	// Get the users authentication ticket
                	
                    SessionUser davUser = (SessionUser) m_request.getSession().getAttribute( AuthenticationFilter.AUTHENTICATION_USER);
                    
                    xml.startElement(WebDAV.DAV_NS, WebDAV.XML_ALF_AUTHTICKET, WebDAV.XML_NS_ALF_AUTHTICKET, nullAttr);
                    if ( davUser != null)
                    	xml.write( davUser.getTicket());
                    xml.endElement(WebDAV.DAV_NS, WebDAV.XML_ALF_AUTHTICKET, WebDAV.XML_NS_ALF_AUTHTICKET);
                }
                else
                {
                    // Could not map the requested property to an Alfresco property
                    if (property.getName().equals(WebDAV.XML_HREF) == false)
                        propertiesNotFound.add(property);
                }
            }
            else
            {
                // Look in the custom properties

                // TODO: Custom properties lookup
//                String qualifiedName = propNamespaceUri + WebDAV.NAMESPACE_SEPARATOR + propName;

                String value = (String) getNodeService().getProperty(nodeRef, property.createQName());
                if (value == null)
                {
                propertiesNotFound.add(property);
            }
                else
                {
                    if (property.hasNamespaceName())
                    {
                        xml.startElement(property.getNamespaceName(), property.getName(), property.getNamespaceName() + WebDAV.NAMESPACE_SEPARATOR + property.getName(), nullAttr);
                        xml.write(value);
                        xml.endElement(property.getNamespaceName(), property.getName(), property.getNamespaceName() + WebDAV.NAMESPACE_SEPARATOR + property.getName());
                    }
                    else
                    {
                        xml.startElement("", property.getName(), property.getName(), nullAttr);
                        xml.write(value);
                        xml.endElement("", property.getName(), property.getName());
                    }
                }

            }
        }

        // Close off the successful part of the response

        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP);

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS, nullAttr);
        xml.write(WebDAV.HTTP1_1 + " " + HttpServletResponse.SC_OK + " " + WebDAV.SC_OK_DESC);
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS);

        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT);

        // If some of the requested properties were not found return another
        // status section

        if (propertiesNotFound.size() > 0)
        {
            // Start the second status section

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT, nullAttr);
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP, nullAttr);

            // Loop through the list of properties that were not found

            for (WebDAVProperty property : propertiesNotFound)
            {
                // Output the property not found status block

                String propName = property.getName();
                String propNamespaceName = property.getNamespaceName();
                String propQName = propName;
                if (propNamespaceName != null && propNamespaceName.length() > 0)
                    propQName = propNamespaceName + ":" + propName;

                xml.write(DocumentHelper.createElement(propQName));
            }

            // Close the unsuccessful part of the response

            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP);

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS, nullAttr);
            xml.write(WebDAV.HTTP1_1 + " " + HttpServletResponse.SC_NOT_FOUND + " " + WebDAV.SC_NOT_FOUND_DESC);
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS);

            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT);
        }
    }

    /**
     * Generates the XML response for a PROPFIND request that asks for all known
     * properties
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param isDir boolean
     */
    protected void generateAllPropertiesResponse(XMLWriter xml, NodeRef node, boolean isDir) throws Exception
    {
        // Get the properties for the node

        Map<QName, Serializable> props = getNodeService().getProperties(node);

        // Output the start of the properties element

        Attributes nullAttr = getDAVHelper().getNullAttributes();

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT, nullAttr);
        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP, nullAttr);

        // Generate a lock status report, if locked

        generateLockDiscoveryResponse(xml, node, isDir);

        // Output the supported lock types

        writeLockTypes(xml);

        // If the node is a folder then return as a collection type

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_RESOURCE_TYPE, WebDAV.XML_NS_RESOURCE_TYPE, nullAttr);
        if (isDir)
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_COLLECTION));
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_RESOURCE_TYPE, WebDAV.XML_NS_RESOURCE_TYPE);

        // Get the node name

        Object davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_DISPLAYNAME);

        TypeConverter typeConv = DefaultTypeConverter.INSTANCE;

        // Output the node name

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_DISPLAYNAME, WebDAV.XML_NS_DISPLAYNAME, nullAttr);
        if (davValue != null)
        {
            String name = typeConv.convert(String.class, davValue);
            if (name == null || name.length() == 0)
            {
                logger.error("WebDAV name is null, value=" + davValue.getClass().getName() + ", node=" + node);
            }
            xml.write(name);
        }
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_DISPLAYNAME, WebDAV.XML_NS_DISPLAYNAME);

        // Output the source
        //
        // NOTE: source is always a no content element in our implementation

        xml.write(DocumentHelper.createElement(WebDAV.XML_NS_SOURCE));

        // Get the creation date

        davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_CREATION_DATE);

        // Output the creation date

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_CREATION_DATE, WebDAV.XML_NS_CREATION_DATE, nullAttr);
        if (davValue != null)
            xml.write(WebDAV.formatCreationDate(typeConv.convert(Date.class, davValue)));
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_CREATION_DATE, WebDAV.XML_NS_CREATION_DATE);

        // Get the modifed date/time

        davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_GET_LAST_MODIFIED);

        // Output the last modified date of the node

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_LAST_MODIFIED, WebDAV.XML_NS_GET_LAST_MODIFIED, nullAttr);
        if (davValue != null)
            xml.write(WebDAV.formatModifiedDate(typeConv.convert(Date.class, davValue)));
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_LAST_MODIFIED, WebDAV.XML_NS_GET_LAST_MODIFIED);

        // For a file node output the content language and content type

        if (isDir == false)
        {
            // Get the content language

            // TODO:
            // Output the content language

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LANGUAGE, WebDAV.XML_NS_GET_CONTENT_LANGUAGE,
                    nullAttr);
            // TODO:
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LANGUAGE, WebDAV.XML_NS_GET_CONTENT_LANGUAGE);

            // Get the content type
            davValue = WebDAV.getDAVPropertyValue(props, WebDAV.XML_GET_CONTENT_TYPE);

            // Output the content type
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_TYPE, WebDAV.XML_NS_GET_CONTENT_TYPE, nullAttr);
            if (davValue != null)
                xml.write(typeConv.convert(String.class, davValue));
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_TYPE, WebDAV.XML_NS_GET_CONTENT_TYPE);

            // Output the etag

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_ETAG, WebDAV.XML_NS_GET_ETAG, nullAttr);
            xml.write(getDAVHelper().makeETag(node));
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_ETAG, WebDAV.XML_NS_GET_ETAG);
        }

        // Get the content length, if it's not a folder

        long len = 0;

        if (isDir == false)
        {
            ContentData contentData = (ContentData) props.get(ContentModel.PROP_CONTENT);
            if (contentData != null)
                len = contentData.getSize();
        }

        // Output the content length

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LENGTH, WebDAV.XML_NS_GET_CONTENT_LENGTH, nullAttr);
        xml.write("" + len);
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_GET_CONTENT_LENGTH, WebDAV.XML_NS_GET_CONTENT_LENGTH);

        // Print out all the custom properties

        SessionUser davUser = (SessionUser) m_request.getSession().getAttribute( AuthenticationFilter.AUTHENTICATION_USER);
        
        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_ALF_AUTHTICKET, WebDAV.XML_NS_ALF_AUTHTICKET, nullAttr);
        if ( davUser != null)
        	xml.write( davUser.getTicket());
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_ALF_AUTHTICKET, WebDAV.XML_NS_ALF_AUTHTICKET);

        // Close off the response

        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP);

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS, nullAttr);
        xml.write(WebDAV.HTTP1_1 + " " + HttpServletResponse.SC_OK + " " + WebDAV.SC_OK_DESC);
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS);

        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT);
    }

    /**
     * Generates the XML response for a PROPFIND request that asks for a list of
     * all known properties
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param isDir boolean
     */
    protected void generateFindPropertiesResponse(XMLWriter xml, NodeRef node, boolean isDir)
    {
        try
        {
            // Output the start of the properties element

            Attributes nullAttr = getDAVHelper().getNullAttributes();

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROPSTAT, WebDAV.XML_NS_PROPSTAT, nullAttr);
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP, nullAttr);

            // Output the well-known properties

            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_LOCK_DISCOVERY));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_SUPPORTED_LOCK));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_RESOURCE_TYPE));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_DISPLAYNAME));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_GET_LAST_MODIFIED));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_GET_CONTENT_LENGTH));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_CREATION_DATE));
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_GET_ETAG));

            if (isDir)
            {
                xml.write(DocumentHelper.createElement(WebDAV.XML_NS_GET_CONTENT_LANGUAGE));
                xml.write(DocumentHelper.createElement(WebDAV.XML_NS_GET_CONTENT_TYPE));
            }

            // Output the custom properties

            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_ALF_AUTHTICKET));

            // Close off the response

            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP);

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_STATUS, WebDAV.XML_NS_STATUS, nullAttr);
            xml.write(WebDAV.HTTP1_1 + " " + HttpServletResponse.SC_OK + " " + WebDAV.SC_OK_DESC);
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
     * Generates the XML response snippet showing the lock information for the
     * given path
     * 
     * @param xml XMLWriter
     * @param node NodeRef
     * @param isDir boolean
     */
    protected void generateLockDiscoveryResponse(XMLWriter xml, NodeRef node, boolean isDir) throws Exception
    {
        // Output the lock status response

        LockInfo lockInfo = getNodeLockInfo(node);
        if (lockInfo.isLocked())
        {
            generateLockDiscoveryXML(xml, node, lockInfo);
        }
    }

    /**
     * Output the supported lock types XML element
     * 
     * @param xml XMLWriter
     */
    protected void writeLockTypes(XMLWriter xml)
    {
        try
        {
            AttributesImpl nullAttr = getDAVHelper().getNullAttributes();

            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_SUPPORTED_LOCK, WebDAV.XML_NS_SUPPORTED_LOCK, nullAttr);

            // Output exclusive lock
            writeLock(xml, WebDAV.XML_NS_EXCLUSIVE);
            // Output shared lock
            writeLock(xml, WebDAV.XML_NS_SHARED);

            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_SUPPORTED_LOCK, WebDAV.XML_NS_SUPPORTED_LOCK);
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("XML write error", ex);
        }
    }
    
    /**
     * Output the lockentry element of the specified type
     * @param xml XMLWriter
     * @param lockType lock type. Can be WebDAV.XML_NS_EXCLUSIVE or WebDAV.XML_NS_SHARED
     * @param lockType lock type containing namespace
     * @throws SAXException
     * @throws IOException
     */
    private void writeLock(XMLWriter xml, String lockType) throws SAXException, IOException
    {
        AttributesImpl nullAttr = getDAVHelper().getNullAttributes();

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_ENTRY, WebDAV.XML_NS_LOCK_ENTRY, nullAttr); 
        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_SCOPE, WebDAV.XML_NS_LOCK_SCOPE, nullAttr);
        xml.write(DocumentHelper.createElement(lockType));
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_SCOPE, WebDAV.XML_NS_LOCK_SCOPE);

        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_TYPE, WebDAV.XML_NS_LOCK_TYPE, nullAttr);
        xml.write(DocumentHelper.createElement(WebDAV.XML_NS_WRITE));
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_TYPE, WebDAV.XML_NS_LOCK_TYPE);
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_ENTRY, WebDAV.XML_NS_LOCK_ENTRY); 
    }
}
