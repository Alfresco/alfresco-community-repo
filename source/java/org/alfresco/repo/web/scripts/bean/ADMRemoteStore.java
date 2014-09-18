/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ADM Remote Store service.
 * <p>
 * This implementation of the RemoteStore is tied to the current SiteService implementation.
 * <p>
 * It remaps incoming generic document path requests to the appropriate folder structure
 * in the Sites folder. Dashboard pages and component bindings are remapped to take advantage
 * of inherited permissions in the appropriate root site folder, ensuring that only valid
 * users can write to the appropriate configuration objects.
 * 
 * @see BaseRemoteStore for the available API methods.
 * 
 * @author Kevin Roast
 */
public class ADMRemoteStore extends BaseRemoteStore
{
    private static final Log logger = LogFactory.getLog(ADMRemoteStore.class);
    
    // name of the surf config folder
    private static final String SURF_CONFIG = "surf-config";

    // patterns used to match site and user specific configuration locations
    private static final String PATH_COMPONENTS = "components";
    private static final String PATH_PAGES      = "pages";
    private static final String PATH_USER       = "user";
    private static final String PATH_SITE       = "site";
    private static final String USER_CONFIG = ".*\\." + PATH_USER + "~(.*)~.*";
    private static final String USER_CONFIG_PATTERN = USER_CONFIG.replaceAll("\\.\\*", "*").replace("\\", "");
    private static final Pattern USER_PATTERN_1 = Pattern.compile(".*/" + PATH_COMPONENTS + "/" + USER_CONFIG);
    private static final Pattern USER_PATTERN_2 = Pattern.compile(".*/" + PATH_PAGES + "/" + PATH_USER + "/(.*?)(/.*)?$");
    private static final Pattern SITE_PATTERN_1 = Pattern.compile(".*/" + PATH_COMPONENTS + "/.*\\." + PATH_SITE + "~(.*)~.*");
    private static final Pattern SITE_PATTERN_2 = Pattern.compile(".*/" + PATH_PAGES + "/" + PATH_SITE + "/(.*?)(/.*)?$");
    
    
    // service beans
    protected NodeService nodeService;
    protected NodeService unprotNodeService;
    protected FileFolderService fileFolderService;
    protected NamespaceService namespaceService;
    protected SiteService siteService;
    protected ContentService contentService;
    protected HiddenAspect hiddenAspect;
    private BehaviourFilter behaviourFilter;
    
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");


    /**
     * @param nodeService       the NodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeService       the NodeService to set
     */
    public void setUnprotectedNodeService(NodeService nodeService)
    {
        this.unprotNodeService = nodeService;
    }

    /**
     * @param fileFolderService the FileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param namespaceService  the NamespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param siteService       the SiteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param contentService    the ContentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService; 
    }
    
    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /**
     * Gets the last modified timestamp for the document.
     * <p>
     * The output will be the last modified date as a long toString().
     * 
     * @param path  document path to an existing document
     */
    @Override
    protected void lastModified(final WebScriptResponse res, final String store, final String path)
        throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final String encpath = encodePath(path);
                final FileInfo fileInfo = resolveFilePath(encpath);
                if (fileInfo == null)
                {
                    throw new WebScriptException("Unable to locate file: " + encpath);
                }
                
                Writer out = res.getWriter();
                out.write(Long.toString(fileInfo.getModifiedDate().getTime()));
                out.close();
                if (logger.isDebugEnabled())
                    logger.debug("lastModified: " + Long.toString(fileInfo.getModifiedDate().getTime()));
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Gets a document.
     * <p>
     * The output will be the document content stream.
     * 
     * @param path  document path
     */
    @Override
    protected void getDocument(final WebScriptResponse res, final String store, final String path)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final String encpath = encodePath(path);
                final FileInfo fileInfo = resolveFilePath(encpath);
                if (fileInfo == null || fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                final ContentReader reader;
                try
                {
                    reader = contentService.getReader(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
                    if (reader == null || !reader.exists())
                    {
                        throw new WebScriptException("No content found for file: " + encpath);
                    }
                    
                    // establish mimetype
                    String mimetype = reader.getMimetype();
                    if (mimetype == null || mimetype.length() == 0)
                    {
                        mimetype = MimetypeMap.MIMETYPE_BINARY;
                        int extIndex = encpath.lastIndexOf('.');
                        if (extIndex != -1)
                        {
                            String ext = encpath.substring(extIndex + 1);
                            String mt = mimetypeService.getMimetypesByExtension().get(ext);
                            if (mt != null)
                            {
                                mimetype = mt;
                            }
                        }
                    }
                    
                    // set mimetype for the content and the character encoding + length for the stream
                    res.setContentType(mimetype);
                    res.setContentEncoding(reader.getEncoding());
                    SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
                    formatter.setTimeZone(GMT);
                    res.setHeader("Last-Modified", formatter.format(fileInfo.getModifiedDate()));
                    res.setHeader("Content-Length", Long.toString(reader.getSize()));
                    
                    if (logger.isDebugEnabled())
                        logger.debug("getDocument: " + fileInfo.toString());
                    
                    // get the content and stream directly to the response output stream
                    // assuming the repository is capable of streaming in chunks, this should allow large files
                    // to be streamed directly to the browser response stream.
                    try
                    {
                        reader.getContent(res.getOutputStream());
                    }
                    catch (SocketException e1)
                    {
                        // the client cut the connection - our mission was accomplished apart from a little error message
                        if (logger.isDebugEnabled())
                            logger.debug("Client aborted stream read:\n\tnode: " + encpath + "\n\tcontent: " + reader);
                    }
                    catch (ContentIOException e2)
                    {
                        if (logger.isInfoEnabled())
                            logger.info("Client aborted stream read:\n\tnode: " + encpath + "\n\tcontent: " + reader);
                    }
                    catch (Throwable err)
                    {
                       if (err.getCause() instanceof SocketException)
                       {
                          if (logger.isDebugEnabled())
                              logger.debug("Client aborted stream read:\n\tnode: " + encpath + "\n\tcontent: " + reader);
                       }
                       else
                       {
                           if (logger.isInfoEnabled())
                               logger.info(err.getMessage());
                           res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
                       }
                    }
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Determines if the document exists.
     * 
     * The output will be either the string "true" or the string "false".
     * 
     * @param path  document path
     */
    @Override
    protected void hasDocument(final WebScriptResponse res, final String store, final String path) throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final String encpath = encodePath(path);
                final FileInfo fileInfo = resolveFilePath(encpath);
                
                Writer out = res.getWriter();
                out.write(Boolean.toString(fileInfo != null && !fileInfo.isFolder()));
                out.close();
                if (logger.isDebugEnabled())
                    logger.debug("hasDocument: " + Boolean.toString(fileInfo != null && !fileInfo.isFolder()));
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Creates a document.
     * <p>
     * Create methods are user authenticated, so the creation of site config must be
     * allowed for the current user.
     * 
     * @param path          document path
     * @param content       content of the document to write
     */
    @Override
    protected void createDocument(final WebScriptResponse res, final String store, final String path, final InputStream content)
    {
        try
        {
            writeDocument(path, content);
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
            throw ae;
        }
        catch (FileExistsException feeErr)
        {
            res.setStatus(Status.STATUS_CONFLICT);
            throw feeErr;
        }
    }
    
    /**
     * Creates multiple XML documents encapsulated in a single one. 
     * 
     * @param content       XML document containing multiple document contents to write
     */
    @Override
    protected void createDocuments(WebScriptResponse res, String store, InputStream in)
    {
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
            Document document;
            document = documentBuilder.parse(in);
            Element docEl = document.getDocumentElement();
            Transformer transformer = ADMRemoteStore.this.transformer.get();
            for (Node n = docEl.getFirstChild(); n != null; n = n.getNextSibling())
            {
                if (!(n instanceof Element))
                {
                    continue;
                }
                final String path = ((Element) n).getAttribute("path");
                
                // Turn the first element child into a document
                Document doc = documentBuilder.newDocument();
                Node child;
                for (child = n.getFirstChild(); child != null ; child=child.getNextSibling())
                {
                   if (child instanceof Element)
                   {
                       doc.appendChild(doc.importNode(child, true));
                       break;
                   }
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream(512);
                transformer.transform(new DOMSource(doc), new StreamResult(out));
                out.close();
                
                writeDocument(path, new ByteArrayInputStream(out.toByteArray()));
            }
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
            throw ae;
        }
        catch (FileExistsException feeErr)
        {
            res.setStatus(Status.STATUS_CONFLICT);
            throw feeErr;
        }
        catch (Exception e)
        {
            // various annoying checked SAX/IO exceptions related to XML processing can be thrown
            // none of them should occur if the XML document is well formed
            logger.error(e);
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
    }
    
    protected void writeDocument(final String path, final InputStream content)
    {
        final String encpath = encodePath(path);
        final int off = encpath.lastIndexOf('/');
        if (off != -1)
        {
            // check we actually are the user we are creating a user specific path for
            final String runAsUser = getPathRunAsUser(path);
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @SuppressWarnings("synthetic-access")
                public Void doWork() throws Exception
                {
                    final FileInfo parentFolder = resolveNodePath(encpath, true, false);
                    if (parentFolder == null)
                    {
                        throw new IllegalStateException("Unable to aquire parent folder reference for path: " + path);
                    }
                    
                    // ALF-17729 / ALF-17796 - disable auditable on parent folder
                    NodeRef parentFolderRef = parentFolder.getNodeRef();
                    behaviourFilter.disableBehaviour(parentFolderRef, ContentModel.ASPECT_AUDITABLE);
                    
                    try
                    {
                        final String name = encpath.substring(off + 1);
                        // existence check - convert to an UPDATE - could occur if multiple threads request
                        // a write to the same document - a valid possibility but rare
                        if (nodeService.getChildByName(parentFolderRef, ContentModel.ASSOC_CONTAINS, name) == null)
                        {
                            FileInfo fileInfo = fileFolderService.create(
                                    parentFolderRef, name, ContentModel.TYPE_CONTENT);
                            Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(1, 1.0f);
                            aspectProperties.put(ContentModel.PROP_IS_INDEXED, false);
                            unprotNodeService.addAspect(fileInfo.getNodeRef(), ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);
                            ContentWriter writer = contentService.getWriter(
                                    fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                            writer.guessMimetype(fileInfo.getName());
                            writer.putContent(content);
                            if (logger.isDebugEnabled())
                                logger.debug("createDocument: " + fileInfo.toString());
                        }
                        else
                        {
                            ContentWriter writer = contentService.getWriter(
                                    nodeService.getChildByName(parentFolderRef, ContentModel.ASSOC_CONTAINS, name),
                                    ContentModel.PROP_CONTENT,
                                    true);
                            writer.guessMimetype(name);
                            writer.putContent(content);
                            if (logger.isDebugEnabled())
                                logger.debug("createDocument (updated): " + name);
                        }
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(parentFolderRef, ContentModel.ASPECT_AUDITABLE);
                    }
                    
                    return null;
                }
            }, runAsUser);
        }
    }
    
    /**
     * Get the RunAs user need to execute a Write operation on the given path.
     * 
     * @param path  Document path
     * @return runas user - will be the Full Authenticated User or System as required
     */
    protected String getPathRunAsUser(final String path)
    {
        // check we actually are the user we are creating a user specific path for
        String runAsUser = AuthenticationUtil.getFullyAuthenticatedUser();
        String userId = null;
        Matcher matcher;
        if ((matcher = USER_PATTERN_1.matcher(path)).matches())
        {
            userId = matcher.group(1);
        }
        else if ((matcher = USER_PATTERN_2.matcher(path)).matches())
        {
            userId = matcher.group(1);
        }
        if (userId != null && userId.equals(runAsUser))
        {
            runAsUser = AuthenticationUtil.getSystemUserName();
        }
        return runAsUser;
    }

    /**
     * Updates an existing document.
     * <p>
     * Update methods are user authenticated, so the modification of site config must be
     * allowed for the current user.
     * 
     * @param path          document path to update
     * @param content       content to update the document with
     */
    @Override
    protected void updateDocument(final WebScriptResponse res, String store, final String path, final InputStream content)
    {
        final String runAsUser = getPathRunAsUser(path);
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final String encpath = encodePath(path);
                final FileInfo fileInfo = resolveFilePath(encpath);
                if (fileInfo == null || fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                try
                {
                    ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                    writer.putContent(content);
                    if (logger.isDebugEnabled())
                        logger.debug("updateDocument: " + fileInfo.toString());
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                    throw ae;
                }
                return null;
            }
        }, runAsUser);
    }
    
    /**
     * Deletes an existing document.
     * <p>
     * Delete methods are user authenticated, so the deletion of the document must be
     * allowed for the current user.
     * 
     * @param path  document path
     */
    @Override
    protected void deleteDocument(final WebScriptResponse res, final String store, final String path)
    {
        final String encpath = encodePath(path);
        final FileInfo fileInfo = resolveFilePath(encpath);
        if (fileInfo == null || fileInfo.isFolder())
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        try
        {
            final NodeRef fileRef = fileInfo.getNodeRef();
            this.nodeService.addAspect(fileRef, ContentModel.ASPECT_TEMPORARY, null);
            
            // ALF-17729
            NodeRef parentFolderRef = unprotNodeService.getPrimaryParent(fileRef).getParentRef();
            behaviourFilter.disableBehaviour(parentFolderRef, ContentModel.ASPECT_AUDITABLE);
            
            try
            {
                this.nodeService.deleteNode(fileRef);
            }
            finally
            {
                behaviourFilter.enableBehaviour(parentFolderRef, ContentModel.ASPECT_AUDITABLE);
            }
            
            if (logger.isDebugEnabled())
                logger.debug("deleteDocument: " + fileInfo.toString());
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
            throw ae;
        }
    }

    /**
     * Lists the document paths under a given path.
     * <p>
     * The output will be the list of relative document paths found under the path.
     * Separated by newline characters.
     * 
     * @param path      document path
     * @param recurse   true to peform a recursive list, false for direct children only.
     * 
     * @throws IOException if an error occurs listing the documents
     */
    @Override
    protected void listDocuments(final WebScriptResponse res, final String store, final String path, final boolean recurse)
        throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                res.setContentType("text/plain;charset=UTF-8");
                
                final String encpath = encodePath(path);
                final FileInfo fileInfo = resolveNodePath(encpath, false, true);
                if (fileInfo == null || !fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                try
                {
                    outputFileNodes(res.getWriter(), fileInfo, aquireSurfConfigRef(encpath, false), "*", recurse);
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                finally
                {
                    res.getWriter().close();
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Lists the document paths matching a file pattern under a given path.
     * 
     * The output will be the list of relative document paths found under the path that
     * match the given file pattern. Separated by newline characters.
     * 
     * @param path      document path
     * @param pattern   file pattern to match - allows wildcards e.g. page.*.site.xml
     * 
     * @throws IOException if an error occurs listing the documents
     */
    @Override
    protected void listDocuments(final WebScriptResponse res, final String store, final String path, final String pattern)
        throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                res.setContentType("text/plain;charset=UTF-8");
                
                String filePattern;
                if (pattern == null || pattern.length() == 0)
                {
                    filePattern = "*";
                }
                else
                {
                    // need to ensure match pattern is path encoded - but don't encode * character!
                    StringBuilder buf = new StringBuilder(pattern.length());
                    for (StringTokenizer t = new StringTokenizer(pattern, "*"); t.hasMoreTokens(); /**/)
                    {
                        buf.append(encodePath(t.nextToken()));
                        if (t.hasMoreTokens())
                        {
                            buf.append('*');
                        }
                    }
                    // ensure the escape character is itself escaped
                    filePattern = buf.toString().replace("\\", "\\\\");
                }
                
                // ensure we pass in the file pattern as it is used as part of the folder match - i.e.
                // for a site component set e.g. /alfresco/site-data/components/page.*.site~xyz~dashboard.xml
                final String encpath = encodePath(path);
                final FileInfo fileInfo = resolveNodePath(encpath, filePattern, false, true);
                if (fileInfo == null || !fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("listDocuments() pattern: " + filePattern);
                
                try
                {
                    outputFileNodes(
                            res.getWriter(), fileInfo,
                            aquireSurfConfigRef(encpath + "/" + filePattern, false),
                            filePattern, false);
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                finally
                {
                    res.getWriter().close();
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @param path      cm:name based root relative path
     *                  example: /alfresco/site-data/pages/customise-user-dashboard.xml
     * 
     * @return FileInfo representing the file/folder at the specified path location
     *         or null if the supplied path does not exist in the store
     */
    private FileInfo resolveFilePath(final String path)
    {
        return resolveNodePath(path, false, false);
    }
    
   /**
     * @param path      cm:name based root relative path
     *                  example: /alfresco/site-data/pages/customise-user-dashboard.xml
     *                           /alfresco/site-data/components
     * @param create    if true create the config and folder dirs for the given path returning
     *                  the FileInfo for the last parent in the path, if false only attempt to
     *                  resolve the folder path if it exists returning the last element.
     * @param isFolder  True if the path is for a folder, false if it ends in a filename
     * 
     * @return FileInfo representing the file/folder at the specified path location (see create
     *         parameter above) or null if the supplied path does not exist in the store.
     */
    private FileInfo resolveNodePath(final String path, final boolean create, final boolean isFolder)
    {
        return resolveNodePath(path, null, create, isFolder);
    }
    
    /**
     * @param path      cm:name based root relative path
     *                  example: /alfresco/site-data/pages/customise-user-dashboard.xml
     *                           /alfresco/site-data/components
     * @param pattern   optional pattern that is used as part of the match to aquire the surf-config
     *                  folder under the appropriate sites or user location.
     * @param create    if true create the config and folder dirs for the given path returning
     *                  the FileInfo for the last parent in the path, if false only attempt to
     *                  resolve the folder path if it exists returning the last element.
     * @param isFolder  True if the path is for a folder, false if it ends in a filename
     * 
     * @return FileInfo representing the file/folder at the specified path location (see create
     *         parameter above) or null if the supplied path does not exist in the store.
     */
    private FileInfo resolveNodePath(final String path, final String pattern, final boolean create, final boolean isFolder)
    {
        if (logger.isDebugEnabled())
            logger.debug("Resolving path: " + path);
        
        FileInfo result = null;
        if (path != null)
        {
            // break down the path into its component elements
            List<String> pathElements = new ArrayList<String>(4);
            final StringTokenizer t = new StringTokenizer(path, "/");
            // the store requires paths of the form /alfresco/site-data/<objecttype>[/<folder>]/<file>.xml
            if (t.countTokens() >= 3)
            {
                t.nextToken();  // skip /alfresco
                t.nextToken();  // skip /site-data
                // collect remaining folder path (and file)
                while (t.hasMoreTokens())
                {
                    pathElements.add(t.nextToken());
                }
                
                NodeRef surfConfigRef = aquireSurfConfigRef(path + (pattern != null ? ("/" + pattern) : ""), create);
                try
                {
                    if (surfConfigRef != null)
                    {
                        if (create)
                        {
                            List<String> folders = isFolder ? pathElements : pathElements.subList(0, pathElements.size() - 1);
                            
                            List<FileFolderUtil.PathElementDetails> folderDetails = new ArrayList<>(pathElements.size());
                            Map<QName, Serializable> prop = new HashMap<>(2);
                            prop.put(ContentModel.PROP_IS_INDEXED, false);
                            prop.put(ContentModel.PROP_IS_CONTENT_INDEXED, false);
                            for (String element : folders)
                            {
                                Map<QName, Map<QName, Serializable>> aspects = Collections.singletonMap(ContentModel.ASPECT_INDEX_CONTROL, prop);
                                folderDetails.add(new FileFolderUtil.PathElementDetails(element, aspects));
                            }
                            // ensure folders exist down to the specified parent
                            // ALF-17729 / ALF-17796 - disable auditable on parent folders
                            result = FileFolderUtil.makeFolders(
                                    this.fileFolderService,nodeService,
                                    surfConfigRef,
                                    folderDetails,
                                    ContentModel.TYPE_FOLDER,
                                    behaviourFilter,
                                    new HashSet<QName>(Arrays.asList(new QName[]{ContentModel.ASPECT_AUDITABLE})));
                        }
                        else
                        {
                            // perform the cm:name path lookup against our config root node
                            result = this.fileFolderService.resolveNamePath(surfConfigRef, pathElements);
                        }
                    }
                }
                catch (FileNotFoundException fnfErr)
                {
                    // this is a valid condition - we return null to indicate failed lookup
                }
            }
        }
        return result;
    }

    /**
     * Aquire (optionally create) the NodeRef to the "surf-config" folder as appropriate
     * for the given path.
     * <p>
     * Disassmbles the path to correct match either user, site or generic folder path.
     * 
     * @param path
     * @param create
     * 
     * @return NodeRef to the "surf-config" folder, or null if it does not exist yet.
     */
    private NodeRef aquireSurfConfigRef(final String path, final boolean create)
    {
        // remap the path into the appropriate Sites or site relative folder location
        // by first matching the path to appropriate user or site regex
        final boolean debug = logger.isDebugEnabled();
        String userId = null;
        String siteName = null;
        Matcher matcher;
        if (debug)
        {
            // user data is stored directly under the Sites folder along with
            // other generic config files - there is actually no need to match
            // anything other than site specific config other than for debug
            if ((matcher = USER_PATTERN_1.matcher(path)).matches())
            {
                userId = matcher.group(1);
            }
            else if ((matcher = USER_PATTERN_2.matcher(path)).matches())
            {
                userId = matcher.group(1);
            }
            else if ((matcher = SITE_PATTERN_1.matcher(path)).matches())
            {
                siteName = matcher.group(1);
            }
            else if ((matcher = SITE_PATTERN_2.matcher(path)).matches())
            {
                siteName = matcher.group(1);
            }
        }
        else if ((matcher = SITE_PATTERN_1.matcher(path)).matches())
        {
            siteName = matcher.group(1);
        }
        else if ((matcher = SITE_PATTERN_2.matcher(path)).matches())
        {
            siteName = matcher.group(1);
        }
        
        NodeRef surfConfigRef = null;
        if (siteName != null)
        {
            if (debug) logger.debug("...resolved site path id: " + siteName);
            NodeRef siteRef = getSiteNodeRef(siteName);
            if (siteRef != null)
            {
                surfConfigRef = getSurfConfigNodeRef(siteRef, create);
            }
        }
        else
        {
            if (debug)
            {
                if (userId != null)
                {
                    logger.debug("...resolved user path id: " + userId);
                }
                else
                {
                    logger.debug("...resolved to generic path.");
                }
            }
            surfConfigRef = getSurfConfigNodeRef(getRootNodeRef(), create);
        }
        return surfConfigRef;
    }
    
    /**
     * Return the "surf-config" noderef under the given root. No attempt will be made
     * to create the node if it does not exist yet.
     * 
     * @param rootRef   Root node reference where the "surf-config" folder should live
     * 
     * @return surf-config folder ref if found, null otherwise
     */
    private NodeRef getSurfConfigNodeRef(final NodeRef rootRef)
    {
        return getSurfConfigNodeRef(rootRef, false);
    }
    
    /**
     * Return the "surf-config" noderef under the given root. Optionally create the
     * folder if it does not exist yet. NOTE: must only be set to create if within a
     * WRITE transaction context.
     * <p>
     * Adds the "isIndexed = false" property to the surf-config folder node.
     * 
     * @param rootRef   Root node reference where the "surf-config" folder should live
     * @param create    True to create the folder if missing, false otherwise
     * 
     * @return surf-config folder ref if found, null otherwise if not creating
     */
    protected NodeRef getSurfConfigNodeRef(final NodeRef rootRef, final boolean create)
    {
        NodeRef surfConfigRef = this.unprotNodeService.getChildByName(
                rootRef, ContentModel.ASSOC_CONTAINS, SURF_CONFIG);
        if (create && surfConfigRef == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("'surf-config' folder not found under path, creating...");
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, SURF_CONFIG);
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1, 1.0f);
            properties.put(ContentModel.PROP_NAME, (Serializable) SURF_CONFIG);
            ChildAssociationRef ref = this.unprotNodeService.createNode(
                    rootRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER, properties);
            surfConfigRef = ref.getChildRef();
            // surf-config needs to be hidden - applies index control aspect as part of the hidden aspect
            hiddenAspect.hideNode(ref.getChildRef(), false, false, false);
        }
        return surfConfigRef;
    }

    /**
     * @return NodeRef to the shared components config folder
     */
    protected NodeRef getGlobalComponentsNodeRef()
    {
        NodeRef result = null;
        
        NodeRef surfRef = getSurfConfigNodeRef(siteService.getSiteRoot());
        if (surfRef != null)
        {
            result = nodeService.getChildByName(surfRef, ContentModel.ASSOC_CONTAINS, PATH_COMPONENTS);
        }
        
        return result;
    }

    /**
     * @return NodeRef to the shared user config folder
     */
    protected NodeRef getGlobalUserFolderNodeRef()
    {
        NodeRef result = null;
        
        NodeRef surfRef = getSurfConfigNodeRef(siteService.getSiteRoot());
        if (surfRef != null)
        {
            NodeRef pagesRef = nodeService.getChildByName(surfRef, ContentModel.ASSOC_CONTAINS, PATH_PAGES);
            if (pagesRef != null)
            {
                result = nodeService.getChildByName(pagesRef, ContentModel.ASSOC_CONTAINS, PATH_USER);
            }
        }
        
        return result;
    }

    /**
     * Generate the search pattern for a Surf config location for a user name.
     * 
     * @param userName  to build pattern for
     * @return the search pattern
     */
    protected String buildUserConfigSearchPattern(String userName)
    {
        return USER_CONFIG_PATTERN.replace("(*)", encodePath(userName));
    }

    /**
     * @return the Sites folder root node reference
     */
    private NodeRef getRootNodeRef()
    {
        return this.siteService.getSiteRoot();
    }
    
    /**
     * @param shortName     Site shortname
     * 
     * @return the given Site folder node reference
     */
    private NodeRef getSiteNodeRef(String shortName)
    {
        SiteInfo siteInfo = this.siteService.getSite(shortName); 
        return siteInfo != null ? siteInfo.getNodeRef() : null;
    }
    
    /**
     * Output the matching file paths a node contains based on a pattern search.
     * 
     * @param out Writer for output - relative paths separated by newline characters
     * @param surfConfigRef Surf-Config folder
     * @param fileInfo The FileInfo node to use as the parent
     * @param pattern Optional pattern to match filenames against ("*" is match all)
     * @param recurse True to recurse sub-directories
     * @throws IOException
     */
    private void outputFileNodes(Writer out, FileInfo fileInfo, NodeRef surfConfigRef, String pattern, boolean recurse) throws IOException
    {
        if (surfConfigRef != null)
        {
            final boolean debug = logger.isDebugEnabled();
            PagingResults<FileInfo> files = getFileNodes(fileInfo, pattern, recurse);
            
            final Map<NodeRef, String> nameCache = new HashMap<NodeRef, String>();
            for (final FileInfo file : files.getPage())
            {
                // walking up the parent tree manually until the "surf-config" parent is hit
                // and manually appending the rest of the cm:name path down to the node.
                final StringBuilder displayPath = new StringBuilder(64);
                NodeRef ref = unprotNodeService.getPrimaryParent(file.getNodeRef()).getParentRef();
                while (!ref.equals(surfConfigRef))
                {
                    String name = nameCache.get(ref);
                    if (name == null)
                    {
                        name = (String) unprotNodeService.getProperty(ref, ContentModel.PROP_NAME);
                        nameCache.put(ref, name);
                    }
                    displayPath.insert(0, '/');
                    displayPath.insert(0, name);
                    ref = unprotNodeService.getPrimaryParent(ref).getParentRef();
                }
                
                out.write("/alfresco/site-data/");
                out.write(URLDecoder.decode(displayPath.toString()));
                out.write(URLDecoder.decode(file.getName()));
                out.write('\n');
                if (debug) logger.debug("   /alfresco/site-data/" + displayPath.toString() + file.getName());
            }
        }
    }
    
    protected PagingResults<FileInfo> getFileNodes(FileInfo fileInfo, String pattern, boolean recurse)
    {
        return fileFolderService.list(
                fileInfo.getNodeRef(), true, false,
                pattern, null, null,
                new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
    }
}
