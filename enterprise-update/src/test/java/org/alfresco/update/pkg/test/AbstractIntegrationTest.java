package org.alfresco.update.pkg.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;

/**
 * Abstract base class for tests.
 * 
 * @author Matt Ward
 */
public abstract class AbstractIntegrationTest
{
    protected File targetDir;
    
    @Before
    public void setUp() throws Exception
    {
        initTargetDir();
    }
    
    private void initTargetDir()
    {
        String targetDir = System.getProperty("alfresco.target.dir");
        if (targetDir == null)
        {
            targetDir = "./target";    // test needs to be run in target dir.
        }
        this.targetDir = new File(targetDir);
        assertTrue("target dir does not exist :" + targetDir, this.targetDir.exists());
    }
    
    
    protected boolean runningOnWindows()
    {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows"))
        {
           return true;
        }
        return false;
    }
}
