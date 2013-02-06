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
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class ShareResourceStore extends BaseRemoteStore
{
    private static final Log logger = LogFactory.getLog(ShareResourceStore.class);

 // service beans
    private NodeService nodeService;
    private NodeService unprotNodeService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private SiteService siteService;
    private ContentService contentService;
    private HiddenAspect hiddenAspect;
    
    
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
    
    private Repository repositoryHelper;
    /**
     * 
     */
    @Override
    protected void getDocument(final WebScriptResponse res, final String store, final String path) throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>() {
           @SuppressWarnings("synthetic-access")
           public Void doWork() throws Exception {
               final String encpath = encodePath(path);
//             
//             this.nodeService.getRootNode(storeRef)
                 NodeRef rootNode = repositoryHelper.getCompanyHome();
                 FileInfo fileInfo = null;
                 List<String> pathElements = new ArrayList<String>(4);
                 final StringTokenizer t = new StringTokenizer(path, "/");
                 while (t.hasMoreTokens())
                 {
                     pathElements.add(t.nextToken());
                 }
                 
                 try
                 {
                     fileInfo = fileFolderService.resolveNamePath(rootNode, pathElements);
                     
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
                 }
                 catch (FileNotFoundException e)
                 {
                     res.setStatus(Status.STATUS_NOT_FOUND);
                 }
                 return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void lastModified(WebScriptResponse res, String store, String path) throws IOException
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void hasDocument(WebScriptResponse res, String store, String path) throws IOException
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }
    
    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void listDocuments(WebScriptResponse res, String store, String path, boolean recurse) throws IOException
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void listDocuments(WebScriptResponse res, String store, String path, String pattern) throws IOException
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void createDocument(WebScriptResponse res, String store, String path, InputStream content)
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void createDocuments(WebScriptResponse res, String store, InputStream content)
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void updateDocument(WebScriptResponse res, String store, String path, InputStream content)
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    /**
     * Returns an HTTP 501 because this class does not support this API
     */
    @Override
    protected void deleteDocument(WebScriptResponse res, String store, String path)
    {
        res.setStatus(Status.STATUS_NOT_IMPLEMENTED);
    }

    public Repository getRepositoryHelper()
    {
        return repositoryHelper;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }
}
