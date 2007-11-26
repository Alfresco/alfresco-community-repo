package org.alfresco.repo.web.scripts;

import javax.servlet.ServletContext;

import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.web.ui.common.Utils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;


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

    /*(non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
        throws Exception
    {
        this.imageResolver = new TemplateImageResolver()
        {
            public String resolveImagePathForName(String filename, FileTypeImageSize size)
            {
                return Utils.getFileTypeImage(servletContext, filename, size);
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
