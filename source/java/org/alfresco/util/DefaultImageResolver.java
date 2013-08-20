/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.util;

import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Default implementation of {@link TemplateImageResolver} interface.
 */
public class DefaultImageResolver implements TemplateImageResolver
{
    private static final long serialVersionUID = 7531417785209341858L;

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.repository.TemplateImageResolver#resolveImagePathForName(java.lang.String, org.alfresco.service.cmr.repository.FileTypeImageSize)
     */
    @Override
    public String resolveImagePathForName(String filename, FileTypeImageSize size)
    {
        return null;
    }
}
