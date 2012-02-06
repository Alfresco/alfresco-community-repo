package org.alfresco.repo.module.tool;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.VersionNumber;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import de.schlichtherle.io.FileOutputStream;

/**
 * Tests the war helper. 
 *
 * @author Gethin James
 */
public class WarHelperImplTest extends WarHelperImpl
{

    private WarHelper warhelper = new WarHelperImpl();
    
//    @Before
//    public void setUp() throws Exception
//    {
//    }

    @Test
    public void testCheckCompatibleVersion()
    {
        File theWar = getFile(".war", "module/test.war");   //Version 4.0.1

        ModuleDetails installingModuleDetails = new ModuleDetailsImpl("test_it",  new VersionNumber("9999"), "Test Mod", "Testing module");
        installingModuleDetails.setRepoVersionMin(new VersionNumber("10.1"));
        try
        {
            this.warhelper.checkCompatibleVersion(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("must be installed on a repo version greater than 10.1"));
        }

        installingModuleDetails.setRepoVersionMin(new VersionNumber("1.1"));
        this.warhelper.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception     
     
        installingModuleDetails.setRepoVersionMax(new VersionNumber("3.0"));
        try
        {
            this.warhelper.checkCompatibleVersion(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("cannot be installed on a repo version greater than 3.0"));
        }        

        installingModuleDetails.setRepoVersionMax(new VersionNumber("99"));
        this.warhelper.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception
        
        installingModuleDetails.setRepoVersionMin(new VersionNumber("4.0.1"));  //current war version
        installingModuleDetails.setRepoVersionMax(new VersionNumber("4.0.1"));  //current war version
        this.warhelper.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception  
    }

    @Test
    public void testCheckCompatibleEdition()
    {
        Properties props = new Properties();
        props.setProperty(ModuleDetails.PROP_ID, "TestComp");
        props.setProperty(ModuleDetails.PROP_VERSION, "9999");
        props.setProperty(ModuleDetails.PROP_TITLE, "Test for Compatiblity");
        props.setProperty(ModuleDetails.PROP_DESCRIPTION, "Test for Compatible Editions");
        ModuleDetails installingModuleDetails = new ModuleDetailsImpl(props);
        File theWar = getFile(".war", "module/test.war");   //Community Edition
        
        //Test for no edition specified
        this.warhelper.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception 
        
        //Test for invalid edition
        props.setProperty(ModuleDetails.PROP_EDITIONS, "CommuniT");
        installingModuleDetails = new ModuleDetailsImpl(props);
        
        try
        {
            this.warhelper.checkCompatibleEdition(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("can only be installed in one of the following editions[CommuniT]"));
        }
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("CoMMunity"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);
        this.warhelper.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception 
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("enterprise,community,bob"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);
        this.warhelper.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("enterprise,Community"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);      
        this.warhelper.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception 
    }
    
    private File getFile(String extension, String location) 
    {
        File file = TempFileProvider.createTempFile("moduleManagementToolTest-", extension);        
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
        assertNotNull(is);
        OutputStream os;
        try
        {
            os = new FileOutputStream(file);
            FileCopyUtils.copy(is, os);
        }
        catch (IOException error)
        {
            error.printStackTrace();
        }        
        return file;
}
}
