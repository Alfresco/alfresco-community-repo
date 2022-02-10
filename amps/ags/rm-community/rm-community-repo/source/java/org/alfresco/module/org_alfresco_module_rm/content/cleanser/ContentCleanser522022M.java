/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.content.cleanser;

import java.io.File;

import org.alfresco.service.cmr.repository.ContentIOException;

/**
 * DoD 5220-22M data cleansing implementation.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
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
