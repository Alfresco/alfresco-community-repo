 
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
