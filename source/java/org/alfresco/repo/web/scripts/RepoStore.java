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
package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.webscripts.AbstractStore;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.ScriptLoader;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptException;

import freemarker.cache.TemplateLoader;


/**
 * Repository based Web Script Store
 * 
 * @author davidc
 */
public class RepoStore extends AbstractStore implements TenantDeployer
{
    protected boolean mustExist = false;
    protected StoreRef repoStore;
    protected String repoPath;
    protected Map<String, NodeRef> baseNodeRefs;

    // dependencies
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected FileFolderService fileService;
    protected NamespaceService namespaceService;
    protected PermissionService permissionService;
    protected TenantAdminService tenantAdminService;

    
    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * Sets the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Sets the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Sets the file service
     */
    public void setFileFolderService(FileFolderService fileService)
    {
        this.fileService = fileService;
    }

    /**
     * Sets the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Sets the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Sets the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Sets whether the repo store must exist
     * 
     * @param mustExist
     */
    public void setMustExist(boolean mustExist)
    {
        this.mustExist = mustExist;
    }
    
    /**
     * Sets the repo store
     */
    public void setStore(String repoStore)
    {
        this.repoStore = new StoreRef(repoStore);
    }
    
    /**
     * Sets the repo path
     * 
     * @param repoPath  repoPath
     */
    public void setPath(String repoPath)
    {
        this.repoPath = repoPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#init()
     * @see org.alfresco.repo.tenant.TenantDeployer#init()
     */
    public void init()
    {
        if (baseNodeRefs == null)
        {
    		baseNodeRefs = new HashMap<String, NodeRef>(1);
    	}
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#destroy()
     */
    public void destroy()
    {
        baseNodeRefs.remove(tenantAdminService.getCurrentUserDomain());
    }
    
    private NodeRef getBaseNodeRef()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        NodeRef baseNodeRef = baseNodeRefs.get(tenantDomain);
        if (baseNodeRef == null)
        {
            baseNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
            	public NodeRef doWork() throws Exception
                {
    	            return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
    	            {
                        public NodeRef execute() throws Exception
                        {
                            NodeRef repoStoreRootNodeRef = nodeService.getRootNode(repoStore);
                            List<NodeRef> nodeRefs = searchService.selectNodes(
                                    repoStoreRootNodeRef,
                                    repoPath,
                                    new QueryParameterDefinition[] {},
                                    namespaceService,
                                    false,
                                    SearchService.LANGUAGE_XPATH);
                            if (nodeRefs.size() == 1)
                            {
                                return nodeRefs.get(0);
                            }
                            else if (nodeRefs.size() > 1)
                            {
                                throw new WebScriptException(
                                        "Web Script Store " + repoStore.toString() + repoPath + " must exist; multiple entries found.");
                            }
                            else
                            {
                                throw new WebScriptException(
                                        "Web Script Store " + repoStore.toString() + repoPath + " must exist; it was not found");
                            }
                        }
                    }, true, false);
                }
    	    }, AuthenticationUtil.getSystemUserName());
    		
    		// TODO clear on deleteTenant
    		baseNodeRefs.put(tenantDomain, baseNodeRef);
    	}
    	return baseNodeRef;
    }
    
    private String getBaseDir()
    {
    	return getPath(getBaseNodeRef());
    }
        
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#isSecure()
     */
    public boolean isSecure()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#exists()
     */
    public boolean exists()
    {
        return (getBaseNodeRef() != null);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getBasePath()
     */
    public String getBasePath()
    {
        return repoStore.toString() + repoPath;
    }

    /**
     * Gets the display path for the specified node
     * 
     * @param nodeRef
     * @return  display path
     */
    protected String getPath(NodeRef nodeRef)
    {
        return nodeService.getPath(nodeRef).toDisplayPath(nodeService, permissionService) +
               "/" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }
    
    /**
     * Gets the node ref for the specified path within this repo store
     * 
     * @param documentPath
     * @return  node ref
     */
    protected NodeRef findNodeRef(String documentPath)
    {
        NodeRef node = null;
        try
        {
            String[] pathElements = documentPath.split("/");
            List<String> pathElementsList = Arrays.asList(pathElements);
            FileInfo file = fileService.resolveNamePath(getBaseNodeRef(), pathElementsList);
            node = file.getNodeRef();
        }
        catch (FileNotFoundException e)
        {
            // NOTE: return null
        }
        return node;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getScriptDocumentPaths(org.alfresco.web.scripts.WebScript)
     */
    public String[] getScriptDocumentPaths(final WebScript script)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String[]>()
        {
            public String[] doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<String[]>()
                {
                    public String[] execute() throws Exception
                    {
                        int baseDirLength = getBaseDir().length() +1;
                        List<String> documentPaths = null;
                        String scriptPath = script.getDescription().getScriptPath();
                        NodeRef scriptNodeRef = (scriptPath.length() == 0) ? getBaseNodeRef() : findNodeRef(scriptPath);
                        if (scriptNodeRef != null)
                        {
                            org.alfresco.service.cmr.repository.Path repoScriptPath = nodeService.getPath(scriptNodeRef);
                            String id = script.getDescription().getId().substring(scriptPath.length() + (scriptPath.length() > 0 ? 1 : 0));
                            String query = "+PATH:\"" + repoScriptPath.toPrefixString(namespaceService) +
                                           "//*\" +QNAME:" + lucenifyNamePattern(id) + "*";
                            ResultSet resultSet = searchService.query(repoStore, SearchService.LANGUAGE_LUCENE, query);
                            try
                            {
                                documentPaths = new ArrayList<String>(resultSet.length());
                                List<NodeRef> nodes = resultSet.getNodeRefs();
                                for (NodeRef nodeRef : nodes)
                                {
                                    String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                                    if (name.startsWith(id))
                                    {
                                        String nodeDir = getPath(nodeRef);
                                        String documentPath = nodeDir.substring(baseDirLength);
                                        documentPaths.add(documentPath);
                                    }
                                }
                            }
                            finally
                            {
                                resultSet.close();
                            }
                        }
                        
                        return documentPaths != null ? documentPaths.toArray(new String[documentPaths.size()]) : new String[0];
                    }
                }, true, false);
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getDocumentPaths(java.lang.String, boolean, java.lang.String)
     */
    public String[] getDocumentPaths(String path, boolean includeSubPaths, String documentPattern)
    {
        if ((documentPattern == null) || (documentPattern.length() == 0))
        {
            documentPattern = "*";
        }
        
        String matcher = documentPattern.replace(".","\\.").replace("*",".*");
        final Pattern pattern = Pattern.compile(matcher);
        
        String encPath = encodePathISO9075(path);
        final StringBuilder query = new StringBuilder(128);
        query.append("+PATH:\"").append(repoPath)
             .append(encPath.length() != 0 ? ('/' + encPath) : "")
             .append((includeSubPaths ? '/' : ""))
             .append("/*\" +QNAME:")
             .append(lucenifyNamePattern(documentPattern));
        
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String[]>()
        {
            public String[] doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<String[]>()
                {
                    public String[] execute() throws Exception
                    {
                        int baseDirLength = getBaseDir().length() +1;
                        
                        List<String> documentPaths;
                        ResultSet resultSet = searchService.query(repoStore, SearchService.LANGUAGE_LUCENE, query.toString());
                        try
                        {
                            documentPaths = new ArrayList<String>(resultSet.length());
                            List<NodeRef> nodes = resultSet.getNodeRefs();
                            for (NodeRef nodeRef : nodes)
                            {
                                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                                if (pattern.matcher(name).matches())
                                {
                                    String nodeDir = getPath(nodeRef);
                                    String documentPath = nodeDir.substring(baseDirLength);
                                    documentPaths.add(documentPath);
                                }
                            }
                        }
                        finally
                        {
                            resultSet.close();
                        }
                        return documentPaths.toArray(new String[documentPaths.size()]);
                    }
                }, true, false);
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Helper to encode the elements of a path to be used as a Lucene PATH statement
     * using the ISO9075 encoding. Note that leading and trailing '/' elements will NOT
     * be preserved.
     * 
     * @param path  Path to encode, elements separated by '/'
     * 
     * @return the encoded path, a minimum of the empty string will be returned
     */
    public static String encodePathISO9075(String path)
    {
        if (path == null || path.length() == 0)
        {
            return "";
        }
        StringBuilder result = new StringBuilder(path.length() + 16);
        for (StringTokenizer t = new StringTokenizer(path, "/"); t.hasMoreTokens(); /**/)
        {
            result.append(ISO9075.encode(t.nextToken()));
            if (t.hasMoreTokens())
            {
                result.append('/');
            }
        }
        return result.toString();
    }
    
    /**
     * ALF-7059: Because we can't quote QNAME patterns, and because characters like minus have special meaning, we have
     * to pass the document 'pattern' though the Lucene escaper, preserving the wildcard parts. Also, because you can't
     * search for whitespace in a QNAME, we have to replace whitespace with the ? wildcard
     * 
     * @param pattern
     * @return
     */
    private static String lucenifyNamePattern(String pattern)
    {
        // Assume already escaped if the pattern includes a backslash
        if (pattern.indexOf('\\') != -1)
        {
            return pattern;
        }
        StringBuilder result = new StringBuilder(pattern.length() * 2);
        StringTokenizer tkn = new StringTokenizer(pattern, "\t\r\n *", true);
        while (tkn.hasMoreTokens())
        {
            String token = tkn.nextToken();
            if (token.length() == 1)
            {
                char c = token.charAt(0);
                if (Character.isWhitespace(c))
                {
                    // We can't include whitespace in a QNAME expression so we will have to use a wildcard character and
                    // filter the results later
                    result.append('?');
                }
                else if (c == '*')
                {
                    result.append(c);
                }
                else
                {
                    result.append(LuceneQueryParser.escape(token));
                }
            }
            else
            {
                result.append(LuceneQueryParser.escape(token));
            }
        }
        return result.toString();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getDescriptionDocumentPaths()
     */
    public String[] getDescriptionDocumentPaths()
    {
        return getDocumentPaths("/", true, DESC_PATH_PATTERN);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getAllDocumentPaths()
     */
    public String[] getAllDocumentPaths()
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String[]>()
        {
            public String[] doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<String[]>()
                {
                    public String[] execute() throws Exception
                    {
                        int baseDirLength = getBaseDir().length() +1;
                        
                        List<String> documentPaths;
                        String query = "+PATH:\"" + repoPath +
                                       "//*\" +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
                        ResultSet resultSet = searchService.query(repoStore, SearchService.LANGUAGE_LUCENE, query);
                        try
                        {
                            documentPaths = new ArrayList<String>(resultSet.length());
                            List<NodeRef> nodes = resultSet.getNodeRefs();
                            for (NodeRef nodeRef : nodes)
                            {
                                String nodeDir = getPath(nodeRef);
                                documentPaths.add(nodeDir.substring(baseDirLength));
                            }
                        }
                        finally
                        {
                            resultSet.close();
                        }
                        
                        return documentPaths.toArray(new String[documentPaths.size()]);
                    }
                }, true, false);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#lastModified(java.lang.String)
     */
    public long lastModified(final String documentPath) throws IOException
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Long>()
        {
            public Long doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                {
                    public Long execute() throws Exception
                    {
                        ContentReader reader = contentService.getReader(
                                findNodeRef(documentPath), ContentModel.PROP_CONTENT);
                        return reader.getLastModified();
                    }
                }, true, false);
            }
        }, AuthenticationUtil.getSystemUserName());            
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#hasDocument(java.lang.String)
     */
    public boolean hasDocument(final String documentPath)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    public Boolean execute() throws Exception
                    {
                        NodeRef nodeRef = findNodeRef(documentPath);
                        return (nodeRef != null);
                    }
                }, true, false);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getDescriptionDocument(java.lang.String)
     */
    public InputStream getDocument(final String documentPath)      
        throws IOException
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<InputStream>()
        {
            public InputStream doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<InputStream>()
                {
                    public InputStream execute() throws Exception
                    {
                        NodeRef nodeRef = findNodeRef(documentPath);
                        if (nodeRef == null)
                        {
                            throw new IOException("Document " + documentPath + " does not exist.");
                        }
                        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                        if (reader == null || !reader.exists())
                        {
                            throw new IOException("Failed to read content at " + documentPath + " (content reader does not exist)");
                        }
                        return reader.getContentInputStream();
                    }
                }, true, false);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#createDocument(java.lang.String, java.lang.String)
     */
    public void createDocument(String documentPath, String content) throws IOException
    {
        String[] pathElements = documentPath.split("/");
        String[] folderElements = new String[pathElements.length -1];
        System.arraycopy(pathElements, 0, folderElements, 0, pathElements.length -1);
        List<String> folderElementsList = Arrays.asList(folderElements);
        
        // create folder
        FileInfo pathInfo;
        if (folderElementsList.size() == 0)
        {
            pathInfo = fileService.getFileInfo(getBaseNodeRef());
        }
        else
        {
            pathInfo = FileFolderServiceImpl.makeFolders(fileService, getBaseNodeRef(), folderElementsList, ContentModel.TYPE_FOLDER);
        }

        // create file
        String fileName = pathElements[pathElements.length -1];
        if (fileService.searchSimple(pathInfo.getNodeRef(), fileName) != null)
        {
            throw new IOException("Document " + documentPath + " already exists");
        }
        FileInfo fileInfo = fileService.create(pathInfo.getNodeRef(), fileName, ContentModel.TYPE_CONTENT);
        ContentWriter writer = fileService.getWriter(fileInfo.getNodeRef());
        writer.putContent(content);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#updateDocument(java.lang.String, java.lang.String)
     */
    public void updateDocument(String documentPath, String content) throws IOException
    {
        String[] pathElements = documentPath.split("/");
        
        // get parent folder
        NodeRef parentRef;
        if (pathElements.length == 1)
        {
            parentRef = getBaseNodeRef();
        }
        else
        {
            parentRef = findNodeRef(documentPath.substring(0, documentPath.lastIndexOf('/')));
        }

        // update file
        String fileName = pathElements[pathElements.length -1];
        if (fileService.searchSimple(parentRef, fileName) == null)
        {
            throw new IOException("Document " + documentPath + " does not exists");
        }
        FileInfo fileInfo = fileService.create(parentRef, fileName, ContentModel.TYPE_CONTENT);
        ContentWriter writer = fileService.getWriter(fileInfo.getNodeRef());
        writer.putContent(content);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#removeDocument(java.lang.String)
     */
    public boolean removeDocument(String documentPath)
        throws IOException
    {
        // TODO: Implement remove for Repository Store
        return false;
    }    
    

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getTemplateLoader()
     */
    public TemplateLoader getTemplateLoader()
    {
        return new RepoTemplateLoader();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Store#getScriptLoader()
     */
    public ScriptLoader getScriptLoader()
    {
        return new RepoScriptLoader();
    }        
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onEnableTenant()
     */
    public void onEnableTenant()
    {
        init();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onDisableTenant()
     */
    public void onDisableTenant()
    {
        destroy();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return repoPath;
    }
    
    /**
     * Repository path based template loader
     * 
     * @author davidc
     */
    private class RepoTemplateLoader implements TemplateLoader
    {
        /* (non-Javadoc)
         * @see freemarker.cache.TemplateLoader#findTemplateSource(java.lang.String)
         */
        public Object findTemplateSource(final String name)
            throws IOException
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Exception
                        {
                            RepoTemplateSource source = null;
                            NodeRef nodeRef = findNodeRef(name);
                            if (nodeRef != null)
                            {
                                source = new RepoTemplateSource(nodeRef);
                            }
                            return source;
                        }
                    }, true);
                }
            }, AuthenticationUtil.getSystemUserName());
        }

        /* (non-Javadoc)
         * @see freemarker.cache.TemplateLoader#getLastModified(java.lang.Object)
         */
        public long getLastModified(Object templateSource)
        {
            return ((RepoTemplateSource)templateSource).lastModified();
        }
        
        /* (non-Javadoc)
         * @see freemarker.cache.TemplateLoader#getReader(java.lang.Object, java.lang.String)
         */
        public Reader getReader(Object templateSource, String encoding) throws IOException
        {
            return ((RepoTemplateSource)templateSource).getReader();
        }

        /* (non-Javadoc)
         * @see freemarker.cache.TemplateLoader#closeTemplateSource(java.lang.Object)
         */
        public void closeTemplateSource(Object arg0) throws IOException
        {
        }
    }

    /**
     * Repository (content) node template source
     *
     * @author davidc
     */
    private class RepoTemplateSource
    {
        protected final NodeRef nodeRef;

        /**
         * Construct
         * 
         * @param ref
         */
        private RepoTemplateSource(NodeRef ref)
        {
            this.nodeRef = ref;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o)
        {
            if (o instanceof RepoTemplateSource)
            {
                return nodeRef.equals(((RepoTemplateSource)o).nodeRef);
            }
            else
            {
                return false;
            }
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return nodeRef.hashCode();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return nodeRef.toString();
        }
        
        /**
         * Gets the last modified time of the content
         * 
         * @return  last modified time
         */
        public long lastModified()
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Long>()
            {
                public Long doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {
                        public Long execute() throws Exception
                        {
                            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                            return reader.getLastModified();
                        }
                    });
                }
            }, AuthenticationUtil.getSystemUserName());            
        }
        
        /**
         * Gets the content reader
         * 
         * @return  content reader
         * @throws IOException
         */
        public Reader getReader() throws IOException
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Reader>()
            {
                public Reader doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Reader>()
                    {
                        public Reader execute() throws Exception
                        {
                            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                            return new InputStreamReader(reader.getContentInputStream(), reader.getEncoding());
                        }
                    });
                }
            }, AuthenticationUtil.getSystemUserName());            
        }
    }
        
    /**
     * Repository path based script loader
     * 
     * @author davidc
     */
    private class RepoScriptLoader implements ScriptLoader
    {
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.ScriptLoader#getScriptLocation(java.lang.String)
         */
        public ScriptContent getScript(final String path)
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ScriptContent>()
            {
                public ScriptContent doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<ScriptContent>()
                    {
                        public ScriptContent execute() throws Exception
                        {
                            ScriptContent location = null;
                            NodeRef nodeRef = findNodeRef(path);
                            if (nodeRef != null)
                            {
                                location = new RepoScriptContent(path, nodeRef);
                            }
                            return location;
                        }
                    }, true, false);
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }
    
    /**
     * Repo path script location
     * 
     * @author davidc
     */
    private class RepoScriptContent implements ScriptContent
    {
        protected String path;
        protected NodeRef nodeRef;

        /**
         * Construct
         * 
         * @param location
         */
        public RepoScriptContent(String path, NodeRef nodeRef)
        {
            this.path = path;
            this.nodeRef = nodeRef;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getInputStream()
         */
        public InputStream getInputStream()
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<InputStream>()
            {
                public InputStream doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<InputStream>()
                    {
                        public InputStream execute() throws Exception
                        {
                            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                            return reader.getContentInputStream();
                        }
                    });
                }
            }, AuthenticationUtil.getSystemUserName());            
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getReader()
         */
        public Reader getReader()
        {
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            try
            {
                return new InputStreamReader(getInputStream(), reader.getEncoding());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new AlfrescoRuntimeException("Unsupported Encoding", e);
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.ScriptContent#getPath()
         */
		public String getPath()
		{
            return repoStore + getBaseDir() + "/" + path;
		}
		
		/* (non-Javadoc)
		 * @see org.alfresco.web.scripts.ScriptContent#getPathDescription()
		 */
		public String getPathDescription()
		{
		    return "/" + path + " (in repository store " + repoStore.toString() + getBaseDir() + ")";
		}
		
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.ScriptContent#isCachable()
         */
        public boolean isCachable()
        {
            return false;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.ScriptContent#isSecure()
         */
        public boolean isSecure()
        {
            return false;
        }
    }
}
