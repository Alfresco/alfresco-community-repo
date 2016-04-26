package org.alfresco.repo.web.scripts;

import javax.servlet.ServletContext;

import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;


/**
 * Web Scripts Image Resolver
 * 
 * @author davidc
 */
public class RepositoryImageResolver
    implements ServletContextAware, InitializingBean
{
    private ServletContext servletContext;
    private TemplateImageResolver imageResolver;
    
    
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context)
    {
        this.servletContext = context;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @SuppressWarnings("serial")
    public void afterPropertiesSet()
        throws Exception
    {
        this.imageResolver = new TemplateImageResolver()
        {
            public String resolveImagePathForName(String filename, FileTypeImageSize size)
            {
                return FileTypeImageUtils.getFileTypeImage(servletContext, filename, size);
            }  
        };        
    }

    /**
     * @return  image resolver
     */
    public TemplateImageResolver getImageResolver()
    {
        return this.imageResolver;
    }

}
