package org.alfresco.web.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;


public class ClassPathAPIStore implements APIStore, InitializingBean
{
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private String classPath;
    private String classPathDir;
    
    
    public void setClassPath(String classPath)
    {
        this.classPath = classPath;
    }

    public void afterPropertiesSet()
        throws Exception
    {
        ClassPathResource resource = new ClassPathResource(classPath);
        classPathDir = resource.getURL().toExternalForm();
    }
    
    public String getBasePath()
    {
        return "classpath:" + classPath;
    }

    
    public String[] getDescriptionDocumentPaths()
    {
        String[] paths = null;
               
        try
        {
            Resource[] resources = resolver.getResources("classpath*:" + classPath + "/**/*_desc.xml");
            paths = new String[resources.length];
            int i = 0;
            for (Resource resource : resources)
            {
                paths[i++] = resource.getURL().toExternalForm().substring(classPathDir.length());
            }
        }
        catch(IOException e)
        {
            // Note: Ignore: no service description documents found
            paths = new String[0];
        }
        
        return paths;
    }

    
    public InputStream getDescriptionDocument(String documentPath)      
        throws IOException
    {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(classPathDir + "/" + documentPath);
        return resource.getInputStream();
    }

    
    public static void main(String[] args)
        throws Exception
    {
        ClassPathAPIStore store = new ClassPathAPIStore();
        store.setClassPath("alfresco/templates/api");
        store.afterPropertiesSet();
        String[] paths = store.getDescriptionDocumentPaths();
        InputStream is = store.getDescriptionDocument(paths[0]);
        is.close();
        TemplateLoader loader = store.getTemplateLoader();
        Object obj = loader.findTemplateSource("KeywordSearch_view_atom.ftl");
    }

    public TemplateLoader getTemplateLoader()
    {
        FileTemplateLoader loader = null;
        try
        {
            File classPathFile = new File(new URI(classPathDir));
            loader = new FileTemplateLoader(classPathFile);
        }
        catch (URISyntaxException e)
        {
            // Note: Can't establish loader, so return null
        }
        catch (IOException e)
        {
            // Note: Can't establish loader, so return null
        }
        return loader;
    }

    public ScriptLoader getScriptLoader()
    {
        // TODO Auto-generated method stub
        return null;
    }

    
}
