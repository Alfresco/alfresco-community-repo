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
    protected String version;
    
    @Before
    public void setUp() throws Exception
    {
        initTargetDir();
        initVersion();
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
    
    private void initVersion()
    {
        // This is set by maven, see pom.xml
        version = System.getProperty("version");
        assertNotNull("'version' system property not set. If using an IDE, then add an appropriate VM argument to "
                    + "your run configuration, e.g. -Dversion=5.0.3-SNAPSHOT", version);
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
