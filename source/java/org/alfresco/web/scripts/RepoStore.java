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
package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import freemarker.cache.TemplateLoader;


/**
 * Repository based Web Script Store
 * 
 * @author davidc
 */
public class RepoStore implements WebScriptStore, ApplicationContextAware, ApplicationListener
{
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();
    protected StoreRef repoStore;
    protected String repoPath;
    protected NodeRef baseNodeRef;
    protected String baseDir;

    // dependencies
    protected TransactionService transactionService;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected NamespaceService namespaceService;

    
    /**
     * Sets transaction service
     * 
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Sets the search service
     * 
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Sets the node service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the content service
     * 
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Sets the namespace service
     * 
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Sets the repo store
     * 
     * @param repoStore
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
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * Hooks into Spring Application Lifecycle
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            init();
        }
    
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    protected void init()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        String query = "PATH:\"" + repoPath + "\"";
                        ResultSet resultSet = searchService.query(repoStore, SearchService.LANGUAGE_LUCENE, query);
                        if (resultSet.length() == 0)
                        {
                            throw new WebScriptException("Unable to locate repository path " + repoStore.toString() + repoPath);
                        }
                        if (resultSet.length() > 1)
                        {
                            throw new WebScriptException("Multiple repository paths found for " + repoStore.toString() + repoPath);
                        }
                        baseNodeRef = resultSet.getNodeRef(0);
                        baseDir = getPath(baseNodeRef);
                        return null;
                    }
                });
            }
        }, AuthenticationUtil.getSystemUserName());
    }
        
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getBasePath()
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
        return nodeService.getPath(nodeRef).toDisplayPath(nodeService) + "/" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }
    
    /**
     * Gets the node ref for the specified path within this repo store
     * 
     * @param documentPath
     * @return  node ref
     */
    protected NodeRef findNodeRef(String documentPath)
    {
        StringBuilder xpath = new StringBuilder(documentPath.length() << 1);
        for (StringTokenizer t = new StringTokenizer(documentPath, "/"); t.hasMoreTokens(); /**/)
        {
            if (xpath.length() != 0)
            {
                xpath.append('/');
            }
            xpath.append("*[@cm:name='").append(t.nextToken()).append("']");
        }
        
        List<NodeRef> nodes = searchService.selectNodes(baseNodeRef, xpath.toString(), null, namespaceService, false);
        return (nodes.size() == 1) ? nodes.get(0) : null; 
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getDescriptionDocumentPaths()
     */
    public String[] getDescriptionDocumentPaths()
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String[]>()
        {
            public String[] doWork() throws Exception
            {
                return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<String[]>()
                {
                    public String[] doWork() throws Exception
                    {
                        int baseDirLength = baseDir.length() +1;
                        List<String> documentPaths = new ArrayList<String>();
                        
                        String query = "PATH:\"" + repoPath + "//*\" AND @cm\\:name:\"*_desc.xml\"";
                        ResultSet resultSet = searchService.query(repoStore, SearchService.LANGUAGE_LUCENE, query);
                        List<NodeRef> nodes = resultSet.getNodeRefs();
                        for (NodeRef nodeRef : nodes)
                        {
                            String nodeDir = getPath(nodeRef);
                            String documentPath = nodeDir.substring(baseDirLength);
                            documentPaths.add(documentPath);
                        }
                        
                        return documentPaths.toArray(new String[documentPaths.size()]);
                    }
                });
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getDescriptionDocument(java.lang.String)
     */
    public InputStream getDescriptionDocument(final String documentPath)      
        throws IOException
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<InputStream>()
        {
            public InputStream doWork() throws Exception
            {
                return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<InputStream>()
                {
                    public InputStream doWork() throws Exception
                    {
                        NodeRef nodeRef = findNodeRef(documentPath);
                        if (nodeRef == null)
                        {
                            throw new IOException("Description document " + documentPath + " does not exist.");
                        }
                        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                        return reader.getContentInputStream();
                    }
                });
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getTemplateLoader()
     */
    public TemplateLoader getTemplateLoader()
    {
        return new RepoTemplateLoader();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptStore#getScriptLoader()
     */
    public ScriptLoader getScriptLoader()
    {
        return new RepoScriptLoader();
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
                    return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            RepoTemplateSource source = null;
                            NodeRef nodeRef = findNodeRef(name);
                            if (nodeRef != null)
                            {
                                source = new RepoTemplateSource(nodeRef);
                            }
                            return source;
                        }
                    });
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
                    return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<Long>()
                    {
                        public Long doWork() throws Exception
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
                    return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<Reader>()
                    {
                        public Reader doWork() throws Exception
                        {
                            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                            return new InputStreamReader(reader.getContentInputStream());
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
        public ScriptLocation getScriptLocation(final String path)
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ScriptLocation>()
            {
                public ScriptLocation doWork() throws Exception
                {
                    return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<ScriptLocation>()
                    {
                        public ScriptLocation doWork() throws Exception
                        {
                            ScriptLocation location = null;
                            NodeRef nodeRef = findNodeRef(path);
                            if (nodeRef != null)
                            {
                                location = new RepoScriptLocation(path, nodeRef);
                            }
                            return location;
                        }
                    });
                }
            }, AuthenticationUtil.getSystemUserName());            
        }
    }
    
    /**
     * Repo path script location
     * 
     * @author davidc
     */
    private class RepoScriptLocation implements ScriptLocation
    {
        protected String path;
        protected NodeRef nodeRef;

        /**
         * Construct
         * 
         * @param location
         */
        public RepoScriptLocation(String path, NodeRef nodeRef)
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
                    return TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<InputStream>()
                    {
                        public InputStream doWork() throws Exception
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
            return new InputStreamReader(getInputStream());
        }

        @Override
        public String toString()
        {
            return repoStore + "/" + baseDir + "/" + path;
        }
    }

}
