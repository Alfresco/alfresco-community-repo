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
package org.alfresco.repo.cmis.ws;

import java.io.InputStream;

import org.alfresco.service.cmr.repository.FileTypeImageSize;

/**
 * Icon files retriever
 * 
 * @author Stas Sokolovsky
 */
public interface FileTypeIconRetriever
{
    /**
     * Get content of icon by file type.
     * 
     * @param filename file name
     * @param size icon size
     * @return content input stream
     */
    public InputStream getIconContent(String filename, FileTypeImageSize size);

    /**
     * Get mimetype of icon.
     * 
     * @param filename file name
     * @param size icon size
     * @return mimetype of icon
     */
    public String getIconMimetype(String filename, FileTypeImageSize size);

}
