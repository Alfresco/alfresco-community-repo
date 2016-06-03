package org.alfresco.repo.bulkimport.impl;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @since 4.0
 *
 * TODO move to core project
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
        
        return(result);
    }
}
