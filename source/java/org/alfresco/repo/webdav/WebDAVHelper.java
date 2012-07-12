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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.jlan.util.IPAddress;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.util.StringUtils;
import org.xml.sax.helpers.AttributesImpl;

/**
 * WebDAV Protocol Helper Class
 * 
 * <p>Provides helper methods for repository access using the WebDAV protocol.
 * 
 * @author GKSpencer
 */
public class WebDAVHelper
{
    // Constants
    private static final String HTTPS_SCHEME = "https://";
    private static final String HTTP_SCHEME = "http://";
    
    // Path seperator
    public static final String PathSeperator   = "/";
    public static final char PathSeperatorChar = '/';
    public static final String EMPTY_SITE_ID = "";
    
    // Logging
    private static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");
    
    // Service registry
    private ServiceRegistry m_serviceRegistry;

    // Services
    private NodeService m_nodeService;
    private FileFolderService m_fileFolderService;
    private SearchService m_searchService;
    private NamespaceService m_namespaceService;
    private DictionaryService m_dictionaryService;
    private MimetypeService m_mimetypeService;
    private WebDAVLockService m_lockService;
    private ActionService m_actionService;
    private AuthenticationService m_authService;
    private PermissionService m_permissionService;
    private TenantService m_tenantService;
    
    //  Empty XML attribute list
    
    private AttributesImpl m_nullAttribs = new AttributesImpl();
    private String m_urlPathPrefix;
    
    /**
     * Class constructor
     */
    protected WebDAVHelper(String urlPathPrefix, ServiceRegistry serviceRegistry, AuthenticationService authService, TenantService tenantService)
    {
        m_serviceRegistry = serviceRegistry;
        
        m_nodeService       = m_serviceRegistry.getNodeService();
        m_fileFolderService = m_serviceRegistry.getFileFolderService();
        m_searchService     = m_serviceRegistry.getSearchService();
        m_namespaceService  = m_serviceRegistry.getNamespaceService();
        m_dictionaryService = m_serviceRegistry.getDictionaryService();
        m_mimetypeService   = m_serviceRegistry.getMimetypeService();
        m_lockService       = (WebDAVLockService)m_serviceRegistry.getService(QName.createQName(NamespaceService.ALFRESCO_URI, WebDAVLockService.BEAN_NAME));
        m_actionService     = m_serviceRegistry.getActionService();
        m_permissionService = m_serviceRegistry.getPermissionService();
        m_tenantService     = tenantService;
        m_authService       = authService;
        m_urlPathPrefix     = urlPathPrefix;
    }
    
    /**
     * @return          Return the authentication service
     */
    public final AuthenticationService getAuthenticationService()
    {
        return m_authService;
    }
    
    /**
     * @return          Return the service registry
     */
    public final ServiceRegistry getServiceRegistry()
    {
        return m_serviceRegistry;
    }
    
    /**
     * @return          Return the node service
     */
    public final NodeService getNodeService()
    {
        return m_nodeService;
    }
    
    public FileFolderService getFileFolderService()
    {
        return m_fileFolderService;
    }

    /**
     * @return          Return the search service
     */
    public final SearchService getSearchService()
    {
        return m_searchService;
    }
    
    /**
     * @return          Return the namespace service
     */
    public final NamespaceService getNamespaceService()
    {
        return m_namespaceService;
    }
    
    /**
     * @return          Return the dictionary service
     */
    public final DictionaryService getDictionaryService()
    {
        return m_dictionaryService;
    }

    /**
     * @return          Return the mimetype service
     */
    public final MimetypeService getMimetypeService()
    {
        return m_mimetypeService;
    }
    
    /**
     * @return          Return the lock service
     */
    public final WebDAVLockService getLockService()
    {
        return m_lockService;
    }
    
    /**
     * @return          Return the action service
     */
    public final ActionService getActionService()
    {
        return m_actionService;
    }
    
    /**
     * 
     * @return          Return the permission service
     */
    public final PermissionService getPermissionService()
    {
        return m_permissionService;
    }
    
    /**
     * Retrieve the {@link TenantService} held by the helper.
     * 
     * @return TenantService
     */
    public TenantService getTenantService()
    {
        return m_tenantService;
    }
    
    /**
     * @return          Return the copy service
     */
    public final CopyService getCopyService()
    {
        return getServiceRegistry().getCopyService();
    }
    
    /**
     * Split the path into seperate directory path and file name strings.
     * If the path is not empty, then there will always be an entry for the filename
     * 
     * @param path Full path string.
     * @return Returns a String[2] with the folder path and file path.
     */
    public final String[] splitPath(String path)
    {
        if (path == null)
            throw new IllegalArgumentException("path may not be null");
        
        // Create an array of strings to hold the path and file name strings
        String[] pathStr = new String[] {"", ""};

        // Check if the path has a trailing seperator, if so then there is no file name.

        int pos = path.lastIndexOf(PathSeperatorChar);
        if (pos == -1 || pos == (path.length() - 1))
        {
            // Set the path string in the returned string array
            pathStr[1] = path;
        }
        else
        {
            pathStr[0] = path.substring(0, pos);
            pathStr[1] = path.substring(pos + 1);
        }
        // Return the path strings
        return pathStr;
    }
    
    /**
     * Split the path into all the component directories and filename
     * 
     * @param path          the string to split
     * @return              an array of all the path components
     */
    public final List<String> splitAllPaths(String path)
    {
        if (path == null || path.length() == 0)
        {
            return Collections.emptyList();
        }

        // split the path
        StringTokenizer token = new StringTokenizer(path, PathSeperator);
        List<String> results = new ArrayList<String>(10);
        while (token.hasMoreTokens())
        {
            results.add(token.nextToken());
        }
        return results;
    }

    protected String getURLForPath(HttpServletRequest request, String path, boolean isFolder, String userAgent)
    {
        return WebDAV.getURLForPath(request, getUrlPathPrefix(request), path, isFolder, userAgent);
    }    

    /**
     * Get the file info for the given paths
     * 
     * @param rootNodeRef       the acting webdav root
     * @param path              the path to search for
     * @param servletPath       the base servlet path, which may be null or empty
     * @return                  Return the file info for the path
     * @throws FileNotFoundException
     *                          if the path doesn't refer to a valid node
     */
    public final FileInfo getNodeForPath(NodeRef rootNodeRef, String path, String servletPath) throws FileNotFoundException
    {
        if (rootNodeRef == null)
        {
            throw new IllegalArgumentException("Root node may not be null");
        }
        else if (path == null)
        {
            throw new IllegalArgumentException("Path may not be null");
        }
        
        FileFolderService fileFolderService = getFileFolderService();
        // Check for the root path
        if ( path.length() == 0 || path.equals(PathSeperator) || EqualsHelper.nullSafeEquals(path, servletPath))
        {
            return fileFolderService.getFileInfo(rootNodeRef);
        }
        
        // Remove the servlet path from the path, assuming it hasn't already been done
        if (servletPath != null && servletPath.length() > 0)
        {
            // Need to ensure we don't strip /alfresco from a site of /alfresco_name/
            String comparePath = servletPath + "/";
            if (path.startsWith(comparePath))
            {
               // Strip the servlet path from the relative path
               path = path.substring(servletPath.length());
            }
        }
        
        // split the paths up
        List<String> splitPath = splitAllPaths(path);
        
        // find it
        FileInfo fileInfo = m_fileFolderService.resolveNamePath(rootNodeRef, splitPath);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Fetched node for path: \n" +
                    "   root: " + rootNodeRef + "\n" +
                    "   path: " + path + "\n" +
                    "   servlet path: " + servletPath + "\n" +
                    "   result: " + fileInfo);
        }
        return fileInfo;
    }
    
    public final FileInfo getParentNodeForPath(NodeRef rootNodeRef, String path, String servletPath) throws FileNotFoundException
    {
        if (rootNodeRef == null)
        {
            throw new IllegalArgumentException("Root node may not be null");
        }
        else if (path == null)
        {
            throw new IllegalArgumentException("Path may not be null");
        }
        // shorten the path
        String[] paths = splitPath(path);
        return getNodeForPath(rootNodeRef, paths[0], servletPath);
    }
    
    /**
     * Return the relative path for the node walking back to the specified root node
     * 
     * @param rootNodeRef the root below which the path will be valid
     * @param nodeRef the node's path to get
     * @return Returns string of form <b>/A/B/C</b> where C represents the from node and 
     */
    public final String getPathFromNode(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException
    {
        // Check if the nodes are valid, or equal
        if (rootNodeRef == null || nodeRef == null)
            throw new IllegalArgumentException("Invalid node(s) in getPathFromNode call");
        
        // short cut if the path node is the root node
        if (rootNodeRef.equals(nodeRef))
            return "";
        
        FileFolderService fileFolderService = getFileFolderService();
        
        // get the path elements
        List<FileInfo> pathInfos = fileFolderService.getNamePath(rootNodeRef, nodeRef);
        
        // build the path string
        StringBuilder sb = new StringBuilder(pathInfos.size() * 20);
        for (FileInfo fileInfo : pathInfos)
        {
            sb.append(WebDAVHelper.PathSeperatorChar);
            sb.append(fileInfo.getName());
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Build name path for node: \n" +
                    "   root: " + rootNodeRef + "\n" +
                    "   target: " + nodeRef + "\n" +
                    "   path: " + sb);
        }
        return sb.toString();
    }
    
    /**
     * Make an ETag value for a node using the GUID and modify date/time
     */
    public final String makeETag(FileInfo nodeInfo)
    {
        // Get the modify date/time property for the node
        
        StringBuilder etag = new StringBuilder();
        makeETagString(nodeInfo, etag);
        return etag.toString();
    }
    
    /**
     * Make an ETag value for a node using the GUID and modify date/time
     */
    public final String makeQuotedETag(FileInfo nodeInfo)
    {
        StringBuilder etag = new StringBuilder();
        
        etag.append("\"");
        makeETagString(nodeInfo, etag);
        etag.append("\"");
        return etag.toString();
    }
    
    /**
     * Make an ETag value for a node using the GUID and modify date/time
     */
    protected final void makeETagString(FileInfo nodeInfo, StringBuilder etag)
    {
        // Get the modify date/time property for the node
        
        Object modVal = nodeInfo.getProperties().get(ContentModel.PROP_MODIFIED);
        
        etag.append(nodeInfo.getNodeRef().getId());
        
        if ( modVal != null)
        {
            etag.append("_");
            etag.append(DefaultTypeConverter.INSTANCE.longValue(modVal));
        }
    }
    
    /**
     * @return              Return the null XML attribute list
     */
    public final AttributesImpl getNullAttributes()
    {
        return m_nullAttribs;
    }
    
    /**
     * Encodes the given string to valid URL format
     * 
     * @param s             the String to convert
     */
    public final static String encodeURL(String s)
    {
        return encodeURL(s, null);
    }
    
    public final static String encodeURL(String s, String userAgent)
    {
        try
        {
            if (userAgent != null && (userAgent.startsWith(WebDAV.AGENT_MICROSOFT_DATA_ACCESS_INTERNET_PUBLISHING_PROVIDER_DAV) 
                    || userAgent.contains(WebDAV.AGENT_INTERNET_EXPLORER)))
            {
                return encodeUrlReservedSymbols(s);
            }
            else
            {
                return URLEncoder.encode(s);
            }
        }
        catch (UnsupportedEncodingException err)
        {
            throw new RuntimeException(err);
        }
    }
    
    public final static String decodeURL(String s)
    {
        return URLDecoder.decode(s);
    }
    
    /**
     * Encodes the given string to valid HTML format
     * 
     * @param string        the String to convert
     */
    public final static String encodeHTML(String string)
    {
        if (string == null)
        {
            return "";
        }
        
        StringBuilder sb = null;      //create on demand
        String enc;
        char c;
        for (int i = 0; i < string.length(); i++)
        {
            enc = null;
            c = string.charAt(i);
            switch (c)
            {
                case '"': enc = "&quot;"; break;    //"
                case '&': enc = "&amp;"; break;     //&
                case '<': enc = "&lt;"; break;      //<
                case '>': enc = "&gt;"; break;      //>
                
                //misc
                case '\u20AC': enc = "&euro;";  break;
                case '\u00AB': enc = "&laquo;"; break;
                case '\u00BB': enc = "&raquo;"; break;
                case '\u00A0': enc = "&nbsp;"; break;
                
                default:
                    if (((int)c) >= 0x80)
                    {
                        //encode all non basic latin characters
                        enc = "&#" + ((int)c) + ";";
                    }
                break;
            }
            
            if (enc != null)
            {
                if (sb == null)
                {
                    String soFar = string.substring(0, i);
                    sb = new StringBuilder(i + 8);
                    sb.append(soFar);
                }
                sb.append(enc);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
        }
        
        if (sb == null)
        {
            return string;
        }
        else
        {
            return sb.toString();
        }
    }
    
    /**
     * ALF-5333: Microsoft clients use ISO-8859-1 to decode WebDAV responses
     * so this method should only be used for Microsoft user agents.
     * 
     * @param string
     * @return The encoded string for Microsoft clients
     * @throws UnsupportedEncodingException
     */
    public final static String encodeUrlReservedSymbols(String string) throws UnsupportedEncodingException
    {
        if (string == null)
        {
            return "";
        }
        
        StringBuilder sb = null;      // create on demand
        String enc;
        char c;
        for (int i = 0; i < string.length(); i++)
        {
            enc = null;
            c = string.charAt(i);
            switch (c)
            {
                // reserved
                case ';': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '/': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '?': enc = URLEncoder.encode(String.valueOf(c)); break;
                case ':': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '@': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '&': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '=': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '+': enc = URLEncoder.encode(String.valueOf(c)); break;
                
                // unsafe
                case '\"': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '#': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '%': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '>': enc = URLEncoder.encode(String.valueOf(c)); break;
                case '<': enc = URLEncoder.encode(String.valueOf(c)); break;
                default: break;
            }
            
            if (enc != null)
            {
                if (sb == null)
                {
                    String soFar = string.substring(0, i);
                    sb = new StringBuilder(i + 8);
                    sb.append(soFar);
                }
                sb.append(enc);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
        }
        
        if (sb == null)
        {
            return string;
        }
        else
        {
            return sb.toString();
        }
    }
    
    public String determineSiteId(WebDAVMethod method)
    {
        SiteService siteService = getServiceRegistry().getSiteService();
        String siteId;
        try
        {
            FileInfo fileInfo = getNodeForPath(method.getRootNodeRef(), method.getPath(), method.getServletPath());
            SiteInfo siteInfo = siteService.getSite(fileInfo.getNodeRef());
            if (siteInfo != null)
            {
                siteId = siteInfo.getShortName();
            }
            else
            {
                throw new RuntimeException("Node is not contained by a site: " + method.getPath());                
            }
        }
        catch (Exception error)
        {
            siteId = EMPTY_SITE_ID;
        }
        return siteId;
    }
    
    public String determineTenantDomain(WebDAVMethod method)
    {
        TenantService tenantService = getTenantService();
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain == null)
        {
            return TenantService.DEFAULT_DOMAIN;
        }
        return tenantDomain;
    }
    
    /**
     * Extract the destination path for MOVE or COPY commands from the
     * supplied destination URL header.
     * 
     * @param servletPath Path prefix of the WebDAV servlet.
     * @param destURL The Destination header.
     * @return The path to move/copy the file to.
     */
    public String getDestinationPath(String contextPath, String servletPath, String destURL)
    {
        if (destURL != null && destURL.length() > 0)
        {
            int offset = -1;

            if (destURL.startsWith(HTTP_SCHEME))
            {
                // Set the offset to the start of the host name
                offset = HTTP_SCHEME.length();
            }
            else if (destURL.startsWith(HTTPS_SCHEME))
            {
                // Set the offset to the start of the host name
                offset = HTTPS_SCHEME.length();
            }

            // Strip the start of the path if not a relative path

            if (offset != -1)
            {
                offset = destURL.indexOf(WebDAV.PathSeperator, offset);
                if (offset != -1)
                {
                    // Strip the host from the beginning
                    String strPath = destURL.substring(offset);
                    
                    // If it starts with /contextPath/servletPath/ (e.g. /alfresco/webdav/path/to/file) - then
                    // strip the servlet path from the start of the path.
                    String pathPrefix = contextPath + servletPath + WebDAV.PathSeperator;
                    if (strPath.startsWith(pathPrefix))
                    {
                        strPath = strPath.substring(pathPrefix.length());
                    }

                    return WebDAV.decodeURL(strPath);
                }
            }            
        }
        
        // Unable to get the path.
        return null;
    }
    
    /**
     * Check that the destination path is on this server and is a valid WebDAV
     * path for this server
     * 
     * @param request The request made against the WebDAV server.
     * @param urlStr String
     * @exception WebDAVServerException
     */
    public void checkDestinationURL(HttpServletRequest request, String urlStr) throws WebDAVServerException
    {
        try
        {
            // Parse the URL

            URL url = new URL(urlStr);

            // Check if the path is on this WebDAV server

            boolean localPath = true;

            if (url.getPort() != -1 && url.getPort() != request.getServerPort())
            {
                // Debug

                if (logger.isDebugEnabled())
                    logger.debug("Destination path, different server port");

                localPath = false;
            }
            else if (url.getHost().equalsIgnoreCase(request.getServerName()) == false
                    && url.getHost().equals(request.getLocalAddr()) == false)
            {
                // The target host may contain a domain or be specified as a numeric IP address
                
                String targetHost = url.getHost();
                
                if ( IPAddress.isNumericAddress( targetHost) == false)
                {
                    String localHost  = request.getServerName();
                    
                    int pos = targetHost.indexOf( ".");
                    if ( pos != -1)
                        targetHost = targetHost.substring( 0, pos);
                    
                    pos = localHost.indexOf( ".");
                    if ( pos != -1)
                        localHost = localHost.substring( 0, pos);
                    
                    // compare the host names
                    
                    if ( targetHost.equalsIgnoreCase( localHost) == false)
                        localPath = false;
                }
                else
                {
                    try
                    {
                        // Check if the target IP address is a local address
                        
                        InetAddress targetAddr = InetAddress.getByName( targetHost);
                        if ( NetworkInterface.getByInetAddress( targetAddr) == null)
                            localPath = false;
                    }
                    catch (Exception ex)
                    {
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Failed to check target IP address, " + targetHost);
                        
                        localPath = false;
                    }
                }
                
                // Debug

                if (localPath == false && logger.isDebugEnabled())
                {
                    logger.debug("Destination path, different server name/address");
                    logger.debug("  URL host=" + url.getHost() + ", ServerName=" + request.getServerName() + ", localAddr=" + request.getLocalAddr());
                }
            }
            else if (!url.getPath().startsWith(getUrlPathPrefix(request)))
            {
                // Debug

                if (logger.isDebugEnabled())
                    logger.debug("Destination path, different serlet path");

                localPath = false;
            }

            // If the URL does not refer to this WebDAV server throw an
            // exception

            if (localPath != true)
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_GATEWAY);
        }
        catch (MalformedURLException ex)
        {
            // Debug

            if (logger.isDebugEnabled())
                logger.debug("Bad destination path, " + urlStr);

            throw new WebDAVServerException(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }
    

    public void setUrlPathPrefix(String urlPathPrefix)
    {
        m_urlPathPrefix = urlPathPrefix;
    }
    
    public String getUrlPathPrefix(HttpServletRequest request)
    {
        StringBuilder urlStr = null;
        if (StringUtils.hasText(m_urlPathPrefix))
        {
            // A specific prefix has been configured in, so use it.
            urlStr = new StringBuilder(m_urlPathPrefix);
        }
        else
        {
            // Extract the path prefix from the request, using the servlet path as a guide.
            // e.g. "/preamble/servlet-mapping/folder/file.txt"
            // with a servlet path of "/servlet-mapping"
            // would result in a path prefix of "/preamble/servlet-mapping" being discovered.
            urlStr = new StringBuilder(request.getRequestURI());
            String servletPath = request.getServletPath();
            
            int rootPos = urlStr.indexOf(servletPath);
            if (rootPos != -1)
            {
                urlStr.setLength(rootPos + servletPath.length());
            }
        }
        
        // Ensure the prefix ends in the path separator.
        if (urlStr.charAt(urlStr.length() - 1) != PathSeperatorChar)
        {
            urlStr.append(PathSeperator);
        }
        
        return urlStr.toString();
    }
    
    public String getRepositoryPath(HttpServletRequest request)
    {
        // Try and get the path

        String strPath = null;
        
        try 
        {
            strPath = WebDAVHelper.decodeURL(request.getRequestURI());
        }
        catch (Exception ex) {}

        // Find the servlet path and trim from the request path
        
        String servletPath = request.getServletPath();
        
        int rootPos = strPath.indexOf(servletPath);
        if ( rootPos != -1)
        {
            strPath = strPath.substring( rootPos);
        }
        
        // If we failed to get the path from the request try and get the path from the servlet path

        if (strPath == null)
        {
            strPath = request.getServletPath();
        }

        if (strPath == null || strPath.length() == 0)
        {
            // If we still have not got a path then default to the root directory
            strPath = WebDAV.RootPath;
        }
        else if (strPath.startsWith(request.getServletPath()))
        {
            // Check if the path starts with the base servlet path
            int len = request.getServletPath().length();
            
            if (strPath.length() > len)
            {
                strPath = strPath.substring(len);
            }
            else
            {
                strPath = WebDAV.RootPath;
            }
        }

        // Make sure there are no trailing slashes
        
        if (strPath.length() > 1 && strPath.endsWith(WebDAV.PathSeperator))
        {
            strPath = strPath.substring(0, strPath.length() - 1);
        }

        // Return the path
        
        return strPath;
    }
}
