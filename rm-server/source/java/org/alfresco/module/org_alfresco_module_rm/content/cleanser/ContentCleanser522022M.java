/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.content.cleanser;

import java.io.File;

import org.alfresco.service.cmr.repository.ContentIOException;

/**
 * DoD 5220-22M data cleansing implementation.
 * 
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class ContentCleanser522022M extends ContentCleanser
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser#cleanse(java.io.File)
     */
    @Override
    public void cleanse(File file)
    {
        // Double check
        if (!file.exists() || !file.canWrite())
        {
            throw new ContentIOException("Unable to write to file: " + file);
        }
        
        // Overwite file
        overwrite(file, overwriteOnes);
        overwrite(file, overwriteZeros);
        overwrite(file, overwriteRandom);
    }
}
