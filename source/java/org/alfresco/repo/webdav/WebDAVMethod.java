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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Abstract base class for all the WebDAV method handling classes
 * 
 * @author gavinc
 */
public abstract class WebDAVMethod
{
    // Log output

    protected static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");

    // Output formatted XML in the response

    private static final boolean XMLPrettyPrint = true;

    // Servlet request/response

    protected HttpServletRequest m_request;
    protected HttpServletResponse m_response;

    // WebDAV helper

    protected WebDAVHelper m_davHelper;

    // Root node

    protected NodeRef m_rootNodeRef;

    // Repository path

    protected String m_strPath = null;

    /**
     * Default constructor
     */
    public WebDAVMethod()
    {
    }

    /**
     * Set the request/response details
     * 
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param registry ServiceRegistry
     * @param rootNode NodeRef
     */
    public void setDetails(HttpServletRequest req, HttpServletResponse resp, WebDAVHelper davHelper, NodeRef rootNode)
    {
        m_request = req;
        m_response = resp;
        m_davHelper = davHelper;
        m_rootNodeRef = rootNode;

        m_strPath = WebDAV.getRepositoryPath(req);
    }
    
    /**
     * Override and return <tt>true</tt> if the method is a query method only.  The default implementation
     * returns <tt>false</tt>.
     * 
     * @return          Returns <tt>true</tt> if the method transaction may be read-only
     */
    protected boolean isReadOnly()
    {
        return false;
    }

    /**
     * Executes the method, wrapping the call to {@link #executeImpl()} in an appropriate transaction
     * and handling the error conditions.
     */
    public final void execute() throws WebDAVServerException
    {
        // Parse the HTTP headers
        parseRequestHeaders();

        // Parse the HTTP body
        parseRequestBody();
        
        RetryingTransactionCallback<Object> executeImplCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                executeImpl();
                return null;
            }
        };
        try
        {
            boolean isReadOnly = isReadOnly();
            // Execute the method
            getTransactionService().getRetryingTransactionHelper().doInTransaction(executeImplCallback, isReadOnly);
        }
        catch (AccessDeniedException e)
        {
            // Return a forbidden status
            throw new WebDAVServerException(HttpServletResponse.SC_UNAUTHORIZED, e);
        }
        catch (Throwable e)
        {
            if (e instanceof WebDAVServerException)
            {
               throw (WebDAVServerException) e;
            }
            else if (e.getCause() instanceof WebDAVServerException)
            {
                throw (WebDAVServerException) e.getCause();
            }
            else
            {
                // Convert error to a server error
                throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }
        }
    }

    /**
     * Access the content repository to satisfy the request and generates the appropriate WebDAV
     * response.
     * 
     * @throws WebDAVServerException a general server exception
     * @throws Exception any unhandled exception
     */
    protected abstract void executeImpl() throws WebDAVServerException, Exception;

    /**
     * Parses the given request body represented as an XML document and sets any necessary context
     * ready for execution.
     */
    protected abstract void parseRequestBody() throws WebDAVServerException;

    /**
     * Parses the HTTP headers of the request and sets any necessary context ready for execution.
     */
    protected abstract void parseRequestHeaders() throws WebDAVServerException;

    /**
     * Retrieves the request body as an XML document
     * 
     * @return The body of the request as an XML document or null if there isn't a body
     */
    protected Document getRequestBodyAsDocument() throws WebDAVServerException
    {
        Document body = null;

        if (m_request.getContentLength() > 0)
        {
            // TODO: Do we need to do anything for chunking support?

            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);

                DocumentBuilder builder = factory.newDocumentBuilder();
                body = builder.parse(new InputSource(m_request.getReader()));
            }
            catch (ParserConfigurationException e)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST, e);
            }
            catch (SAXException e)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST, e);
            }
            catch (IOException e)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST, e);
            }
        }

        return body;
    }

    /**
     * Returns the lock token present in the If header
     * 
     * @return The lock token present in the If header
     */
    protected String parseIfHeader() throws WebDAVServerException
    {
        String strLockToken = null;

        String strIf = m_request.getHeader(WebDAV.HEADER_IF);

        if (logger.isDebugEnabled())
            logger.debug("Parsing If header: " + strIf);

        if (strIf != null && strIf.length() > 0)
        {
            if (strIf.startsWith("(<"))
            {
                // Parse the tokens (only get the first one though)
                
                int idx = strIf.indexOf(">");
                if (idx != -1)
                {
                    try
                    {
                        strLockToken = strIf.substring(WebDAV.OPAQUE_LOCK_TOKEN.length() + 2, idx);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        logger.warn("Failed to parse If header: " + strIf);
                    }
                }
                else
                {
                    throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
                }

                // Print a warning if there are other tokens detected
                
                if (strIf.length() > idx + 2)
                {
                    logger.warn("The If header contained more than one lock token, only one is supported");
                }
            }
            else if (strIf.startsWith("<"))
            {
                logger.warn("Tagged lists in the If header are not supported");
            }
            else if (strIf.startsWith("(["))
            {
                logger.warn("ETags in the If header are not supported");
            }
        }

        return strLockToken;
    }

    /**
     * Return the WebDAV protocol helper
     * 
     * @return WebDAVHelper
     */
    protected final WebDAVHelper getDAVHelper()
    {
        return m_davHelper;
    }

    /**
     * Return the service registry
     * 
     * @return ServiceRegistry
     */
    protected final ServiceRegistry getServiceRegistry()
    {
        return m_davHelper.getServiceRegistry();
    }

    /**
     * Convenience method to return the transaction service
     * 
     * @return TransactionService
     */
    protected final TransactionService getTransactionService()
    {
        return m_davHelper.getServiceRegistry().getTransactionService();
    }

    /**
     * Convenience method to return the node service
     * 
     * @return NodeService
     */
    protected final NodeService getNodeService()
    {
        return m_davHelper.getNodeService();
    }
    
    /**
     * @return Returns the general file/folder manipulation service
     */
    protected final FileFolderService getFileFolderService()
    {
        return m_davHelper.getFileFolderService();
    }

    /**
     * Convenience method to return the content service
     * 
     * @return ContentService
     */
    protected final ContentService getContentService()
    {
        return m_davHelper.getServiceRegistry().getContentService();
    }

    /**
     * Convenience method to return the mimetype service
     * 
     * @return MimetypeService
     */
    protected final MimetypeService getMimetypeService()
    {
        return m_davHelper.getMimetypeService();
    }
    
    /**
     * Convenience method to return the lock service
     * 
     * @return LockService
     */
    protected final LockService getLockService()
    {
        return m_davHelper.getLockService();
    }
    
    /**
     * Convenience method to return the authentication service
     * 
     * @return AuthenticationService
     */
    protected final AuthenticationService getAuthenticationService()
    {
        return m_davHelper.getAuthenticationService();
    }
    
    /**
     * @return Returns the path of the servlet
     */
    protected final String getServletPath()
    {
        return m_request.getServletPath();
    }
    
    /**
     * Return the root node
     * 
     * @return NodeRef
     */
    protected final NodeRef getRootNodeRef()
    {
        return m_rootNodeRef;
    }

    /**
     * Return the relative path
     * 
     * @return String
     */
    protected final String getPath()
    {
        return m_strPath;
    }

    /**
     * Create an XML writer for the response
     * 
     * @return XMLWriter
     * @exception IOException
     */
    protected final XMLWriter createXMLWriter() throws IOException
    {
        // Check if debug output or XML pretty printing is enabled

        XMLWriter writer = null;
        
        if (XMLPrettyPrint == true || logger.isDebugEnabled())
        {
            writer = new XMLWriter(m_response.getWriter(), OutputFormat.createPrettyPrint());
        }
        else
        {
            writer = new XMLWriter(m_response.getWriter(), OutputFormat.createCompactFormat());
        }
        
        // Return the writer
        
        return writer;
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
            
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_DISCOVERY, WebDAV.XML_NS_LOCK_DISCOVERY, nullAttr);  
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_ACTIVE_LOCK, WebDAV.XML_NS_ACTIVE_LOCK, nullAttr);
             
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_TYPE, WebDAV.XML_NS_LOCK_TYPE, nullAttr);
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_WRITE));
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_TYPE, WebDAV.XML_NS_LOCK_TYPE);
             
            // NOTE: We only do exclusive lock tokens at the moment
           
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_SCOPE, WebDAV.XML_NS_LOCK_SCOPE, nullAttr);
            xml.write(DocumentHelper.createElement(WebDAV.XML_NS_EXCLUSIVE));
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_SCOPE, WebDAV.XML_NS_LOCK_SCOPE);
             
            // NOTE: We only support one level of lock at the moment
           
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_DEPTH, WebDAV.XML_NS_DEPTH, nullAttr);
            xml.write(WebDAV.ZERO);
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_DEPTH, WebDAV.XML_NS_DEPTH);
             
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_OWNER, WebDAV.XML_NS_OWNER, nullAttr);
            xml.write(owner);
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_OWNER, WebDAV.XML_NS_OWNER);
             
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_TIMEOUT, WebDAV.XML_NS_TIMEOUT, nullAttr);

            // Output the expiry time
            
            String strTimeout = WebDAV.INFINITE;
            if (expiryDate != null)
            {
                  long timeoutRemaining = (expiryDate.getTime() - System.currentTimeMillis())/1000L;
              
                  strTimeout = WebDAV.SECOND + timeoutRemaining;
            }
            xml.write(strTimeout);
           
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_TIMEOUT, WebDAV.XML_NS_TIMEOUT);
             
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_TOKEN, WebDAV.XML_NS_LOCK_TOKEN, nullAttr);
            xml.startElement(WebDAV.DAV_NS, WebDAV.XML_HREF, WebDAV.XML_NS_HREF, nullAttr);
           
            xml.write(WebDAV.makeLockToken(lockNode, owner));
            
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_HREF, WebDAV.XML_NS_HREF);
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_TOKEN, WebDAV.XML_NS_LOCK_TOKEN);
           
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_ACTIVE_LOCK, WebDAV.XML_NS_ACTIVE_LOCK);
            xml.endElement(WebDAV.DAV_NS, WebDAV.XML_LOCK_DISCOVERY, WebDAV.XML_NS_LOCK_DISCOVERY);
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
