package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.File;

import org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser522022M;

/**
 * Test Content Cleanser
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class TestContentCleanser extends ContentCleanser522022M
{
    private boolean hasCleansed = false;
    
    public void reset()
    {
        hasCleansed = false;
    }
    
    public boolean hasCleansed()
    {
        return hasCleansed;
    }    
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser#cleanse(java.io.File)
     */
    @Override
    public void cleanse(File file)
    {
        hasCleansed = false;
        super.cleanse(file);
        hasCleansed = true;
    }

}
