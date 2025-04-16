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
package org.alfresco.repo.bulkimport.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * 
 * @since 4.0
 *
 *        TODO move to core project
 */
public class FileUtils
{
    public static String getFileName(final File file)
    {
        String result = null;

        if (file != null)
        {
            try
            {
                result = file.getCanonicalPath();
            }
            catch (final IOException ioe)
            {
                result = file.toString();
            }
        }

        return (result);
    }

    public static String getFileName(final Path path)
    {
        String result = null;

        if (path != null)
        {
            try
            {
                result = path.toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
            }
            catch (final IOException ioe)
            {
                result = path.toString();
            }
        }

        return result;
    }
}
