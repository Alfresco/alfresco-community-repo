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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WebDAVModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implements the WebDAV LOCK method
 * 
 * @author gavinc
 */
public class LockMethod extends WebDAVMethod
{
    public static final String EMPTY_NS = "";
    
    protected int m_timeoutDuration = WebDAV.TIMEOUT_INFINITY;
    
    protected LockInfo lockInfo = new LockInfo();

    protected boolean createExclusive;

    protected String lockToken= null;

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
        // Get user Agent
        
        m_userAgent = m_request.getHeader(WebDAV.HEADER_USER_AGENT);

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
            OUTER: for (Node currentNode = body.getDocumentElement().getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling())
            {
                if (currentNode instanceof Element && WebDAV.DEFAULT_NAMESPACE_URI.equals(((Element) currentNode).getNamespaceURI())
                        && WebDAV.XML_LOCK_SCOPE.equals(((Element) currentNode).getLocalName()))
                {
                    for (Node propertiesNode = currentNode.getFirstChild(); propertiesNode != null; propertiesNode = propertiesNode.getNextSibling())
                    {
                        if (propertiesNode instanceof Element && WebDAV.DEFAULT_NAMESPACE_URI.equals(((Element) propertiesNode).getNamespaceURI()))
                        {
                            this.createExclusive = WebDAV.XML_EXCLUSIVE.equals(propertiesNode.getLocalName());
                            break OUTER;
                        }
                    }
                    break OUTER;
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
            lockNodeInfo = getNodeForPath(getRootNodeRef(), getPath(), m_request.getServletPath());
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
               dirInfo = FileFolderUtil.makeFolders(fileFolderService, rootNodeRef, dirPathElements, ContentModel.TYPE_FOLDER);
            }
            
            if (dirInfo == null)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
            // create the file
            lockNodeInfo = createNode(dirInfo.getNodeRef(), splitPath[1], ContentModel.TYPE_CONTENT);
            
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
            // If a request body is not defined and "If" header is sent we have createExclusive as false,
            // but we need to check a previous LOCK was an exclusive. I.e. get the property for node. It
            // is already has got in a checkNode method, so we need just get a scope from lockInfo.
            // This particular case was raised as ALF-4008.
            this.createExclusive = WebDAV.XML_EXCLUSIVE.equals(this.lockInfo.getScope());
            // Refresh an existing lock
            refreshLock(lockNodeInfo, userName);
        }
        else
        {
            this.lockInfo = checkNode(lockNodeInfo, true, this.createExclusive);
            // Create a new lock
            createLock(lockNodeInfo, userName);
        }


        m_response.setHeader(WebDAV.HEADER_LOCK_TOKEN, "<" + WebDAV.makeLockToken(lockNodeInfo.getNodeRef(), userName) + ">");
        m_response.setHeader(WebDAV.HEADER_CONTENT_TYPE, WebDAV.XML_CONTENT_TYPE);

        // We either created a new lock or refreshed an existing lock, send back the lock details
        generateResponse(lockNodeInfo.getNodeRef(), userName);
    }
    
    /**
     * Create a new node
     * 
     * @param parentNodeRef the parent node.  
     * @param name the name of the node
     * @param typeQName the type to create
     * @return Returns the new node's file information
     *  
     */
    protected FileInfo createNode(NodeRef parentNodeRef, String name, QName typeQName)
    {
        return getFileFolderService().create(parentNodeRef, name, ContentModel.TYPE_CONTENT);
    }

    /**
     * Create a new lock
     * 
     * @param lockNode NodeRef
     * @param userName String
     * @exception WebDAVServerException
     */
    protected final void createLock(FileInfo lockNode, String userName) throws WebDAVServerException
    {
        LockService lockService = getLockService();

        // Create Lock token
        lockToken = WebDAV.makeLockToken(lockNode.getNodeRef(), userName);

        if (this.createExclusive)
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
        getNodeService().setProperty(lockNode.getNodeRef(), WebDAVModel.PROP_LOCK_SCOPE, this.createExclusive ? WebDAV.XML_EXCLUSIVE : WebDAV.XML_SHARED);

        logger.debug("Properties of the " + lockNode + " was changed");
    }

    /**
     * Refresh an existing lock
     * 
     * @param lockNode NodeRef
     * @param userName String
     * @exception WebDAVServerException
     */
    protected final void refreshLock(FileInfo lockNode, String userName) throws WebDAVServerException
    {
        if (this.createExclusive)
        {
            // Update the expiry for the lock
            getLockService().lock(lockNode.getNodeRef(), LockType.WRITE_LOCK, getLockTimeout());
        }
    }

    /**
     * Generates the XML lock discovery response body
     */
    protected void generateResponse(NodeRef lockNode, String userName) throws Exception
    {
        String scope;
        String lt;
        if (lockToken != null)
        {
            // In case of lock creation take the scope from request header
            scope = this.createExclusive ? WebDAV.XML_EXCLUSIVE : WebDAV.XML_SHARED;
            // Output created lock
            lt = lockToken;
        }
        else
        {
            // In case of lock refreshing take the scope from previously stored lock
            scope = this.lockInfo.getScope();
            // Output refreshed lock
            lt = this.lockInfo.getToken();
        }
        String owner = (String) getNodeService().getProperty(lockNode, ContentModel.PROP_LOCK_OWNER);

        XMLWriter xml = createXMLWriter();

        xml.startDocument();

        String nsdec = generateNamespaceDeclarations(null);
        xml.startElement(WebDAV.DAV_NS, WebDAV.XML_PROP + nsdec, WebDAV.XML_NS_PROP + nsdec, getDAVHelper().getNullAttributes());

        // Output the lock details
        generateLockDiscoveryXML(xml, lockNode, false, scope, WebDAV.getDepthName(m_depth), lt, owner);

        // Close off the XML
        xml.endElement(WebDAV.DAV_NS, WebDAV.XML_PROP, WebDAV.XML_NS_PROP);

       
    }
        
    /**
     * Generates a list of namespace declarations for the response
     */
    protected String generateNamespaceDeclarations(HashMap<String,String> nameSpaces)
    {
        StringBuilder ns = new StringBuilder();

        ns.append(" ");
        ns.append(WebDAV.XML_NS);
        ns.append(":");
        ns.append(WebDAV.DAV_NS);
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
