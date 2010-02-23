/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.springframework.web.context.ServletContextAware;

/**
 * Icon retriever implementation
 * 
 * @author Stas Sokolovsky
 */
public class FileTypeIconRetrieverImpl implements FileTypeIconRetriever, ServletContextAware
{
    private ServletContext servletContext;
    private String mimetypeForSmall;
    private String mimetypeForMedium;

    /**
     * @see org.alfresco.repo.cmis.ws.FileTypeIconRetriever#getIconContent(java.lang.String, org.alfresco.service.cmr.repository.FileTypeImageSize)
     */
    public InputStream getIconContent(String filename, FileTypeImageSize size)
    {
        // TODO: Should we cache icon names or icon content?
        String iconPath = FileTypeImageUtils.getFileTypeImage(servletContext, filename, size);
        return iconPath != null ? servletContext.getResourceAsStream(iconPath) : null;
    }

    /**
     * @see org.alfresco.repo.cmis.ws.FileTypeIconRetriever#getIconMimetype(java.lang.String, org.alfresco.service.cmr.repository.FileTypeImageSize)
     */
    public String getIconMimetype(String filename, FileTypeImageSize size)
    {
        return size.equals(FileTypeImageSize.Small) ? mimetypeForSmall : mimetypeForMedium;
    }

    /**
     * Set the mimetype for small icons.
     * 
     * @param mimetypeForSmall mimetype
     */
    public void setMimetypeForSmall(String mimetypeForSmall)
    {
        this.mimetypeForSmall = mimetypeForSmall;
    }

    /**
     * Set the mimetype for medium icons.
     * 
     * @param mimetypeForMedium mimetype
     */
    public void setMimetypeForMedium(String mimetypeForMedium)
    {
        this.mimetypeForMedium = mimetypeForMedium;
    }

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context)
    {
        this.servletContext = context;
    }
}

