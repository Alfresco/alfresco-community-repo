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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.web.util.HttpRangeProcessor;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implements the WebDAV GET method
 * 
 * @author gavinc
 */
public class GetMethod extends WebDAVMethod
{
    // Request parameters

    private static final String RANGE_HEADER_UNIT_SPECIFIER = "bytes=";
    private static final int MAX_RECURSE_ERROR_STACK = 20;
    private ArrayList<String> ifMatchTags = null;
    private ArrayList<String> ifNoneMatchTags = null;
    private Date m_ifModifiedSince = null;
    private Date m_ifUnModifiedSince = null;

    protected boolean m_returnContent = true;
    private String byteRanges;

    /**
     * Default constructor
     */
    public GetMethod()
    {
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // If the range header is present output a warning, add support later

        String strRange = m_request.getHeader(WebDAV.HEADER_RANGE);

        if (strRange != null && strRange.length() > 0)
        {
            byteRanges = strRange;
            if (logger.isDebugEnabled())
            {
                logger.debug("Range header supplied: " + byteRanges);
            }
        }

        // Capture all the If headers, process later

        String strIfMatch = m_request.getHeader(WebDAV.HEADER_IF_MATCH);

        if (strIfMatch != null && strIfMatch.length() > 0)
        {
            ifMatchTags = parseETags(strIfMatch);
        }

        String strIfNoneMatch = m_request.getHeader(WebDAV.HEADER_IF_NONE_MATCH);
        if (strIfNoneMatch != null && strIfNoneMatch.length() > 0)
        {
            ifNoneMatchTags = parseETags(strIfNoneMatch);
        }

        // Parse the dates

        SimpleDateFormat dateFormat = new SimpleDateFormat(WebDAV.HEADER_IF_DATE_FORMAT);
        String strIfModifiedSince = m_request.getHeader(WebDAV.HEADER_IF_MODIFIED_SINCE);

        if (strIfModifiedSince != null && strIfModifiedSince.length() > 0)
        {
            try
            {
                m_ifModifiedSince = dateFormat.parse(strIfModifiedSince);
            }
            catch (ParseException e)
            {
                logger.warn("Failed to parse If-Modified-Since date of " + strIfModifiedSince);
            }
        }

        String strIfUnModifiedSince = m_request.getHeader(WebDAV.HEADER_IF_UNMODIFIED_SINCE);
        if (strIfUnModifiedSince != null && strIfUnModifiedSince.length() > 0)
        {
            try
            {
                m_ifUnModifiedSince = dateFormat.parse(strIfUnModifiedSince);
            }
            catch (ParseException e)
            {
                logger.warn("Failed to parse If-Unmodified-Since date of " + strIfUnModifiedSince);
            }
        }
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // Nothing to do in this method
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
     * Exceute the WebDAV request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException, Exception
    {
        FileFolderService fileFolderService = getFileFolderService();
        NodeRef rootNodeRef = getRootNodeRef();
        String path = getPath();

        if (!m_returnContent)
        {
            // There are multiple cases where no content is sent (due to a HEAD request).
            // All of them require that the content length is set appropriately.
            m_response.setContentLength(0);
        }
        
        FileInfo nodeInfo = null;
        try
        {
            nodeInfo = getDAVHelper().getNodeForPath(rootNodeRef, path);
        }
        catch (FileNotFoundException e)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }
        
        FileInfo realNodeInfo = nodeInfo;

        // ALF-12008: Due to Windows Explorer's URL concatenation behaviour, we must present links as shortcuts to the real URL, rather than direct hrefs
        // This is at least consistent with the way the CIFS server handles links. See org.alfresco.filesys.repo.ContentDiskDriver.openFile().
        if (realNodeInfo.isLink())
        {
            Path pathToNode = getNodeService().getPath(nodeInfo.getLinkNodeRef());
            if (pathToNode.size() > 2)
            {
                pathToNode = pathToNode.subPath(2, pathToNode.size() -1);
            }

            String rootURL = getDAVHelper().getURLForPath(m_request, pathToNode.toDisplayPath(getNodeService(), getPermissionService()), true);
            if (rootURL.endsWith(WebDAVHelper.PathSeperator) == false)
            {
                rootURL = rootURL + WebDAVHelper.PathSeperator;
            }
           
            String fname = (String) getNodeService().getProperty(nodeInfo.getLinkNodeRef(), ContentModel.PROP_NAME);
            StringBuilder urlStr = new StringBuilder(200);
            urlStr.append("[InternetShortcut]\r\n");
            urlStr.append("URL=file://");
            urlStr.append(m_request.getServerName());
            // Only append the port if it is non-default for compatibility with XP
            int port = m_request.getServerPort();
            if (port != 80)
            {
                urlStr.append(":").append(port);
            }
            urlStr.append(rootURL).append(WebDAVHelper.encodeURL(fname, m_userAgent));
            urlStr.append("\r\n");
           
            m_response.setHeader(WebDAV.HEADER_CONTENT_TYPE, "text/plain; charset=ISO-8859-1");
            m_response.setHeader(WebDAV.HEADER_CONTENT_LENGTH, String.valueOf(urlStr.length()));
            m_response.getWriter().write(urlStr.toString());
        }
        // Check if the node is a folder
        else if (realNodeInfo.isFolder())
        {
            // is content required
            if (!m_returnContent)
            {
                // ALF-7883 fix, HEAD for collection (see http://www.webdav.org/specs/rfc2518.html#rfc.section.8.4)
                return;
            }
            
            // Generate a folder listing
            m_response.setContentType("text/html;charset=UTF-8");
            generateDirectoryListing(nodeInfo);
        }
        else
        {
            // Return the node details, and content if requested, check that the node passes the pre-conditions

            checkPreConditions(realNodeInfo);

            // Build the response header
            m_response.setHeader(WebDAV.HEADER_ETAG, getDAVHelper().makeQuotedETag(nodeInfo));

            Date modifiedDate = realNodeInfo.getModifiedDate();
            if (modifiedDate != null)
            {
                long modDate = DefaultTypeConverter.INSTANCE.longValue(modifiedDate);
                m_response.setHeader(WebDAV.HEADER_LAST_MODIFIED, WebDAV.formatHeaderDate(modDate));
            }

            ContentReader reader = fileFolderService.getReader(realNodeInfo.getNodeRef());
            // ensure that we generate something, even if the content is missing
            reader = FileContentReader.getSafeContentReader(
                    (ContentReader) reader,
                    I18NUtil.getMessage(FileContentReader.MSG_MISSING_CONTENT),
                    realNodeInfo.getNodeRef(), reader);
            
            readContent(realNodeInfo, reader);
        }
    }


    protected void readContent(FileInfo realNodeInfo, ContentReader reader) throws IOException,
                WebDAVServerException
    {
        try
        {
            attemptReadContent(realNodeInfo, reader);                
        }
        catch (final Throwable e)
        {
            boolean logAsError = true;
            Throwable t = e;
            // MNT-8989: Traverse the exception cause hierarchy, if we find a SocketException at fault,
            // assume this is a dropped connection and do not log a stack trace.
            int levels = 0;
            while (t.getCause() != null)
            {
                if (t == t.getCause() || ++levels == MAX_RECURSE_ERROR_STACK)
                {
                    // Avoid infinite loops.
                    break;
                }
                t = t.getCause();
                if (t instanceof SocketException || t.getClass().getSimpleName().equals("ClientAbortException"))
                {
                    logAsError = false;
                }
            }
            
            if (logAsError && logger.isErrorEnabled())
            {
                // Only log at ERROR level when not a SocketException as underlying cause.
                logger.error("Error while reading content", e);
            }
            else if (logger.isDebugEnabled())
            {
                // Log other errors at DEBUG level.
                logger.debug("Error while reading content", e);                
            }
            
            // Note no cause parameter supplied - avoid logging stack trace elsewhere.
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void attemptReadContent(FileInfo realNodeInfo, ContentReader reader)
                throws IOException
    {
        if (byteRanges != null && byteRanges.startsWith(RANGE_HEADER_UNIT_SPECIFIER))
        {
            HttpRangeProcessor rangeProcessor = new HttpRangeProcessor(getContentService());
            String userAgent = m_request.getHeader(WebDAV.HEADER_USER_AGENT);
            
            if (m_returnContent)
            {
                rangeProcessor.processRange(
                        m_response,
                        reader,
                        byteRanges.substring(6),
                        realNodeInfo.getNodeRef(),
                        ContentModel.PROP_CONTENT,
                        reader.getMimetype(),
                        userAgent);
            }
        }
        else
        {
                if (m_returnContent)
                {
                // there is content associated with the node
                m_response.setHeader(WebDAV.HEADER_CONTENT_LENGTH, Long.toString(reader.getSize()));
                m_response.setHeader(WebDAV.HEADER_CONTENT_TYPE, reader.getMimetype());
                
                // copy the content to the response output stream
                reader.getContent(m_response.getOutputStream());
            }
        }
    }

    /**
     * Checks the If header conditions
     * 
     * @param nodeInfo the node to check
     * @throws WebDAVServerException if a pre-condition is not met
     */
    private void checkPreConditions(FileInfo nodeInfo) throws WebDAVServerException
    {
        // Make an etag for the node

        String strETag = getDAVHelper().makeQuotedETag(nodeInfo);
        TypeConverter typeConv = DefaultTypeConverter.INSTANCE;

        // Check the If-Match header, don't send any content back if none of the tags in
        // the list match the etag, and the wildcard is not present

        if (ifMatchTags != null)
        {
            if (ifMatchTags.contains(WebDAV.ASTERISK) == false && ifMatchTags.contains(strETag) == false)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        }

        // Check the If-None-Match header, don't send any content back if any of the tags
        // in the list match the etag, or the wildcard is present

        if (ifNoneMatchTags != null)
        {
            if (ifNoneMatchTags.contains(WebDAV.ASTERISK) || ifNoneMatchTags.contains(strETag))
            {
                throw new WebDAVServerException(HttpServletResponse.SC_NOT_MODIFIED);
            }
        }

        // Check the modified since list, if the If-None-Match header was not specified

        if (m_ifModifiedSince != null && ifNoneMatchTags == null)
        {
            Date lastModifiedDate = nodeInfo.getModifiedDate();

            long fileLastModified = lastModifiedDate != null ? typeConv.longValue(lastModifiedDate) : 0L;
            long modifiedSince = m_ifModifiedSince.getTime();

            if (fileLastModified != 0L && fileLastModified <= modifiedSince)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_NOT_MODIFIED);
            }
        }

        // Check the un-modified since list

        if (m_ifUnModifiedSince != null)
        {
            Date lastModifiedDate = nodeInfo.getModifiedDate();

            long fileLastModified = lastModifiedDate != null ? typeConv.longValue(lastModifiedDate) : 0L;
            long unModifiedSince = m_ifUnModifiedSince.getTime();

            if (fileLastModified >= unModifiedSince)
            {
                throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        }
    }

    /**
     * Parses the given ETag header into a list of separate ETags
     * 
     * @param strETagHeader The header to parse
     * @return A list of ETags
     */
    private ArrayList<String> parseETags(String strETagHeader)
    {
        ArrayList<String> list = new ArrayList<String>();

        StringTokenizer tokenizer = new StringTokenizer(strETagHeader, WebDAV.HEADER_VALUE_SEPARATOR);
        while (tokenizer.hasMoreTokens())
        {
            list.add(tokenizer.nextToken().trim());
        }

        return list;
    }

    /**
     * Generates a HTML representation of the contents of the path represented by the given node
     * 
     * @param fileInfo the file to use
     */
    private void generateDirectoryListing(FileInfo fileInfo)
    {
        MimetypeService mimeTypeService = getMimetypeService();
        NodeService nodeService = getNodeService();

        Writer writer = null;

        try
        {
            writer = m_response.getWriter();

            boolean wasLink = false;
            if (fileInfo.isLink())
            {
                fileInfo = getFileFolderService().getFileInfo(fileInfo.getLinkNodeRef());
                wasLink = true;
            }
            
            // Get the list of child nodes for the parent node
            List<FileInfo> childNodeInfos = getDAVHelper().getChildren(fileInfo);

            // Send back the start of the HTML
            writer.write("<html><head><title>");
            writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.repository_title")));
            writer.write("</title>");
            writer.write("<style>");
            writer.write("body { font-family: Arial, Helvetica; font-size: 12pt; background-color: white; }\n");
            writer.write("table { font-family: Arial, Helvetica; font-size: 12pt; background-color: white; }\n");
            writer.write(".listingTable { border: solid black 1px; }\n");
            writer.write(".textCommand { font-family: verdana; font-size: 10pt; }\n");
            writer.write(".textLocation { font-family: verdana; font-size: 11pt; font-weight: bold; color: #2a568f; }\n");
            writer.write(".textData { font-family: verdana; font-size: 10pt; }\n");
            writer.write(".tableHeading { font-family: verdana; font-size: 10pt; font-weight: bold; color: white; background-color: #2a568f; }\n");
            writer.write(".rowOdd { background-color: #eeeeee; }\n");
            writer.write(".rowEven { background-color: #dddddd; }\n");
            writer.write("</style></head>\n");
            writer.flush();

            // Send back the table heading
            writer.write("<body>\n");
            writer.write("<table cellspacing='2' cellpadding='3' border='0' width='100%'>\n");
            writer.write("<tr><td colspan='4' class='textLocation'>");
            writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.directory_listing")));
            writer.write(' ');
            writer.write(WebDAVHelper.encodeHTML(getPath()));
            writer.write("</td></tr>\n");
            writer.write("<tr><td height='10' colspan='4'></td></tr></table>");

            writer.write("<table cellspacing='2' cellpadding='3' border='0' width='100%' class='listingTable'>\n");
            writer.write("<tr><td class='tableHeading' width='*'>");
            writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.column.name")));
            writer.write("</td>");
            writer.write("<td class='tableHeading' width='10%'>");
            writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.column.size")));
            writer.write("</td>");
            writer.write("<td class='tableHeading' width='20%'>");
            writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.column.type")));
            writer.write("</td>");
            writer.write("<td class='tableHeading' width='25%'>");
            writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.column.modifieddate")));
            writer.write("</td>");
            writer.write("</tr>\n");

            // Get the URL for the root path
            String rootURL = getURLForPath(m_request, getPath(), true);
            if (rootURL.endsWith(WebDAVHelper.PathSeperator) == false)
            {
                rootURL = rootURL + WebDAVHelper.PathSeperator;
            }

            if (wasLink)
            {
                Path pathToNode = nodeService.getPath(fileInfo.getNodeRef());
                if (pathToNode.size() > 2)
                {
                    pathToNode = pathToNode.subPath(2, pathToNode.size() - 1);
                }

                rootURL = getURLForPath(m_request, pathToNode.toDisplayPath(nodeService, getPermissionService()), true);
                if (rootURL.endsWith(WebDAVHelper.PathSeperator) == false)
                {
                    rootURL = rootURL + WebDAVHelper.PathSeperator;
                }

                rootURL = rootURL + WebDAVHelper.encodeURL(fileInfo.getName(), m_userAgent) + WebDAVHelper.PathSeperator;
            }
            // Start with a link to the parent folder so we can navigate back up, unless we are at the root level
            if (! getDAVHelper().isRootPath(getPath(), getServletPath()))
            {
                writer.write("<tr class='rowOdd'>");
                writer.write("<td colspan='4' class='textData'><a href=\"");

                // Strip the last folder from the path
                String parentFolderUrl = parentFolder(rootURL);
                writer.write(parentFolderUrl);

                writer.write("\">");
                writer.write("[");
                writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.column.navigate_up")));
                writer.write("]</a>");
                writer.write("</tr>\n");
            }

            // Send back what we have generated so far
            writer.flush();
            int rowId = 0;
            
            for (FileInfo childNodeInfo : childNodeInfos)
            {
                // Output the details for the current node
                writer.write("<tr class='");
                if ((rowId++ & 1) == 1)
                {
                    writer.write("rowOdd");
                }
                else
                {
                    writer.write("rowEven");
                }
                writer.write("'><td class='textData'><a href=\"");
                writer.write(rootURL);

                // name field
                String fname = childNodeInfo.getName();

                writer.write(WebDAVHelper.encodeURL(fname, m_userAgent));
                writer.write("\">");
                writer.write(WebDAVHelper.encodeHTML(fname));
                writer.write("</a>");

                // size field
                writer.write("</td><td class='textData'>");

                ContentData contentData = null;
                if (!childNodeInfo.isFolder())
                {
                    Serializable contentPropertyName = nodeService.getProperty(childNodeInfo.getNodeRef(), ContentModel.PROP_CONTENT_PROPERTY_NAME);
                    QName contentPropertyQName = DefaultTypeConverter.INSTANCE.convert(QName.class, contentPropertyName);

                    if (null == contentPropertyQName)
                    {
                        contentPropertyQName = ContentModel.PROP_CONTENT;
                    }

                    Serializable contentProperty = nodeService.getProperty(childNodeInfo.getNodeRef(), contentPropertyQName);

                    if (contentProperty instanceof ContentData)
                    {
                        contentData = (ContentData) contentProperty;
                    }
                }

                if (childNodeInfo.isFolder())
                {
                    writer.write("&nbsp;");
                }
                else
                {
                    if (null != contentData)
                    {
                        writer.write(formatSize(Long.toString(contentData.getSize())));
                    }
                    else
                    {
                        writer.write("&nbsp;");
                    }

                }
                writer.write("</td><td class='textData'>");

                // mimetype field
                if (childNodeInfo.isFolder())
                {
                    writer.write("&nbsp;");
                }
                else
                {
                    String mimetype = "&nbsp;";
                    if (null != contentData)
                    {
                        mimetype = contentData.getMimetype();
                        String displayType = mimeTypeService.getDisplaysByMimetype().get(mimetype);

                        if (displayType != null)
                        {
                            mimetype = displayType;
                        }
                    }
                    writer.write(mimetype);
                }
                writer.write("</td><td class='textData'>");
                
                // modified date field
                Date modifiedDate = childNodeInfo.getModifiedDate();
                if (modifiedDate != null)
                {
                    writer.write(WebDAV.formatHeaderDate(DefaultTypeConverter.INSTANCE.longValue(modifiedDate)));
                }
                else
                {
                    writer.write("&nbsp;");
                }
                writer.write("</td></tr>\n");
                
                // flush every few rows
                if ((rowId & 15) == 0)
                {
                    writer.flush();
                }
            }
            
            writer.write("</table></body></html>");
        }
        catch (Throwable e)
        {
            logger.error(e);

            if (writer != null)
            {
                try
                {
                    writer.write("</table><table><tr><td style='color:red'>");
                    writer.write(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.err.dir")));
                    writer.write("</td></tr></table></body></html>");
                    writer.flush();
                }
                catch (IOException ioe)
                {
                }
            }
        }
    }

    /**
     * Given a path, will return the parent path. For example: /a/b/c
     * will return /a/b and /a/b will return /a.
     * 
     * @param path The path to return the parent of - must be non-null.
     * @return String - parent path.
     */
    private String parentFolder(String path)
    {
        if (path.endsWith(WebDAVHelper.PathSeperator))
        {
            // Strip trailing slash.
            path = path.substring(0, path.length() - 1);
        }
        String[] paths = getDAVHelper().splitPath(path);
        String parent = paths[0];
        if (parent.equals(""))
        {
            parent = WebDAVHelper.PathSeperator;
        }
        return parent;
    }

    /**
     * Formats the given size for display in a directory listing
     * 
     * @param strSize The content size
     * @return The formatted size
     */
    private String formatSize(String strSize)
    {
        String strFormattedSize = strSize;

        int length = strSize.length();
        if (length < 4)
        {
            strFormattedSize = strSize + ' ' + WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.size.bytes"));
        }
        else if (length >= 4 && length < 7)
        {
            String strLeft = strSize.substring(0, length - 3);
            String strRight = strSize.substring(length - 3, length - 2);

            StringBuilder buffer = new StringBuilder(strLeft);
            if (!strRight.equals("0"))
            {
                buffer.append('.');
                buffer.append(strRight);
            }
            buffer.append(' ').append(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.size.kilobytes")));

            strFormattedSize = buffer.toString();
        }
        else
        {
            String strLeft = strSize.substring(0, length - 6);
            String strRight = strSize.substring(length - 6, length - 5);

            StringBuilder buffer = new StringBuilder(strLeft);
            if (!strRight.equals("0"))
            {
                buffer.append('.');
                buffer.append(strRight);
            }
            buffer.append(' ').append(WebDAVHelper.encodeHTML(I18NUtil.getMessage("webdav.size.megabytes")));

            strFormattedSize = buffer.toString();
        }

        return strFormattedSize;
    }
}
