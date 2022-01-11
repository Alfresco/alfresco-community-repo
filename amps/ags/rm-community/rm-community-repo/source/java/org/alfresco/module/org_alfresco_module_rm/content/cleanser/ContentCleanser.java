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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Content cleanser base implementation.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public abstract class ContentCleanser
{
    /**
     * Cleanse file
     * 
     * @param file  file to cleanse
     */
    public abstract void cleanse(File file);

    /**
     * Overwrite files bytes with provided overwrite operation
     * 
     * @param file                  file
     * @param overwriteOperation    overwrite operation
     */
    protected void overwrite(File file, OverwriteOperation overwriteOperation)
    {
        // get the number of bytes
        long bytes = file.length();
        try
        {
            // get an output stream
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
            {
                for (int i = 0; i < bytes; i++)
                {
                    // overwrite byte
                    overwriteOperation.operation(os);
                }
            }
        }
        catch (IOException ioException)
        {
            // re-throw
            throw new AlfrescoRuntimeException("Unable to overwrite file", ioException);
        }
    }

    /**
     * Overwrite operation
     */
    protected abstract class OverwriteOperation
    {
        public abstract void operation(OutputStream os) throws IOException;
    }

    /**
     * Overwrite with zeros operation
     */
    protected OverwriteOperation overwriteZeros = new OverwriteOperation()
    {
        public void operation(OutputStream os) throws IOException
        {
            os.write(0);
        }
    };

    /**
     * Overwrite with ones operation
     */
    protected OverwriteOperation overwriteOnes = new OverwriteOperation()
    {
        public void operation(OutputStream os) throws IOException
        {
            os.write(0xff);
        }
    };

    /**
     * Overwrite with random operation
     */
    protected OverwriteOperation overwriteRandom = new OverwriteOperation()
    {
        private Random random = new Random();

        public void operation(OutputStream os) throws IOException
        {
            byte[] randomByte = new byte[1];
            random.nextBytes(randomByte);
            os.write(randomByte[0]);
        }
    };
}
