/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import junit.framework.TestCase;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Now only contains methods to load quick files
 * 
 * @author Derek Hulley
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public abstract class AbstractContentTransformerTest extends TestCase
{
    private static Log logger = LogFactory.getLog(AbstractContentTransformerTest.class);
    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     * 
     * @param quickname file required, eg <b>quick.txt</b>
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadNamedQuickTestFile(String quickname) throws IOException
    {
        String quickNameAndPath = "quick/" + quickname;
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource(quickNameAndPath);
        if (url == null)
        {
            return null;
        }
        if (ResourceUtils.isJarURL(url))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Using a temp file for quick resource that's in a jar." + quickNameAndPath);
            }
            try
            {
                InputStream is = AbstractContentTransformerTest.class.getClassLoader().getResourceAsStream(quickNameAndPath);
                File tempFile = TempFileProvider.createTempFile(is, quickname, ".tmp");
                return tempFile;
            }
            catch (Exception error)
            {
                logger.error("Failed to load a quick file from a jar. "+error);
                return null;
            }
        }
        return ResourceUtils.getFile(url);
    }
    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     * 
     * @param extension file extension required, eg <b>txt</b> for the file quick.txt
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadQuickTestFile(String extension) throws IOException
    {
       return loadNamedQuickTestFile("quick."+extension);
    }
}
