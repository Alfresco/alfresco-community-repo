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
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
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
    private static final Pattern USER_PATTERN_1 = Pattern.compile(".*/components/.*\\.user~(.*)~.*");
    private static final Pattern USER_PATTERN_2 = Pattern.compile(".*/pages/user/(.*?)(/.*)?$");
    private static final Pattern SITE_PATTERN_1 = Pattern.compile(".*/components/.*\\.site~(.*)~.*");
    private static final Pattern SITE_PATTERN_2 = Pattern.compile(".*/pages/site/(.*?)(/.*)?$");
    
    // service beans
    private NodeService nodeService;
    private NodeService unprotNodeService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private SiteService siteService;
    private ContentService contentService;
    
    
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
                    res.setHeader("Last-Modified", Long.toString(fileInfo.getModifiedDate().getTime()));
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
            // do not support filenames directly at the root - all objectypes must exist in a named child folder
            final String encpath = encodePath(path);
            final int off = encpath.lastIndexOf('/');
            if (off != -1)
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
                        FileInfo fileInfo = fileFolderService.create(
                                parentFolder.getNodeRef(), encpath.substring(off + 1), ContentModel.TYPE_CONTENT);
                        contentService.getWriter(
                                fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true).putContent(content);
                        if (logger.isDebugEnabled())
                            logger.debug("createDocument: " + fileInfo.toString());
                        return null;
                    }
                }, runAsUser);
            }
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
        }
        catch (FileExistsException feeErr)
        {
            res.setStatus(Status.STATUS_CONFLICT);
        }
    }
    
    /**
     * Creates multiple XML documents encapsulated in a single one. 
     * 
     * @param content       XML document containing multiple document contents to write
     */
    @Override
    protected void createDocuments(WebScriptResponse res, String store, InputStream content)
    {
        // no implementation currently
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
        final String encpath = encodePath(path);
        final FileInfo fileInfo = resolveFilePath(encpath);
        if (fileInfo == null || fileInfo.isFolder())
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
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
        }
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
            this.nodeService.deleteNode(fileRef);
            if (logger.isDebugEnabled())
                logger.debug("deleteDocument: " + fileInfo.toString());
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
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
                    if (create)
                    {
                        // ensure folders exist down to the specified parent
                        result = FileFolderUtil.makeFolders(
                                this.fileFolderService,
                                surfConfigRef,
                                isFolder ? pathElements : pathElements.subList(0, pathElements.size() - 1),
                                ContentModel.TYPE_FOLDER);
                    }
                    else
                    {
                        // perform the cm:name path lookup against our config root node
                        if (surfConfigRef != null)
                        {
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
     * 
     * @param rootRef   Root node reference where the "surf-config" folder should live
     * @param create    True to create the folder if missing, false otherwise
     * 
     * @return surf-config folder ref if found, null otherwise if not creating
     */
    private NodeRef getSurfConfigNodeRef(final NodeRef rootRef, final boolean create)
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
        }
        return surfConfigRef;
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
     * @param out       Writer for output - relative paths separated by newline characters
     * @param surfConfigRef Surf-Config folder
     * @param fileInfo  The FileInfo node to use as the parent
     * @param pattern   Optional pattern to match filenames against ("*" is match all)
     * @param recurse   True to recurse sub-directories
     * 
     * @throws IOException
     */
    private void outputFileNodes(Writer out, FileInfo fileInfo, NodeRef surfConfigRef, String pattern, boolean recurse)
        throws IOException
    {
        final boolean debug = logger.isDebugEnabled();
        final Map<NodeRef, String> nameCache = new HashMap<NodeRef, String>();
        PagingResults<FileInfo> files = fileFolderService.list(
                fileInfo.getNodeRef(), true, false, pattern, null, null,
                new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
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
                    name = (String)unprotNodeService.getProperty(ref, ContentModel.PROP_NAME);
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
