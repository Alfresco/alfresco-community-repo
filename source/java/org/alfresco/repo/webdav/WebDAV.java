/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class used by the WebDAV protocol handling classes
 * 
 * @author gavinc
 */
public class WebDAV
{
    // Logging

    private static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");
    
    // WebDAV XML namespace
    
    public static final String DAV_NS   = "D";
    public static final String DAV_NS_PREFIX = DAV_NS + ":";
    
    // PROPFIND, LOCK depth
    
    public static final int DEPTH_0 = 0;
    public static final int DEPTH_1 = 1;
    public static final int DEPTH_INFINITY = -1;
    public static final short TIMEOUT_INFINITY = -1;

    // WebDAV HTTP response codes
    
    public static final int WEBDAV_SC_MULTI_STATUS = 207;
    public static final int WEBDAV_SC_LOCKED = 423;
    public static final int WEBDAV_SC_FAILED_DEPENDENCY = 424;

    // WebDAV HTTP response code descriptions

    public static final String WEBDAV_SC_FAILED_DEPENDENCY_DESC = "Failed Dependency";

    // HTTP response code descriptions
    
    public static final String SC_OK_DESC = "OK";
    public static final String SC_NOT_FOUND_DESC = "Not Found";
    public static final String SC_FORBIDDEN_DESC = "Forbidden";


    // HTTP methods
    
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_PROPFIND = "PROPFIND";
    public static final String METHOD_PROPPATCH = "PROPPATCH";
    public static final String METHOD_MKCOL = "MKCOL";
    public static final String METHOD_MOVE = "MOVE";
    public static final String METHOD_COPY = "COPY";
    public static final String METHOD_LOCK = "LOCK";
    public static final String METHOD_UNLOCK = "UNLOCK";

    // HTTP headers
    
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_DEPTH = "Depth";
    public static final String HEADER_DESTINATION = "Destination";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_EXPECT = "Expect";
    public static final String HEADER_EXPECT_CONTENT = "100-continue";
    public static final String HEADER_IF = "If";
    public static final String HEADER_IF_MATCH = "If-Match";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_IF_RANGE = "If-Range";
    public static final String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_LOCK_TOKEN = "Lock-Token";
    public static final String HEADER_OVERWRITE = "Overwrite";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_TIMEOUT = "Timeout";
    public static final String HEADER_USER_AGENT = "User-Agent";

    // If-Modified/If-Unmodified date format
    
    public static final String HEADER_IF_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    
    // If header keyword
    
    public static final String HEADER_KEY_NOT = "Not";

    public static final String AGENT_MICROSOFT_DATA_ACCESS_INTERNET_PUBLISHING_PROVIDER_DAV = "Microsoft Data Access Internet Publishing Provider DAV";
    public static final String AGENT_INTERNET_EXPLORER = "MSIE";
    // General string constants
    
    public static final String ASTERISK = "*";
    public static final String DEFAULT_NAMESPACE_URI = "DAV:";
    public static final String DIR_SEPARATOR = "/";
    public static final String FAKE_TOKEN = "faketoken";
    public static final String HTTP1_1 = "HTTP/1.1";
    public static final String INFINITE = "Infinite";
    public static final String INFINITY = "infinity";
    public static final String OPAQUE_LOCK_TOKEN = "opaquelocktoken:";
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String SECOND = "Second-";
    public static final String HEADER_VALUE_SEPARATOR = ",";
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String T = "T";

    // Strings used in WebDAV XML payload
    
    public static final String XML_NS = "xmlns";
    
    public static final String XML_ACTIVE_LOCK = "activelock";
    public static final String XML_ALLPROP = "allprop";
    public static final String XML_COLLECTION = "collection";
    public static final String XML_CREATION_DATE = "creationdate";
    public static final String XML_DEPTH = "depth";
    public static final String XML_DISPLAYNAME = "displayname";
    public static final String XML_EXCLUSIVE = "exclusive";
    public static final String XML_GET_CONTENT_LANGUAGE = "getcontentlanguage";
    public static final String XML_GET_CONTENT_LENGTH = "getcontentlength";
    public static final String XML_GET_CONTENT_TYPE = "getcontenttype";
    public static final String XML_GET_ETAG = "getetag";
    public static final String XML_GET_LAST_MODIFIED = "getlastmodified";
    public static final String XML_HREF = "href";
    public static final String XML_LOCK_DISCOVERY = "lockdiscovery";
    public static final String XML_LOCK_ENTRY = "lockentry";
    public static final String XML_LOCK_SCOPE = "lockscope";
    public static final String XML_LOCK_TOKEN = "locktoken";
    public static final String XML_LOCK_TYPE = "locktype";
    public static final String XML_MULTI_STATUS = "multistatus";
    public static final String XML_OWNER = "owner";
    public static final String XML_PROP = "prop";
    public static final String XML_PROPNAME = "propname";
    public static final String XML_PROPSTAT = "propstat";
    public static final String XML_RESOURCE_TYPE = "resourcetype";
    public static final String XML_RESPONSE = "response";
    public static final String XML_SHARED = "shared";
    public static final String XML_SOURCE = "source";
    public static final String XML_STATUS = "status";
    public static final String XML_SUPPORTED_LOCK = "supportedlock";
    public static final String XML_TIMEOUT = "timeout";
    public static final String XML_WRITE = "write";
    public static final String XML_SET = "set";
    public static final String XML_REMOVE = "remove";
    public static final String XML_ERROR = "error";

    // Namespaced versions of payload elements
    
    public static final String XML_NS_ACTIVE_LOCK = DAV_NS_PREFIX + "activelock";
    public static final String XML_NS_ALLPROP = DAV_NS_PREFIX + "allprop";
    public static final String XML_NS_COLLECTION = DAV_NS_PREFIX + "collection";
    public static final String XML_NS_CREATION_DATE = DAV_NS_PREFIX + "creationdate";
    public static final String XML_NS_DEPTH = DAV_NS_PREFIX + "depth";
    public static final String XML_NS_DISPLAYNAME = DAV_NS_PREFIX + "displayname";
    public static final String XML_NS_EXCLUSIVE = DAV_NS_PREFIX + "exclusive";
    public static final String XML_NS_GET_CONTENT_LANGUAGE = DAV_NS_PREFIX + "getcontentlanguage";
    public static final String XML_NS_GET_CONTENT_LENGTH = DAV_NS_PREFIX + "getcontentlength";
    public static final String XML_NS_GET_CONTENT_TYPE = DAV_NS_PREFIX + "getcontenttype";
    public static final String XML_NS_GET_ETAG = DAV_NS_PREFIX + "getetag";
    public static final String XML_NS_GET_LAST_MODIFIED = DAV_NS_PREFIX + "getlastmodified";
    public static final String XML_NS_HREF = DAV_NS_PREFIX + "href";
    public static final String XML_NS_LOCK_DISCOVERY = DAV_NS_PREFIX + "lockdiscovery";
    public static final String XML_NS_LOCK_ENTRY = DAV_NS_PREFIX + "lockentry";
    public static final String XML_NS_LOCK_SCOPE = DAV_NS_PREFIX + "lockscope";
    public static final String XML_NS_LOCK_TOKEN = DAV_NS_PREFIX + "locktoken";
    public static final String XML_NS_LOCK_TYPE = DAV_NS_PREFIX + "locktype";
    public static final String XML_NS_MULTI_STATUS = DAV_NS_PREFIX + "multistatus";
    public static final String XML_NS_OWNER = DAV_NS_PREFIX + "owner";
    public static final String XML_NS_PROP = DAV_NS_PREFIX + "prop";
    public static final String XML_NS_PROPNAME = DAV_NS_PREFIX + "propname";
    public static final String XML_NS_PROPSTAT = DAV_NS_PREFIX + "propstat";
    public static final String XML_NS_RESOURCE_TYPE = DAV_NS_PREFIX + "resourcetype";
    public static final String XML_NS_RESPONSE = DAV_NS_PREFIX + "response";
    public static final String XML_NS_SHARED = DAV_NS_PREFIX + "shared";
    public static final String XML_NS_SOURCE = DAV_NS_PREFIX + "source";
    public static final String XML_NS_STATUS = DAV_NS_PREFIX + "status";
    public static final String XML_NS_SUPPORTED_LOCK = DAV_NS_PREFIX + "supportedlock";
    public static final String XML_NS_TIMEOUT = DAV_NS_PREFIX + "timeout";
    public static final String XML_NS_WRITE = DAV_NS_PREFIX + "write";
    public static final String XML_NS_ERROR = DAV_NS_PREFIX + "error";
    public static final String XML_NS_CANNOT_MODIFY_PROTECTED_PROPERTY = DAV_NS_PREFIX + "cannot-modify-protected-property";
    
    
    public static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";
    
    // Alfresco specific properties
    
    public static final String XML_ALF_AUTHTICKET    = "authticket";
    public static final String XML_NS_ALF_AUTHTICKET = DAV_NS_PREFIX + "authticket";
    
    // Path seperator
    
    public static final String PathSeperator   = "/";
    public static final char PathSeperatorChar = '/';
    
    // Lock token seperator
    
    public static final String LOCK_TOKEN_SEPERATOR = ":";
    
    // Root path
    
    private static final String RootPath = PathSeperator;
    
    // Map WebDAV property names to Alfresco property names
    
    private static Hashtable<String, QName> _propertyNameMap;
    
    // WebDAV creation date/time formatter
    
    private static String CREATION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /**
     * Formats the given date so that it conforms with the Last-Modified HTTP header
     * 
     * @param date The date to format
     * @return The formatted date string
     */
    public static String formatModifiedDate(Date date)
    {
        return formatHeaderDate(date);
    }

    /**
     * Formats the given date so that it conforms with the Last-Modified HTTP header
     * 
     * @param date long
     * @return The formatted date string
     */
    public static String formatModifiedDate(long ldate)
    {
        return formatHeaderDate(ldate);
    }

    /**
     * Formats the given date so that it conforms with the WebDAV creation date/time format
     * 
     * @param date The date to format
     * @return The formatted date string
     */
    public static String formatCreationDate(Date date)
    {
        return DateFormatUtils.formatUTC(date, CREATION_DATE_FORMAT);
    }

    /**
     * Formats the given date so that it conforms with the WebDAV creation date/time format
     * 
     * @param date long
     * @return The formatted date string
     */
    public static String formatCreationDate(long ldate)
    {
        return DateFormatUtils.formatUTC(ldate, CREATION_DATE_FORMAT);
    }

    /**
     * Formats the given date for use in the HTTP header
     * 
     * @param date Date
     * @return String
     */
    public static String formatHeaderDate(Date date)
    {
        // HTTP header date/time format
        // NOTE: According to RFC2616 dates should always be in English and in
        //        the GMT timezone see http://rfc.net/rfc2616.html#p20 for details
        return DateFormatUtils.format(date, HEADER_IF_DATE_FORMAT, TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
    }
    
    /**
     * Formats the given date for use in the HTTP header
     * 
     * @param date long
     * @return String
     */
    public static String formatHeaderDate(long ldate)
    {
        // HTTP header date/time format
        // NOTE: According to RFC2616 dates should always be in English and in
        //        the GMT timezone see http://rfc.net/rfc2616.html#p20 for details
        return DateFormatUtils.format(ldate, HEADER_IF_DATE_FORMAT, TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
    }
    
    /**
     * Return the Alfresco property value for the specified WebDAV property
     * 
     * @param props Map<QName, Serializable>
     * @param davPropName String
     * @return Object
     */
    public static Object getDAVPropertyValue( Map<QName, Serializable> props, String davPropName)
    {
        // Convert the WebDAV property name to the corresponding Alfresco property
        
        QName propName = _propertyNameMap.get( davPropName);
        if ( propName == null)
            throw new AlfrescoRuntimeException("No mapping for WebDAV property " + davPropName);
        
        //  Return the property value
        Object value = props.get(propName);
        if (value instanceof ContentData)
        {
            ContentData contentData = (ContentData) value;
            if (davPropName.equals(WebDAV.XML_GET_CONTENT_TYPE))
            {
                value = contentData.getMimetype();
            }
            else if (davPropName.equals(WebDAV.XML_GET_CONTENT_LENGTH))
            {
                value = new Long(contentData.getSize());
            }
        }
        return value;
    }
    
    
    /**
     * Maps the current HTTP request to a path that can be used to access a content repository
     * 
     * @param request HTTP request
     * @return A content repository path
     */
    public static String getRepositoryPath(HttpServletRequest request)
    {
        // Try and get the path

        String strPath = null;
        
        try 
        {
            strPath = URLDecoder.decode( request.getRequestURI(), "UTF-8");
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
            strPath = RootPath;
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
                strPath = RootPath;
            }
        }

        // Make sure there are no trailing slashes
        
        if (strPath.length() > 1 && strPath.endsWith(DIR_SEPARATOR))
        {
            strPath = strPath.substring(0, strPath.length() - 1);
        }

        // Return the path
        
        return strPath;
    }

    /**
     * Returns a URL that could be used to access the given path.
     * 
     * @param request HttpServletRequest
     * @param path String
     * @param isCollection boolean
     * @return String
     */
    public static String getURLForPath(HttpServletRequest request, String path, boolean isCollection)
    {
        return getURLForPath(request, path, isCollection, null);
    }
    
    /**
     * Returns a URL that could be used to access the given path.
     * 
     * @param request HttpServletRequest
     * @param path String
     * @param isCollection boolean
     * @param userAgent String
     * @return String
     */
    public static String getURLForPath(HttpServletRequest request, String path, boolean isCollection, String userAgent)
    {
        StringBuilder urlStr = new StringBuilder(request.getRequestURI());
        String servletPath = request.getServletPath();
        
        int rootPos = urlStr.indexOf(servletPath);
        if (rootPos != -1)
        {
            urlStr.setLength(rootPos + servletPath.length());
        }
        
        if (urlStr.charAt(urlStr.length() - 1) != PathSeperatorChar)
        {
            urlStr.append(PathSeperator);
        }
        
        if (path.equals(RootPath) == false)
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
        
        // Return the URL string
        return urlStr.toString();
    }

    /**
     * Returns a context-relative path, beginning with a "/", that represents the canonical version
     * of the specified path after ".." and "." elements are resolved out. If the specified path
     * attempts to go outside the boundaries of the current context (i.e. too many ".." path
     * elements are present), return <code>null</code> instead.
     * 
     * @param strPath The path to be decoded
     */
    public static String decodeURL(String strPath)
    {
        if (strPath == null)
            return null;

        // Resolve encoded characters in the normalized path, which also handles encoded
        // spaces so we can skip that later. Placed at the beginning of the chain so that
        // encoded bad stuff(tm) can be caught by the later checks
        
        String strNormalized = null;

        try
        {
            strNormalized = URLDecoder.decode(strPath, "UTF-8");
        }
        catch (Exception ex)
        {
            logger.error("Error in decodeURL, URL = " + strPath, ex);
        }

        if (strNormalized == null)
            return (null);

        // Normalize the slashes and add leading slash if necessary
        
        if (strNormalized.indexOf('\\') >= 0)
            strNormalized = strNormalized.replace('\\', '/');

        if (!strNormalized.startsWith("/"))
            strNormalized = "/" + strNormalized;

        // Resolve occurrences of "//" in the normalized path
        
        while (true)
        {
            int index = strNormalized.indexOf("//");
            if (index < 0)
                break;
            strNormalized = strNormalized.substring(0, index) + strNormalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        
        while (true)
        {
            int index = strNormalized.indexOf("/./");
            if (index < 0)
                break;
            strNormalized = strNormalized.substring(0, index) + strNormalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        
        while (true)
        {
            int index = strNormalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context

            int index2 = strNormalized.lastIndexOf('/', index - 1);
            strNormalized = strNormalized.substring(0, index2) + strNormalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        
        return strNormalized;
    }
    
    /**
     * Make a unique lock token
     * 
     * @param lockNode NodeRef
     * @param owner String
     * @return String
     */
    public static final String makeLockToken(NodeRef lockNode, String owner)
    {
        StringBuilder str = new StringBuilder();
        
        str.append(WebDAV.OPAQUE_LOCK_TOKEN);
        str.append(lockNode.getId());
        str.append(LOCK_TOKEN_SEPERATOR);
        str.append(ISO9075.encode(owner));
        
        return str.toString();
    }
    
    /**
     * Parse a lock token returning the node if and username
     * 
     * @param lockToken String
     * @return String[]
     */
    public static final String[] parseLockToken(String lockToken)
    {
        // Check if the lock token is valid
        
        if ( lockToken == null)
            return null;
        
        // Check if the token contains the lock token header
        
        if ( lockToken.startsWith(WebDAV.OPAQUE_LOCK_TOKEN))
            lockToken = lockToken.substring(WebDAV.OPAQUE_LOCK_TOKEN.length());
        
        // Split the node id and username tokens
        
        int pos = lockToken.indexOf(LOCK_TOKEN_SEPERATOR);
        if ( pos == -1)
            return null;
        
        String[] tokens = new String[2];
        
        tokens[0] = lockToken.substring(0,pos);
        tokens[1] = lockToken.substring(pos + 1);
        
        return tokens;
    }
    
    /**
     * Returns string representation of the depth
     * 
     * @param depth
     * @return String
     */
    public static final String getDepthName(int depth)
    {
        switch (depth)
        {
        case DEPTH_0:
            return ZERO;

        case DEPTH_1:
            return ONE;

        case DEPTH_INFINITY:
            return INFINITY;
            
        default:
            throw new IllegalArgumentException("Unknown depth:" + depth);
        }
    }

    /**
     * Static initializer
     */
    static
    {
        // Create the WebDAV to Alfresco property mapping table
        
        _propertyNameMap = new Hashtable<String, QName>();
        
        _propertyNameMap.put(XML_DISPLAYNAME, ContentModel.PROP_NAME);
        _propertyNameMap.put(XML_CREATION_DATE, ContentModel.PROP_CREATED);
        _propertyNameMap.put(XML_GET_LAST_MODIFIED, ContentModel.PROP_MODIFIED);
        _propertyNameMap.put(XML_GET_CONTENT_TYPE, ContentModel.PROP_CONTENT);
    }
}
