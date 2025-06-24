/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

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
     * 
     * @see org.springframework.web.context.ServletContextAware#setServletContext(jakarta.servlet.ServletContext) */
    public void setServletContext(ServletContext context)
    {
        this.servletContext = context;
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet() */
    @SuppressWarnings("serial")
    public void afterPropertiesSet()
            throws Exception
    {
        this.imageResolver = new TemplateImageResolver() {
            public String resolveImagePathForName(String filename, FileTypeImageSize size)
            {
                return FileTypeImageUtils.getFileTypeImage(servletContext, filename, size);
            }
        };
    }

    /**
     * @return image resolver
     */
    public TemplateImageResolver getImageResolver()
    {
        return this.imageResolver;
    }

}
