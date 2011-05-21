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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WebDAVModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.util.FileCopyUtils;
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
    
    // Mapping of User-Agent pattern to response status code
    // used to determine which status code should be returned for AccessDeniedException

    private static final Map<String, Integer> accessDeniedStatusCodes = new LinkedHashMap<String, Integer>();
    static
    {
        accessDeniedStatusCodes.put("(darwin)|(macintosh)", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        accessDeniedStatusCodes.put(".*", HttpServletResponse.SC_FORBIDDEN);
    }

    // Servlet request/response

    protected HttpServletRequest m_request;
    protected HttpServletResponse m_response;
    private File m_requestBody;
    private ServletInputStream m_inputStream;
    private BufferedReader m_reader;

    // WebDAV helper

    protected WebDAVHelper m_davHelper;

    // Root node

    protected NodeRef m_rootNodeRef;

    // Repository path

    protected String m_strPath = null;

    // User Agent
    
    protected String m_userAgent = null;

    // If header conditions 
    
    protected LinkedList<Condition> m_conditions = null;

    // If header resource-tag
    
    protected String m_resourceTag = null;

    // Depth header
    
    protected int m_depth = WebDAV.DEPTH_INFINITY;

    /**
     * Default constructor
     */
    public WebDAVMethod()
    {
    }

    /**
     * Set the request/response details
     * 
     * @param req
     *            HttpServletRequest
     * @param resp
     *            HttpServletResponse
     * @param registry
     *            ServiceRegistry
     * @param rootNode
     *            NodeRef
     */
    public void setDetails(final HttpServletRequest req, HttpServletResponse resp, WebDAVHelper davHelper,
            NodeRef rootNode)
    {
        // Wrap the request so that it is 'retryable'. Calls to getInputStream() and getReader() will result in the
        // request body being read into an intermediate file.
        this.m_request = new HttpServletRequestWrapper(req)
        {

            @Override
            public ServletInputStream getInputStream() throws IOException
            {
                if (WebDAVMethod.this.m_reader != null)
                {
                    throw new IllegalStateException("Reader in use");
                }
                if (WebDAVMethod.this.m_inputStream == null)
                {
                    final FileInputStream in = new FileInputStream(getRequestBodyAsFile(req));
                    WebDAVMethod.this.m_inputStream = new ServletInputStream()
                    {

                        @Override
                        public int read() throws IOException
                        {
                            return in.read();
                        }

                        @Override
                        public int read(byte b[]) throws IOException
                        {
                            return in.read(b);
                        }

                        @Override
                        public int read(byte b[], int off, int len) throws IOException
                        {
                            return in.read(b, off, len);
                        }

                        @Override
                        public long skip(long n) throws IOException
                        {
                            return in.skip(n);
                        }

                        @Override
                        public int available() throws IOException
                        {
                            return in.available();
                        }

                        @Override
                        public void close() throws IOException
                        {
                            in.close();
                        }

                        @Override
                        public void mark(int readlimit)
                        {
                            in.mark(readlimit);
                        }

                        @Override
                        public void reset() throws IOException
                        {
                            in.reset();
                        }

                        @Override
                        public boolean markSupported()
                        {
                            return in.markSupported();
                        }
                    };
                }

                return WebDAVMethod.this.m_inputStream;
            }

            @Override
            public BufferedReader getReader() throws IOException
            {
                if (WebDAVMethod.this.m_inputStream != null)
                {
                    throw new IllegalStateException("Input Stream in use");
                }
                if (WebDAVMethod.this.m_reader == null)
                {
                    String encoding = req.getCharacterEncoding();
                    WebDAVMethod.this.m_reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                            getRequestBodyAsFile(req)), encoding == null ? "ISO-8859-1" : encoding));
                }

                return WebDAVMethod.this.m_reader;
            }

        };
        this.m_response = resp;
        this.m_davHelper = davHelper;
        this.m_rootNodeRef = rootNode;

        this.m_strPath = WebDAV.getRepositoryPath(req);
    }

    private File getRequestBodyAsFile(HttpServletRequest req) throws IOException
    {
        if (this.m_requestBody == null)
        {
            this.m_requestBody = TempFileProvider.createTempFile("webdav_" + req.getMethod() + "_", ".bin");
            OutputStream out = new FileOutputStream(this.m_requestBody);
            int bytesRead = FileCopyUtils.copy(req.getInputStream(), out);
            
            // ALF-7377: check for corrupt request
            int contentLength = req.getIntHeader(WebDAV.HEADER_CONTENT_LENGTH);
            if (contentLength >= 0 && contentLength != bytesRead)
            {
                throw new IOException("Request body does not have specified Content Length");
            }
        }
        return this.m_requestBody;
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
     * Return the property find depth
     * 
     * @return int
     */
    public final int getDepth()
    {
        return m_depth;
    }

    /**
     * Executes the method, wrapping the call to {@link #executeImpl()} in an appropriate transaction
     * and handling the error conditions.
     */
    public void execute() throws WebDAVServerException
    {
        // Parse the HTTP headers
        parseRequestHeaders();

        // Parse the HTTP body
        parseRequestBody();
        
        m_userAgent = m_request.getHeader(WebDAV.HEADER_USER_AGENT);

        RetryingTransactionCallback<Object> executeImplCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Reset the request input stream / reader state
                WebDAVMethod.this.m_inputStream = null;
                WebDAVMethod.this.m_reader = null;

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
            throw new WebDAVServerException(getStatusForAccessDeniedException(), e);
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
        finally
        {
			// Remove temporary file if created
            if (this.m_requestBody != null)
            {
                try
                {
                    this.m_requestBody.delete();
					this.m_requestBody = null;
                }
                catch (Throwable t)
                {
                    WebDAVMethod.logger.error("Failed to delete temp file", t);
                }
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
                if (m_request.getCharacterEncoding() == null)
                {
                    // Let the XML parser work out the encoding if it is not explicitly declared in the HTTP header
                    body = builder.parse(new InputSource(m_request.getInputStream()));
                }
                else
                {
                    body = builder.parse(new InputSource(m_request.getReader()));
                }
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
     * Parses "Depth" request header
     * 
     * @throws WebDAVServerException
     */
    protected void parseDepthHeader() throws WebDAVServerException
    {
        // Store the Depth header as this is used by several WebDAV methods

        String strDepth = m_request.getHeader(WebDAV.HEADER_DEPTH);
        if (strDepth != null && strDepth.length() > 0)
        {
            if (strDepth.equals(WebDAV.ZERO))
            {
                m_depth = WebDAV.DEPTH_0;
            }
            else if (strDepth.equals(WebDAV.ONE))
            {
                m_depth = WebDAV.DEPTH_1;
            }
            else
            {
                m_depth = WebDAV.DEPTH_INFINITY;
            }
        }
    }

    /**
     * Parses "If" header of the request.
     * Stores conditions that should be checked.
     * Parses both No-tag-list and Tagged-list formats
     * See "10.4.2 Syntax" paragraph of the WebDAV specification for "If" header format.
     * 
     */
    protected void parseIfHeader() throws WebDAVServerException
    {
        //String strLockToken = null;

        String strIf = m_request.getHeader(WebDAV.HEADER_IF);

        if (logger.isDebugEnabled())
            logger.debug("Parsing If header: " + strIf);

        if (strIf != null && strIf.length() > 0)
        {
            if (strIf.startsWith("<"))
            {
                m_resourceTag = strIf.substring(1, strIf.indexOf(">"));
                strIf = strIf.substring(m_resourceTag.length() + 3);
            }

            m_conditions = new LinkedList<Condition>();
            String[] parts = strIf.split("\\) \\(");
            for (int i = 0; i < parts.length; i++)
            {

                String partString = parts[i].replaceAll("\\(", "").replaceAll("\\)", "");
                
                Condition c = new Condition();
                String[] conditions = partString.split(" ");

                for (int j = 0; j < conditions.length; j++)
                {
                    boolean fNot = false;
                    String eTag = null;
                    String lockToken = null;

                    if (WebDAV.HEADER_KEY_NOT.equals(conditions[j]))
                    {
                        // Check if Not keyword followed by State-token or entity-tag
                        if (j == (conditions.length - 1))
                        {
                            throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
                        }
                        fNot = true;
                        j++;
                    }
                
                    // read State-token
                    int index = conditions[j].indexOf('<');
                    if (index != -1)
                {
                    try
                    {
                            String s = conditions[j].substring(index + 1, conditions[j].indexOf(">"));
                            if (!s.startsWith(WebDAV.OPAQUE_LOCK_TOKEN))
                            {
                               if(!fNot)
                    {
                                   throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
                    }
                }
                else
                {
                                lockToken = s;
                                c.addLockTocken(lockToken, fNot);
                }
                        }
                        catch (IndexOutOfBoundsException e)
                {
                            throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
                }
            }

                    // read entity-tag
                    index = conditions[j].indexOf("[\"");
                    if (index != -1)
            {
                        // TODO: implement parsing of weak ETags: W/"123..".
                        eTag = conditions[j].substring(index + 1, conditions[j].indexOf("]"));
                        c.addETag(eTag, fNot);
            }

                }
                m_conditions.add(c);
            }
        }
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
     * Convenience method to return the search service
     * 
     * @return SearchService
     */
    protected final SearchService getSearchService()
    {
        return m_davHelper.getSearchService();
    }

    /**
     * Convenience method to return the namespace service
     * 
     * @return NamespaceService
     */
    protected final NamespaceService getNamespaceService()
    {
        return m_davHelper.getNamespaceService();
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
     * Convenience method to return the permission service
     * 
     * @return PermissionService
     */
    protected final PermissionService getPermissionService()
    {
        return m_davHelper.getPermissionService();
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
    protected String getPath()
    {
        return m_strPath;
    }

    /**
     * Create an XML writer for the response
     * 
     * @return XMLWriter
     * @exception IOException
     */
    protected XMLWriter createXMLWriter() throws IOException
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
    protected void generateLockDiscoveryXML(XMLWriter xml, NodeRef lockNode, LockInfo lockInfo) throws Exception
    {
	    String owner = (String) getNodeService().getProperty(lockNode, ContentModel.PROP_LOCK_OWNER);
	    generateLockDiscoveryXML(xml, lockNode, false, lockInfo.getScope(), lockInfo.getDepth(), WebDAV.makeLockToken(lockNode, owner), owner);
	}
  
    /**
     * Generates the lock discovery XML response
     * 
     * @param xml XMLWriter
     * @param lockNode NodeRef
     * @param emptyNamespace boolean True if namespace should be empty. Used to avoid bugs in WebDAV clients.
     * @param scope String lock scope
     * @param depth String lock depth
     * @param lToken String locktoken
     * @param owner String lock owner
     * 
     */
    protected void generateLockDiscoveryXML(XMLWriter xml, NodeRef lockNode, boolean emptyNamespace, String scope, String depth, String lToken, String owner) throws Exception
    {
        Attributes nullAttr= getDAVHelper().getNullAttributes();
        String ns = emptyNamespace ? "" : WebDAV.DAV_NS;
        if (lockNode != null)
        {
            
            // Get the lock details

            NodeService nodeService = getNodeService();
            
            Date expiryDate = (Date) nodeService.getProperty(lockNode, ContentModel.PROP_EXPIRY_DATE);
            
            // Output the XML response
            
            xml.startElement(ns, WebDAV.XML_LOCK_DISCOVERY, emptyNamespace ? WebDAV.XML_LOCK_DISCOVERY : WebDAV.XML_NS_LOCK_DISCOVERY, nullAttr);  
            xml.startElement(ns, WebDAV.XML_ACTIVE_LOCK, emptyNamespace ? WebDAV.XML_ACTIVE_LOCK : WebDAV.XML_NS_ACTIVE_LOCK, nullAttr);
             
            xml.startElement(ns, WebDAV.XML_LOCK_TYPE, emptyNamespace ? WebDAV.XML_LOCK_TYPE : WebDAV.XML_NS_LOCK_TYPE, nullAttr);
            xml.write(DocumentHelper.createElement(emptyNamespace ? WebDAV.XML_WRITE : WebDAV.XML_NS_WRITE));
            xml.endElement(ns, WebDAV.XML_LOCK_TYPE, emptyNamespace ? WebDAV.XML_LOCK_TYPE : WebDAV.XML_NS_LOCK_TYPE);
             
            xml.startElement(ns, WebDAV.XML_LOCK_SCOPE, emptyNamespace ? WebDAV.XML_LOCK_SCOPE : WebDAV.XML_NS_LOCK_SCOPE, nullAttr);
            xml.write(DocumentHelper.createElement(emptyNamespace ? scope : WebDAV.DAV_NS_PREFIX + scope));
            xml.endElement(ns, WebDAV.XML_LOCK_SCOPE, emptyNamespace ? WebDAV.XML_LOCK_SCOPE : WebDAV.XML_NS_LOCK_SCOPE);
             
            // NOTE: We only support one level of lock at the moment
           
            xml.startElement(ns, WebDAV.XML_DEPTH, emptyNamespace ? WebDAV.XML_DEPTH : WebDAV.XML_NS_DEPTH, nullAttr);
            xml.write(depth);
            xml.endElement(ns, WebDAV.XML_DEPTH, emptyNamespace ? WebDAV.XML_DEPTH : WebDAV.XML_NS_DEPTH);
             
            xml.startElement(ns, WebDAV.XML_OWNER, emptyNamespace ? WebDAV.XML_OWNER : WebDAV.XML_NS_OWNER, nullAttr);
            xml.write(owner);
            xml.endElement(ns, WebDAV.XML_OWNER, emptyNamespace ? WebDAV.XML_OWNER : WebDAV.XML_NS_OWNER);
             
            xml.startElement(ns, WebDAV.XML_TIMEOUT, emptyNamespace ? WebDAV.XML_TIMEOUT : WebDAV.XML_NS_TIMEOUT, nullAttr);

            // Output the expiry time
            
            String strTimeout = WebDAV.INFINITE;
            if (expiryDate != null)
            {
                  long timeoutRemaining = (expiryDate.getTime() - System.currentTimeMillis())/1000L;
              
                  strTimeout = WebDAV.SECOND + timeoutRemaining;
            }
            xml.write(strTimeout);
           
            xml.endElement(ns, WebDAV.XML_TIMEOUT, emptyNamespace ? WebDAV.XML_TIMEOUT : WebDAV.XML_NS_TIMEOUT);
             
            xml.startElement(ns, WebDAV.XML_LOCK_TOKEN, emptyNamespace ? WebDAV.XML_LOCK_TOKEN : WebDAV.XML_NS_LOCK_TOKEN, nullAttr);
            xml.startElement(ns, WebDAV.XML_HREF, emptyNamespace ? WebDAV.XML_HREF : WebDAV.XML_NS_HREF, nullAttr);
           
            xml.write(lToken);
            
            xml.endElement(ns, WebDAV.XML_HREF, emptyNamespace ? WebDAV.XML_HREF : WebDAV.XML_NS_HREF);
            xml.endElement(ns, WebDAV.XML_LOCK_TOKEN, emptyNamespace ? WebDAV.XML_LOCK_TOKEN : WebDAV.XML_NS_LOCK_TOKEN);
           
            xml.endElement(ns, WebDAV.XML_ACTIVE_LOCK, emptyNamespace ? WebDAV.XML_ACTIVE_LOCK : WebDAV.XML_NS_ACTIVE_LOCK);
            xml.endElement(ns, WebDAV.XML_LOCK_DISCOVERY, emptyNamespace ? WebDAV.XML_LOCK_DISCOVERY : WebDAV.XML_NS_LOCK_DISCOVERY);
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
                ns.append(strNamespaceUri == null ? "" : strNamespaceUri).append("\" ");
            }
        }
        
        return ns.toString();
    }
    /**
     * Checks if write operation can be performed on node.
     * 
     * @param fileInfo          - node's file info
     * @param ignoreShared      - if true ignores shared locks
     * @param lockMethod        - must be true if used from lock method
     * @return node's lock info
     * @throws WebDAVServerException if node has shared or exclusive lock
     *                               or If header preconditions failed
     */
    protected LockInfo checkNode(FileInfo fileInfo, boolean ignoreShared, boolean lockMethod) throws WebDAVServerException
    {
        LockInfo nodeLockInfo = getNodeLockInfo(fileInfo.getNodeRef());
        String nodeETag = getDAVHelper().makeQuotedETag(fileInfo.getNodeRef());

        
        if (m_conditions == null)
        {
            if (nodeLockInfo.getToken() == null)
            {
                CheckOutCheckInService checkOutCheckInService = m_davHelper.getServiceRegistry().getCheckOutCheckInService();
                if (nodeLockInfo.getSharedLockTokens() == null && checkOutCheckInService.getWorkingCopy(fileInfo.getNodeRef()) == null)
                {
                    return nodeLockInfo;
                }
                if (!ignoreShared)
                {
                    throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
                }
            }
            else
            {
                throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
            }
        }

        // Checking of the If tag consists of two checks:
        // 1. If the node is locked we need to check it's Lock token independently of conditions check result.
        //    For example "(<wrong token>) (Not <DAV:no-lock>)" if always true,
        //    but request must fail with 423 Locked response because node is locked.
        // 2. Check if ANY of the conditions in If header true.
        checkLockToken(nodeLockInfo, ignoreShared, lockMethod);
        checkConditions(nodeLockInfo.getToken(), nodeETag);
        
        return nodeLockInfo;
    }

    /**
     * Checks if write operation can be performed on node.
     * 
     * @param fileInfo
     * @return
     * @throws WebDAVServerException if node has shared or exclusive lock
     *                               or If header preconditions failed
     */
    protected LockInfo checkNode(FileInfo fileInfo) throws WebDAVServerException
    {
        return checkNode(fileInfo, false, true);
    }

    /**
     * Checks if node can be accessed with WebDAV operation
     * 
     * @param nodeLockToken      - token to check
     * @param lockInfo           - node's lock info
     * @param ignoreShared       - if true - ignores shared lock tokens 
     * @param lockMethod         - must be true if used from lock method
     * @throws WebDAVServerException if node has no appropriate lock token
     */
    private void checkLockToken(LockInfo lockInfo, boolean ignoreShared, boolean lockMethod) throws WebDAVServerException
    {
        String nodeLockToken = lockInfo.getToken();
        Set<String> sharedLockTokens = lockInfo.getSharedLockTokens();
        
        if (m_conditions != null)
        {
            // Request has conditions to check
            if (lockInfo.isShared())
            {
                // Node has shared lock. Check if conditions contains lock token of the node.
                // If not throw exception
                if (sharedLockTokens != null)
                {
                    if (!ignoreShared)
                    {
                        for (Condition condition : m_conditions)
                        {
                            for (String sharedLockToken : sharedLockTokens)
                            {
                                if (condition.getLockTokensMatch().contains(sharedLockToken))
                                {
                                    return;
                                }
                            }
                        }
                        throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
                    }
                    return;
                }
            }
            else
            {
                // Node has exclusive lock. Check if conditions contains lock token of the node
                // If not throw exception
                for (Condition condition : m_conditions)
                {
                    if (nodeLockToken != null)
                    {
                        if (condition.getLockTokensMatch().contains(nodeLockToken))
                        {
                            return;
                        }
                    }
                }
                throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
            }
        }
        else
        {
            // Request has no conditions
            if (lockInfo.isShared())
            {
                // If lock is shared and check was called not from LOCK method return
                if (!lockMethod)
                {
                    return;
                }
                // Throw exception - we can't set lock on node with shared lock
                throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
            }
        }
        
        throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
    }


    /**
     * Checks If header conditions. Throws WebDAVServerException with 412(Precondition failed)
     * if none of the conditions success.
     * 
     * @param nodeLockToken          - node's lock token
     * @param nodeETag               - node's ETag
     * @throws WebDAVServerException if conditions fail
     */
    private void checkConditions(String nodeLockToken, String nodeETag) throws WebDAVServerException
    {
        // Checks If header conditions.
        // Each condition can contain check of ETag and check of Lock token.

        if (m_conditions == null)
        {
            // No conditions were provided with "If" request header, so check successful
            return;
        }
        
        // Check the list of "If" header's conditions.
        // If any condition conforms then check is successful
        for (Condition condition : m_conditions)
        {
            // Flag for ETag conditions
            boolean fMatchETag = true;
            // Flag for Lock token conditions
            boolean fMatchLockToken = true;

            // Check ETags that should match
            if (condition.getETagsMatch() != null)
            {
                fMatchETag = condition.getETagsMatch().contains(nodeETag) ? true : false;
            }
            // Check ETags that shouldn't match
            if (condition.getETagsNotMatch() != null)
            {
                fMatchETag = condition.getETagsNotMatch().contains(nodeETag) ? false : true;
            }
            // Check lock tokens that should match
            if (condition.getLockTokensMatch() != null)
            {
                fMatchLockToken = condition.getLockTokensMatch().contains(nodeLockToken) ? true : false;
            }
            // Check lock tokens that shouldn't match
            if (condition.getLockTokensNotMatch() != null)
            {
                fMatchLockToken = condition.getLockTokensNotMatch().contains(nodeLockToken) ? false : true;
            }

            if (fMatchETag && fMatchLockToken)
            {
                // Condition conforms
                return;
            }
        }

        // None of the conditions successful
        throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
    }
    
    
    /**
     * Returns node Lock token in consideration of WebDav lock depth. 
     * 
     * @param fileInfo node
     * @return String Lock token
     */
    protected LockInfo getNodeLockInfo(NodeRef nodeRef)
    {
        LockInfo lockInfo = new LockInfo();
        NodeService nodeService = getNodeService();
        LockService lockService = getLockService();

        // Check if node is locked directly.
        LockStatus lockSts = lockService.getLockStatus(nodeRef);
        if (lockSts == LockStatus.LOCKED || lockSts == LockStatus.LOCK_OWNER)
        {
            String propOpaqueLockToken = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_OPAQUE_LOCK_TOKEN);
            if (propOpaqueLockToken != null)
            {
                // Get lock depth
                String depth = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_DEPTH);
                //Get lock scope
                String scope = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_SCOPE);
                // Get shared lock tokens
                String sharedLocks = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_SHARED_LOCK_TOKENS);

                // Node has it's own Lock token.
                // Store lock information to the lockInfo object
                lockInfo.setToken(propOpaqueLockToken);
                lockInfo.setDepth(depth);
                lockInfo.setScope(scope);
                lockInfo.setSharedLockTokens(LockInfo.parseSharedLockTokens(sharedLocks));
                
                return lockInfo;
            }
        }
        else
        {
            // No has no exclusive lock but can be locked with shared lock
            String sharedLocks = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_SHARED_LOCK_TOKENS);
            if (sharedLocks != null)
            {
                // Get lock depth
                String depth = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_DEPTH);
                //Get lock scope
                String scope = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_SCOPE);

                // Node has it's own Lock token.
                // Store lock information to the lockInfo object
                lockInfo.setDepth(depth);
                lockInfo.setScope(scope);
                lockInfo.setSharedLockTokens(LockInfo.parseSharedLockTokens(sharedLocks));
                lockInfo.setShared(true);

                return lockInfo;
            }
        }


        // Node isn't locked directly and has no it's own  Lock token.
        // Try to search indirect lock.
        NodeRef node = nodeRef;
        while (true)
        {
            List<ChildAssociationRef> assocs = nodeService.getParentAssocs(node, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            if (assocs.isEmpty())
            {
                // Node has no lock and Lock token
                return new LockInfo();
            }
            NodeRef parent = assocs.get(0).getParentRef();

            lockSts = lockService.getLockStatus(parent);
            if (lockSts == LockStatus.LOCKED || lockSts == LockStatus.LOCK_OWNER)
            {
                // Check node lock depth.
                // If depth is WebDAV.INFINITY then return this node's Lock token.
                String depth = (String) nodeService.getProperty(parent, WebDAVModel.PROP_LOCK_DEPTH);
                if (WebDAV.INFINITY.equals(depth))
                {
                    // In this case node is locked indirectly.

                    //Get lock scope
                    String scope = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_SCOPE);
                    // Get shared lock tokens
                    String sharedLocks = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_SHARED_LOCK_TOKENS);

                    // Store lock information to the lockInfo object
                    
                    // Get lock token of the locked node - this is indirect lock token.
                    String propOpaqueLockToken = (String) nodeService.getProperty(parent, WebDAVModel.PROP_OPAQUE_LOCK_TOKEN);
                    lockInfo.setToken(propOpaqueLockToken);
                    lockInfo.setDepth(depth);
                    lockInfo.setScope(scope);
                    lockInfo.setSharedLockTokens(LockInfo.parseSharedLockTokens(sharedLocks));

                    return lockInfo;
                }
            }
            else
            {
                // No has no exclusive lock but can be locked with shared lock
                String sharedLocks = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_SHARED_LOCK_TOKENS);
                if (sharedLocks != null)
                {
                    // Check node lock depth.
                    // If depth is WebDAV.INFINITY then return this node's Lock token.
                    String depth = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_DEPTH);
                    if (WebDAV.INFINITY.equals(depth))
                    {
                        // In this case node is locked indirectly.

                        //Get lock scope
                        String scope = (String) nodeService.getProperty(nodeRef, WebDAVModel.PROP_LOCK_SCOPE);
    
                        // Node has it's own Lock token.
                        lockInfo.setDepth(depth);
                        lockInfo.setScope(scope);
                        lockInfo.setSharedLockTokens(LockInfo.parseSharedLockTokens(sharedLocks));

                        lockInfo.setShared(true);

                        return lockInfo;
                    }
                }
            }
            
            node = parent;
        }
    }
    
    /**
     * Get the file info for the given paths
     * 
     * @param rootNodeRef the acting webdav root
     * @param path the path to search for
     * @param servletPath the base servlet path, which may be null or empty
     * @return Return the file info for the path
     * @throws FileNotFoundException if the path doesn't refer to a valid node
     */
    protected FileInfo getNodeForPath(NodeRef rootNodeRef, String path, String servletPath) throws FileNotFoundException
    {
        return getDAVHelper().getNodeForPath(rootNodeRef, path, servletPath);
    }

    /**
     * Returns a URL that could be used to access the given path.
     * 
     * @param request HttpServletRequest
     * @param path the path to search for
     * @param isFolder indicates file or folder is requested
     * @return URL that could be used to access the given path
     */
    protected String getURLForPath(HttpServletRequest request, String path, boolean isFolder)
    {
        return WebDAV.getURLForPath(request, path, isFolder, m_userAgent);
    }

    /**
     * Flushs a XML Writer.
     * 
     * @param xml XMLWriter that should be flushed
     */
    protected void flushXML(XMLWriter xml) throws IOException
    {
        xml.flush();
    }

    /**
     * Returns a working copy of node for current user.
     * 
     * @param nodeRef node reference
     * @return Returns the working copy's file information
     */
    protected FileInfo getWorkingCopy(NodeRef nodeRef)
    {
        FileInfo result = null;
        NodeRef workingCopy = getServiceRegistry().getCheckOutCheckInService().getWorkingCopy(nodeRef);
        if (workingCopy != null)
        {
            String workingCopyOwner = getNodeService().getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER).toString();
            if (workingCopyOwner.equals(getAuthenticationService().getCurrentUserName()))
            {
                result = getFileFolderService().getFileInfo(workingCopy);
            }
        }
        return result;
    }

    /**
     * Determines status code for AccessDeniedException based on client's HTTP headers.
     * 
     * @return Returns status code
     */
    protected int getStatusForAccessDeniedException()
    {
        if (m_request != null && m_request.getHeader(WebDAV.HEADER_USER_AGENT) != null)
        {
            String userAgent = m_request.getHeader(WebDAV.HEADER_USER_AGENT).toLowerCase();

            for (Entry<String, Integer> entry : accessDeniedStatusCodes.entrySet())
            {
                if (Pattern.compile(entry.getKey()).matcher(userAgent).find())
                {
                    return entry.getValue();
                }
            }
        }
        return HttpServletResponse.SC_UNAUTHORIZED;
    }

    /**
     * Class used for storing conditions which comes with "If" header of the request
     * 
     * @author ivanry
     */
    protected class Condition
    {
        // These tokens will be checked on equivalence against node's lock token
        private LinkedList<String> lockTokensMatch = new LinkedList<String>();

        // These tokens will be checked on non-equivalence against node's lock token
        private LinkedList<String> lockTokensNotMatch = new LinkedList<String>();

        // These ETags will be checked on equivalence against node's ETags
        private LinkedList<String> eTagsMatch;

        // These ETags will be checked on non-equivalence against node's ETags
        private LinkedList<String> eTagsNotMatch;
        
        /**
         * Default constructor
         * 
         */
        public Condition()
        {
        }
        
        /**
         * Returns the list of lock tokens that should be checked against node's lock token on equivalence.
         * 
         * @return lock tokens
         */
        public LinkedList<String> getLockTokensMatch()
        {
            return this.lockTokensMatch;
        }

        /**
         * Returns the list of lock tokens that should be checked against node's lock token on non-equivalence.
         * 
         * @return lock tokens
         */
        public LinkedList<String> getLockTokensNotMatch()
        {
            return this.lockTokensNotMatch;
        }

        /**
         * Returns the list of ETags that should be checked against node's ETag on equivalence.
         * 
         * @return ETags list
         */
        public LinkedList<String> getETagsMatch()
        {
            return this.eTagsMatch;
        }

        /**
         * Returns the list of ETags that should be checked against node's ETag on non-equivalence.
         * 
         * @return ETags list
         */
        public LinkedList<String> getETagsNotMatch()
        {
            return this.eTagsNotMatch;
        }

        /**
         * Adds lock token to check
         * 
         * @param lockToken String
         * @param notMatch true is lock token should be added to the list matched tokens. 
         *                 false if should be added to the list of non-matches. 
         */
        public void addLockTocken(String lockToken, boolean notMatch)
        {
            if (notMatch)
            {
                this.lockTokensNotMatch.add(lockToken);
            }
            else
            {
                this.lockTokensMatch.add(lockToken);
            }
        }

        /**
         * Add ETag to check
         * 
         * @param eTag String
         * @param notMatch true is ETag should be added to the list matched ETags. 
         *                 false if should be added to the list of non-matches.
         */
        public void addETag(String eTag, boolean notMatch)
        {
            if (notMatch)
            {
                if (eTagsNotMatch == null)
                {
                    eTagsNotMatch = new LinkedList<String>();
                }
                this.eTagsNotMatch.add(eTag);
            }
            else
            {
                if (eTagsMatch == null)
                {
                    eTagsMatch = new LinkedList<String>();
                }
                this.eTagsMatch.add(eTag);
            }
        }
    }

        
        
        
    
    
}
