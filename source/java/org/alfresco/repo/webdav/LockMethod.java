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
package org.alfresco.repo.webdav;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WebDAVModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

/**
 * Implements the WebDAV LOCK method
 * 
 * @author gavinc
 */
public class LockMethod extends WebDAVMethod
{
    public static final String EMPTY_NS = "";
    
    private int m_timeoutDuration = WebDAV.TIMEOUT_INFINITY;
    
    private LockInfo lockInfo = new LockInfo();

    private String m_scope = null;

    private String lockToken= null;

    /**
     * Default constructor
     */
    public LockMethod()
    {
    }

    /**
     * Returns true if request has lock token in the If header
     * 
     * @return boolean
     */
    protected final boolean hasLockToken()
    {
        if (m_conditions != null)
        {
            for (Condition condition : m_conditions)
            {
                if (!condition.getLockTokensMatch().isEmpty())
                {
                    return true;
                }
    }
        }
        return false;
    }

    /**
     * Return the lock timeout, in minutes
     * 
     * @return int
     */
    protected final int getLockTimeout()
    {
        return m_timeoutDuration;
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Get the depth

        parseDepthHeader();
        
        // According to the specification: "Values other than 0 or infinity MUST NOT be used with the Depth header on a LOCK method.".
        // The specification does not specify the error code for this case - so we use HttpServletResponse.SC_INTERNAL_SERVER_ERROR.
        if (m_depth != WebDAV.DEPTH_0 && m_depth != WebDAV.DEPTH_INFINITY)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        // Parse Lock tokens and ETags, if any

        parseIfHeader();

        // Get the lock timeout value

        String strTimeout = m_request.getHeader(WebDAV.HEADER_TIMEOUT);

        // If the timeout header starts with anything other than Second
        // leave the timeout as the default

        if (strTimeout != null && strTimeout.startsWith(WebDAV.SECOND))
        {
            try
            {
                // Some clients send header as Second-180 Seconds so we need to
                // look for the space

                int idx = strTimeout.indexOf(" ");

                if (idx != -1)
                {
                    // Get the bit after Second- and before the space

                    strTimeout = strTimeout.substring(WebDAV.SECOND.length(), idx);
                }
                else
                {
                    // The string must be in the correct format

                    strTimeout = strTimeout.substring(WebDAV.SECOND.length());
                }
                m_timeoutDuration = Integer.parseInt(strTimeout);
            }
            catch (Exception e)
            {
                // Warn about the parse failure and leave the timeout as the
                // default

                logger.warn("Failed to parse Timeout header: " + strTimeout);
            }
        }

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Timeout=" + getLockTimeout() + ", depth=" + getDepth());
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        if (m_request.getContentLength() == -1)
        {
            return;
        }

        Document body = getRequestBodyAsDocument();
        if (body != null)
        {
            Element rootElement = body.getDocumentElement();
            NodeList childList = rootElement.getChildNodes();

            for (int i = 0; i < childList.getLength(); i++)
            {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType())
                {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    if (currentNode.getNodeName().endsWith(WebDAV.XML_LOCK_SCOPE))
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
                                m_scope = propertiesNode.getNodeName();
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
     * Execute the request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        FileFolderService fileFolderService = getFileFolderService();
        String path = getPath();
        NodeRef rootNodeRef = getRootNodeRef();
        // Get the active user
        String userName = getDAVHelper().getAuthenticationService().getCurrentUserName();

        if (logger.isDebugEnabled())
        {
            logger.debug("Locking node: \n" +
                    "   user: " + userName + "\n" +
                    "   path: " + path);
        }

        FileInfo lockNodeInfo = null;
        try
        {
            // Check if the path exists
            lockNodeInfo = getDAVHelper().getNodeForPath(getRootNodeRef(), getPath(), m_request.getServletPath());
        }
        catch (FileNotFoundException e)
        {
            // need to create it
            String[] splitPath = getDAVHelper().splitPath(path);
            // check
            if (splitPath[1].length() == 0)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
            FileInfo dirInfo = null;
            List<String> dirPathElements = getDAVHelper().splitAllPaths(splitPath[0]);
            if (dirPathElements.size() == 0)
            {
               // if there are no path elements we are at the root so get the root node
               dirInfo = fileFolderService.getFileInfo(getRootNodeRef());
            }
            else
            {
               // make sure folder structure is present
               dirInfo = FileFolderServiceImpl.makeFolders(fileFolderService, rootNodeRef, dirPathElements, ContentModel.TYPE_FOLDER);
            }
            
            if (dirInfo == null)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
            // create the file
            lockNodeInfo = fileFolderService.create(dirInfo.getNodeRef(), splitPath[1], ContentModel.TYPE_CONTENT);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Created new node for lock: \n" +
                        "   path: " + path + "\n" +
                        "   node: " + lockNodeInfo);
            }
            
            m_response.setStatus(HttpServletResponse.SC_CREATED);
        }


        
        // Check if this is a new lock or a lock refresh
        if (hasLockToken())
        {
            this.lockInfo = checkNode(lockNodeInfo);
            // Refresh an existing lock
            refreshLock(lockNodeInfo, userName);
        }
        else
        {
            this.lockInfo = checkNode(lockNodeInfo, true, WebDAV.XML_EXCLUSIVE.equals(m_scope));
            // Create a new lock
            createLock(lockNodeInfo, userName);
        }


        m_response.setHeader(WebDAV.HEADER_LOCK_TOKEN, "<" + WebDAV.makeLockToken(lockNodeInfo.getNodeRef(), userName) + ">");
        m_response.setHeader(WebDAV.HEADER_CONTENT_TYPE, WebDAV.XML_CONTENT_TYPE);

        // We either created a new lock or refreshed an existing lock, send back the lock details
        generateResponse(lockNodeInfo.getNodeRef(), userName);
    }

    /**
     * Create a new lock
     * 
     * @param lockNode NodeRef
     * @param userName String
     * @exception WebDAVServerException
     */
    private final void createLock(FileInfo lockNode, String userName) throws WebDAVServerException
    {
        LockService lockService = getLockService();

        // Create Lock token
        lockToken = WebDAV.makeLockToken(lockNode.getNodeRef(), userName);

        if (WebDAV.XML_EXCLUSIVE.equals(m_scope))
        {
            // Lock the node
            lockService.lock(lockNode.getNodeRef(), LockType.WRITE_LOCK, getLockTimeout());

            //this.lockInfo.setToken(lockToken);
            getNodeService().setProperty(lockNode.getNodeRef(), WebDAVModel.PROP_OPAQUE_LOCK_TOKEN, lockToken);
        }
        else
        {
            this.lockInfo.addSharedLockToken(lockToken);
            String sharedLockTokens = LockInfo.makeSharedLockTokensString(this.lockInfo.getSharedLockTokens());
            getNodeService().setProperty(lockNode.getNodeRef(), WebDAVModel.PROP_SHARED_LOCK_TOKENS, sharedLockTokens);
            
        }

        // Store lock depth
        getNodeService().setProperty(lockNode.getNodeRef(), WebDAVModel.PROP_LOCK_DEPTH, WebDAV.getDepthName(m_depth));

        // Store lock scope (shared/exclusive)
        getNodeService().setProperty(lockNode.getNodeRef(), WebDAVModel.PROP_LOCK_SCOPE, m_scope);

    }

    /**
     * Refresh an existing lock
     * 
     * @param lockNode NodeRef
     * @param userName String
     * @exception WebDAVServerException
     */
    private final void refreshLock(FileInfo lockNode, String userName) throws WebDAVServerException
    {
        LockService lockService = getLockService();

        if (WebDAV.XML_EXCLUSIVE.equals(m_scope))
        {
            // Update the expiry for the lock
            lockService.lock(lockNode.getNodeRef(), LockType.WRITE_LOCK, getLockTimeout());
        }
    }

    /**
     * Generates the XML lock discovery response body
     */
    private void generateResponse(NodeRef lockNode, String userName) throws Exception
    {
        XMLWriter xml = createXMLWriter();

        xml.startDocument();

        String nsdec = generateNamespaceDeclarations(null);
        xml.startElement(EMPTY_NS, WebDAV.XML_PROP + nsdec, WebDAV.XML_PROP + nsdec,
                getDAVHelper().getNullAttributes());

        // Output the lock details
        generateLockDiscoveryXML(xml, lockNode);

        // Close off the XML
        xml.endElement(EMPTY_NS, WebDAV.XML_PROP, WebDAV.XML_PROP);

        // Send the XML back to the client
        xml.flush();
    }
    
    
    /**
     * Generates the lock discovery XML response
     * 
     * @param xml XMLWriter
     * @param lockNode NodeRef
     */
    protected void generateLockDiscoveryXML(XMLWriter xml, NodeRef lockNode) throws Exception
    {
        Attributes nullAttr= getDAVHelper().getNullAttributes();
        
        if (lockNode != null)
        {
            
            // Get the lock details

            NodeService nodeService = getNodeService();
            
            String owner = (String) nodeService.getProperty(lockNode, ContentModel.PROP_LOCK_OWNER);
            Date expiryDate = (Date) nodeService.getProperty(lockNode, ContentModel.PROP_EXPIRY_DATE);
            
            // Output the XML response
            
            xml.startElement(EMPTY_NS, WebDAV.XML_LOCK_DISCOVERY, WebDAV.XML_LOCK_DISCOVERY, nullAttr);  
            xml.startElement(EMPTY_NS, WebDAV.XML_ACTIVE_LOCK, WebDAV.XML_ACTIVE_LOCK, nullAttr);
             
            xml.startElement(EMPTY_NS, WebDAV.XML_LOCK_TYPE, WebDAV.XML_LOCK_TYPE, nullAttr);
            xml.write(DocumentHelper.createElement(WebDAV.XML_WRITE));
            xml.endElement(EMPTY_NS, WebDAV.XML_LOCK_TYPE, WebDAV.XML_LOCK_TYPE);
             
            xml.startElement(EMPTY_NS, WebDAV.XML_LOCK_SCOPE, WebDAV.XML_LOCK_SCOPE, nullAttr);
            if (lockToken != null)
            {
                // In case of lock creation take the scope from request header
                xml.write(DocumentHelper.createElement(m_scope));
            }
            else
            {
                // In case of lock refreshing take the scope from previously stored lock
                xml.write(DocumentHelper.createElement(this.lockInfo.getScope()));
            }
            xml.endElement(EMPTY_NS, WebDAV.XML_LOCK_SCOPE, WebDAV.XML_LOCK_SCOPE);
             
            xml.startElement(EMPTY_NS, WebDAV.XML_DEPTH, WebDAV.XML_DEPTH, nullAttr);
            xml.write(WebDAV.getDepthName(m_depth));
            xml.endElement(EMPTY_NS, WebDAV.XML_DEPTH, WebDAV.XML_DEPTH);
             
            xml.startElement(EMPTY_NS, WebDAV.XML_OWNER, WebDAV.XML_OWNER, nullAttr);
            xml.write(owner);
            xml.endElement(EMPTY_NS, WebDAV.XML_OWNER, WebDAV.XML_OWNER);
             
            xml.startElement(EMPTY_NS, WebDAV.XML_TIMEOUT, WebDAV.XML_TIMEOUT, nullAttr);

            // Output the expiry time
            
            String strTimeout = WebDAV.INFINITE;
            if (expiryDate != null)
            {
                  long timeoutRemaining = (expiryDate.getTime() - System.currentTimeMillis())/1000L;
              
                  strTimeout = WebDAV.SECOND + timeoutRemaining;
            }
            xml.write(strTimeout);
           
            xml.endElement(EMPTY_NS, WebDAV.XML_TIMEOUT, WebDAV.XML_TIMEOUT);
             
            xml.startElement(EMPTY_NS, WebDAV.XML_LOCK_TOKEN, WebDAV.XML_LOCK_TOKEN, nullAttr);
            xml.startElement(EMPTY_NS, WebDAV.XML_HREF, WebDAV.XML_HREF, nullAttr);
            if (lockToken != null)
            {
                // Output created lock
                xml.write(lockToken);
            }
            else
            {
                // Output refreshed lock
                xml.write(this.lockInfo.getToken());
            }
            xml.endElement(EMPTY_NS, WebDAV.XML_HREF, WebDAV.XML_HREF);
            xml.endElement(EMPTY_NS, WebDAV.XML_LOCK_TOKEN, WebDAV.XML_LOCK_TOKEN);
           
            xml.endElement(EMPTY_NS, WebDAV.XML_ACTIVE_LOCK, WebDAV.XML_ACTIVE_LOCK);
            xml.endElement(EMPTY_NS, WebDAV.XML_LOCK_DISCOVERY, WebDAV.XML_LOCK_DISCOVERY);
        }
    }

    /**
     * Generates a list of namespace declarations for the response
     */
    protected String generateNamespaceDeclarations(HashMap<String,String> nameSpaces)
    {
        StringBuilder ns = new StringBuilder();

        ns.append(" ");
        ns.append(WebDAV.XML_NS);
        ns.append("=\"");
        ns.append(WebDAV.DEFAULT_NAMESPACE_URI);
        ns.append("\"");

        // Add additional namespaces
        
        if ( nameSpaces != null)
        {
            Iterator<String> namespaceList = nameSpaces.keySet().iterator();
    
            while (namespaceList.hasNext())
            {
                String strNamespaceUri = namespaceList.next();
                String strNamespaceName = nameSpaces.get(strNamespaceUri);
                
                ns.append(" ").append(WebDAV.XML_NS).append(":").append(strNamespaceName).append("=\"");
                ns.append(strNamespaceUri).append("\" ");
            }
        }
        
        return ns.toString();
    }

    
}
