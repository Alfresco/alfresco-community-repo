/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.jlan.util.IPAddress;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.LockUtils;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
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
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.EqualsHelper;
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
    public static final String BEAN_NAME = "webDAVHelper";

    private static final String HTTPS_SCHEME = "https://";
    private static final String HTTP_SCHEME = "http://";
    

    // Path seperator
    public static final String PathSeperator   = "/";
    public static final char PathSeperatorChar = '/';
    public static final String EMPTY_SITE_ID = "";
    
    // Logging
    protected static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");
    
    // Service registry TODO: eliminate this - not dependency injection!
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
    private HiddenAspect m_hiddenAspect;
    
    // pattern is tested against full path after it has been lower cased.
    private Pattern m_renameShufflePattern = Pattern.compile("(.*/\\..*)|(.*[a-f0-9]{8}+$)|(.*\\.tmp$)|(.*\\.wbk$)|(.*\\.bak$)|(.*\\~$)|(.*backup.*\\.do[ct]{1}[x]?[m]?$)");
    
    //  Empty XML attribute list
    
    private final AttributesImpl m_nullAttribs = new AttributesImpl();
        
    private BehaviourFilter m_policyBehaviourFilter;

    private String m_urlPathPrefix;
        
    /**
     * Set the regular expression that will be applied to filenames during renames
     * to detect whether clients are performing a renaming shuffle - common during
     * file saving on various clients.
     * <p/>
     * <bALF-3856, ALF-7079, MNT-181</b>
     * 
     * @param renameShufflePattern      a regular expression filename match
     */
    public void setRenameShufflePattern(Pattern renameShufflePattern)
    {
        this.m_renameShufflePattern = renameShufflePattern;
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
    public ServiceRegistry getServiceRegistry()
    {
        // TODO: eliminate this - not dependency injection!
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
    public WebDAVLockService getLockService()
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
     * @return the hidden aspect bean
     */
    public final HiddenAspect getHiddenAspect()
    {
        return m_hiddenAspect;
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
    
    public void setTenantService(TenantService tenantService)
    {
        this.m_tenantService = tenantService;
    }
    
    /**
     * @param serviceRegistry the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.m_serviceRegistry = serviceRegistry;
    }

    /**
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.m_nodeService = nodeService;
    }

    /**
     * @param fileFolderService the fileFolder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.m_fileFolderService = fileFolderService;
    }

    /**
     * @param searchService the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.m_searchService = searchService;
    }

    /**
     * @param namespaceService the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.m_namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.m_dictionaryService = dictionaryService;
    }

    /**
     * @param mimetypeService the mimetype service
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.m_mimetypeService = mimetypeService;
    }

    /**
     * @param lockService the lock service
     */
    public void setLockService(WebDAVLockService lockService)
    {
        this.m_lockService = lockService;
    }

    /**
     * @param actionService the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.m_actionService = actionService;
    }

    /**
     * @param authService the authentication service
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        this.m_authService = authService;
    }

    /**
     * @param permissionService the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.m_permissionService = permissionService;
    }

    /**
     * @param hiddenAspect the hiddenAspect to set
     */
    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.m_hiddenAspect = hiddenAspect;
    }

    
    public BehaviourFilter getPolicyBehaviourFilter()
    {
        return m_policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        m_policyBehaviourFilter = behaviourFilter;
    }

    /**
     * Checks a new path in a move operation to detect whether clients are starting a renaming shuffle - common during
     * file saving on various clients.
     * <p/>
     * <b>ALF-3856, ALF-7079, MNT-181</b>
     */
    public boolean isRenameShuffle(String newPath)
    {
        return m_renameShufflePattern.matcher(newPath.toLowerCase()).matches();
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
    public List<String> splitAllPaths(String path)
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

    public String getURLForPath(HttpServletRequest request, String path, boolean isCollection)
    {
        return getURLForPath(request, path, isCollection, null);
    }
    
    public String getURLForPath(HttpServletRequest request, String path, boolean isCollection, String userAgent)
    {
        String urlPathPrefix = getUrlPathPrefix(request);
        StringBuilder urlStr = new StringBuilder(urlPathPrefix);
        
        if (path.equals(WebDAV.RootPath) == false)
        {
            // split the path and URL encode each path element
            for (StringTokenizer t = new StringTokenizer(path, PathSeperator); t.hasMoreTokens(); /**/)
            {
                urlStr.append( WebDAVHelper.encodeURL(t.nextToken(), userAgent) );
                if (t.hasMoreTokens())
                {
                    urlStr.append(PathSeperator);
                }
            }
        }
        
        // If the URL is to a collection add a trailing slash
        if (isCollection && urlStr.charAt( urlStr.length() - 1) != PathSeperatorChar)
        {
            urlStr.append( PathSeperator);
        }
        
        logger.debug("getURLForPath() path:" + path + " => url:" + urlStr);
        
        // Return the URL string
        return urlStr.toString();
    }    

    /**
     * Get the file info for the given paths
     * 
     * @param rootNodeRef       the acting webdav root
     * @param path              the path to search for
     * @return                  Return the file info for the path
     * @throws FileNotFoundException
     *                          if the path doesn't refer to a valid node
     */
    public FileInfo getNodeForPath(NodeRef rootNodeRef, String path) throws FileNotFoundException
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
        if ( path.length() == 0 || path.equals(PathSeperator))
        {
            return fileFolderService.getFileInfo(rootNodeRef);
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
                    "   result: " + fileInfo);
        }
        return fileInfo;
    }
    
    public boolean isRootPath(String path, String servletPath)
    {
        // Check for the root path
        return( path.length() == 0 || path.equals(PathSeperator) || EqualsHelper.nullSafeEquals(path, servletPath));
    }
    
    public final FileInfo getParentNodeForPath(NodeRef rootNodeRef, String path) throws FileNotFoundException
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
        return getNodeForPath(rootNodeRef, paths[0]);
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
        List<String> pathInfos = fileFolderService.getNameOnlyPath(rootNodeRef, nodeRef);
        
        // build the path string
        StringBuilder sb = new StringBuilder(pathInfos.size() * 20);
        for (String fileInfo : pathInfos)
        {
            sb.append(WebDAVHelper.PathSeperatorChar);
            sb.append(fileInfo);
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
        
    public FileInfo createFile(FileInfo parentNodeInfo, String path) throws WebDAVServerException
    {
        return m_fileFolderService.create(parentNodeInfo.getNodeRef(), path, ContentModel.TYPE_CONTENT);
    }

    public List<FileInfo> getChildren(FileInfo fileInfo) throws WebDAVServerException
    {
        return m_fileFolderService.list(fileInfo.getNodeRef());
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
          return URLEncoder.encode(s);
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
        return determineSiteId(method.getRootNodeRef(), method.getPath());
    }
    
    public String determineSiteId(NodeRef rootNodeRef, String path)
    {
        SiteService siteService = getServiceRegistry().getSiteService();
        String siteId;
        try
        {
            FileInfo fileInfo = getNodeForPath(rootNodeRef, path);
            siteId = siteService.getSiteShortName(fileInfo.getNodeRef());
            if (siteId == null)
            {
                throw new RuntimeException("Node is not contained by a site: " + path);
            }
        }
        catch (Exception error)
        {
            siteId = EMPTY_SITE_ID;
        }
        return siteId;
    }
    
    @Deprecated
    public String determineTenantDomain(WebDAVMethod method)
    {
        return determineTenantDomain();
    }
    
    public String determineTenantDomain()
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
        if (urlStr.length() == 0 || urlStr.charAt(urlStr.length() - 1) != PathSeperatorChar)
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

    /**
     * Indicates if the node is unlocked or the current user has a WRITE_LOCK<p>
     * 
     * @see LockService#isLockedAndReadOnly(NodeRef)
     * 
     * @param nodeRef    the node reference
     */
    public boolean isLockedAndReadOnly(final NodeRef nodeRef)
    {
        return LockUtils.isLockedAndReadOnly(nodeRef, m_serviceRegistry.getLockService());
    }

    /**
     * Indicates if the node is locked and the current user is not lock owner<p>
     *
     * @param nodeRef    the node reference
     */
    public boolean isLockedAndNotLockOwner(final NodeRef nodeRef)
    {
        return LockUtils.isLockedAndNotLockOwner(nodeRef, m_serviceRegistry.getLockService());
    }
}
