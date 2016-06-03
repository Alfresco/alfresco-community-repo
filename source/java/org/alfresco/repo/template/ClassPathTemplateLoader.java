package org.alfresco.repo.template;

import java.net.URL;

import freemarker.cache.URLTemplateLoader;

/**
 * Custom FreeMarker template loader to locate templates stored on the ClassPath.
 * 
 * @author Kevin Roast
 */
public class ClassPathTemplateLoader extends URLTemplateLoader
{
    /**
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     */
    protected URL getURL(String name)
    {
        return this.getClass().getClassLoader().getResource(name);
    }
}
