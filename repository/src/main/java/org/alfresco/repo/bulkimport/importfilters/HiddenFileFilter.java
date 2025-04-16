/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.bulkimport.importfilters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;

/**
 * This class is an <code>ImportFilter</code> that filters out hidden files.
 * 
 * The exact definition of "hidden" is OS dependent - see http://download.oracle.com/javase/6/docs/api/java/io/File.html#isHidden() for details.
 *
 * @since 4.0
 *
 */
public class HiddenFileFilter implements ImportFilter
{
    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        boolean result = false;

        if (importableItem.getHeadRevision().contentFileExists())
        {
            Path file = importableItem.getHeadRevision().getContentFile();
            try
            {
                result = Files.isHidden(file);
            }
            catch (IOException e)
            {}
        }

        return (result);
    }

}
