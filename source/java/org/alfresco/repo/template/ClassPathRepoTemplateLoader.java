package org.alfresco.repo.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;

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
    private String encoding;
    
    public ClassPathRepoTemplateLoader(NodeService nodeService, ContentService contentService, String encoding)
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
        this.encoding = encoding;
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
            //Fix a common problem: classpath resource paths should not start with "/"
            if (name.startsWith("/"))
            {
                name = name.substring(1);
            }
            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource(name);
            return url == null ? null : new ClassPathTemplateSource(classLoader, name, encoding);
        }
    }
    
    public long getLastModified(Object templateSource)
    {
        return ((BaseTemplateSource)templateSource).lastModified();
    }
    
    public Reader getReader(Object templateSource, String encoding) throws IOException
    {
        if (encoding != null)
        {
            return ((BaseTemplateSource)templateSource).getReader(encoding);
        }
        else
        {
            return ((BaseTemplateSource)templateSource).getReader(this.encoding);
        }
    }
    
    public void closeTemplateSource(Object templateSource) throws IOException
    {
        ((BaseTemplateSource)templateSource).close();
    }
    
    
    /**
     * Class used as a base for custom Template Source objects
     */
    abstract class BaseTemplateSource implements TemplateSource
    {
        public abstract Reader getReader(String encoding) throws IOException;
        
        public abstract void close() throws IOException;
        
        public abstract long lastModified();
        
        public InputStream getResource(String name)
        {
            return getRelativeResource(name);
        }
        
        protected abstract InputStream getRelativeResource(String name);
    }
    
    
    /**
     * Class providing a ClassPath based Template Source
     */
    class ClassPathTemplateSource extends BaseTemplateSource
    {
        private final URL url;
        private URLConnection conn;
        private InputStream inputStream;
        private String encoding;
        private ClassLoader classLoader;
        private String resourceName;
        
        ClassPathTemplateSource(ClassLoader classLoader, String name, String encoding) throws IOException
        {
            this.classLoader = classLoader;
            this.resourceName = name;
            this.url = classLoader.getResource(name);
            this.conn = url.openConnection();
            this.encoding = encoding;
        }
        
        public String toString()
        {
            return url.toString();
        }
        
        public long lastModified()
        {
            return conn.getLastModified();
        }
        
        public Reader getReader(String encoding) throws IOException
        {
            inputStream = conn.getInputStream();
            if (encoding != null)
            {
                return new InputStreamReader(inputStream, encoding);
            }
            else
            {
                return new InputStreamReader(inputStream);
            }
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

        @Override
        protected InputStream getRelativeResource(String name)
        {
            String newResourceName = name;
            if (!name.startsWith("/"))
            {
                int lastSlash = resourceName.lastIndexOf('/');
                if (lastSlash != -1)
                {
                    newResourceName = name.substring(0, lastSlash) + "/" + name; 
                }
            }
            URL url = classLoader.getResource(newResourceName);
            try
            {
                return (url == null) ? null : url.openConnection().getInputStream();
            } 
            catch (IOException e)
            {
                return null;
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
        
        public Reader getReader(String encoding) throws IOException
        {
            inputStream = conn.getContentInputStream();
            return new InputStreamReader(inputStream, conn.getEncoding());
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

        @Override
        protected InputStream getRelativeResource(String name)
        {
            InputStream stream = null;
            NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
            NodeRef child = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, name);
            if (child != null)
            {
                ContentReader contentReader = contentService.getReader(child, ContentModel.PROP_CONTENT);
                if (contentReader.exists())
                {
                    stream = contentReader.getContentInputStream();
                }
            }
            return stream;
        }
    }
}
