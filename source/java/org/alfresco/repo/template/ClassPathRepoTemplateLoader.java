/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ApplicationContextHelper;

import freemarker.cache.TemplateLoader;

/**
 * Custom FreeMarker template loader to locate templates stored either from the ClassPath
 * or in a Alfresco Repository.
 * <p>
 * The template name should be supplied either as a NodeRef String or a ClassPath path String.
 * 
 * @author Kevin Roast
 */
public class ClassPathRepoTemplateLoader implements TemplateLoader
{
    private NodeService nodeService;
    private ContentService contentService;
    
    public ClassPathRepoTemplateLoader(NodeService nodeService, ContentService contentService)
    {
        if (nodeService == null)
        {
            throw new IllegalArgumentException("NodeService is mandatory.");
        }
        if (contentService == null)
        {
            throw new IllegalArgumentException("ContentService is mandatory.");
        }
        this.nodeService = nodeService;
        this.contentService = contentService;
    }
    
    /**
     * Return an object wrapping a source for a template
     */
    public Object findTemplateSource(String name)
        throws IOException
    {
        if (name.indexOf(StoreRef.URI_FILLER) != -1)
        {
            NodeRef ref = new NodeRef(name);
            if (this.nodeService.exists(ref) == true)
            {
                return new RepoTemplateSource(ref);
            }
            else
            {
                return null;
            }
        }
        else
        {
            URL url = this.getClass().getClassLoader().getResource(name);
            return url == null ? null : new ClassPathTemplateSource(url);
        }
    }
    
    public long getLastModified(Object templateSource)
    {
        return ((BaseTemplateSource)templateSource).lastModified();
    }
    
    public Reader getReader(Object templateSource, String encoding) throws IOException
    {
        return ((BaseTemplateSource)templateSource).getReader();
    }
    
    public void closeTemplateSource(Object templateSource) throws IOException
    {
        ((BaseTemplateSource)templateSource).close();
    }
    
    
    /**
     * Class used as a base for custom Template Source objects
     */
    abstract class BaseTemplateSource
    {
        public abstract Reader getReader() throws IOException;
        
        public abstract void close() throws IOException;
        
        public abstract long lastModified();
    }
    
    
    /**
     * Class providing a ClassPath based Template Source
     */
    class ClassPathTemplateSource extends BaseTemplateSource
    {
        private final URL url;
        private URLConnection conn;
        private InputStream inputStream;
        
        ClassPathTemplateSource(URL url) throws IOException
        {
            this.url = url;
            this.conn = url.openConnection();
        }
        
        public boolean equals(Object o)
        {
            if (o instanceof ClassPathTemplateSource)
            {
                return url.equals(((ClassPathTemplateSource)o).url);
            }
            else
            {
                return false;
            }
        }
        
        public int hashCode()
        {
            return url.hashCode();
        }
        
        public String toString()
        {
            return url.toString();
        }
        
        public long lastModified()
        {
            return conn.getLastModified();
        }
        
        public Reader getReader() throws IOException
        {
            inputStream = conn.getInputStream();
            return new InputStreamReader(inputStream);
        }
        
        public void close() throws IOException
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            finally
            {
                inputStream = null;
                conn = null;
            }
        }
    }
    
    /**
     * Class providing a Repository based Template Source
     */
    class RepoTemplateSource extends BaseTemplateSource
    {
        private final NodeRef nodeRef;
        private InputStream inputStream;
        private ContentReader conn;
        
        RepoTemplateSource(NodeRef ref) throws IOException
        {
            this.nodeRef = ref;
            this.conn = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        }
        
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
        
        public int hashCode()
        {
            return nodeRef.hashCode();
        }
        
        public String toString()
        {
            return nodeRef.toString();
        }
        
        public long lastModified()
        {
            return conn.getLastModified();
        }
        
        public Reader getReader() throws IOException
        {
            inputStream = conn.getContentInputStream();
            return new InputStreamReader(inputStream);
        }
        
        public void close() throws IOException
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            finally
            {
                inputStream = null;
                conn = null;
            }
        }
    }
}
