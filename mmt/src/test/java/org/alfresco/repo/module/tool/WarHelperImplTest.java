/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.module.tool;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.VersionNumber;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

import static org.junit.Assert.*;

/**
 * Tests the war helper. 
 *
 * @author Gethin James
 */
public class WarHelperImplTest extends WarHelperImpl
{

    public WarHelperImplTest()
    {
        super(new LogOutput()
        {            
            @Override
            public void info(Object message)
            {
                System.out.println(message);
            }
        });
        
        TConfig config = TConfig.get();
        config.setArchiveDetector(new TArchiveDetector("war|amp", new ZipDriver(IOPoolLocator.SINGLETON)));
    }
    
    @Test
    public void testRegEx()
    {
    	String x = "1";
    	assertTrue(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "king";
    	assertFalse(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "2.5.a";
    	assertFalse(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "1.2.5";
    	assertTrue(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "123";
    	assertTrue(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "3.4.11";
    	assertTrue(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "4.1.1";
    	assertTrue(x.matches(REGEX_NUMBER_OR_DOT));
    	x = "4.2.b";
    	assertFalse(x.matches(REGEX_NUMBER_OR_DOT));
    	
    }
    @Test
    public void testCheckCompatibleVersion()
    {
        TFile theWar = getFile(".war", "module/test.war");   //Version 4.1.0

        ModuleDetails installingModuleDetails = new ModuleDetailsImpl("test_it",  new ModuleVersionNumber("9999"), "Test Mod", "Testing module");
        installingModuleDetails.setRepoVersionMin(new VersionNumber("10.1"));
        try
        {
            this.checkCompatibleVersion(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("must be installed on a war version equal to or greater than 10.1"));
        }

        installingModuleDetails.setRepoVersionMin(new VersionNumber("1.1"));
        this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception     
     
        installingModuleDetails.setRepoVersionMax(new VersionNumber("3.0"));
        try
        {
            this.checkCompatibleVersion(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("cannot be installed on a war version greater than 3.0"));
        }        

        installingModuleDetails.setRepoVersionMax(new VersionNumber("99"));
        this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception
        
        installingModuleDetails.setRepoVersionMin(new VersionNumber("4.1.0"));  //current war version
        installingModuleDetails.setRepoVersionMax(new VersionNumber("4.1.0"));  //current war version
        this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception  
        
        installingModuleDetails.setRepoVersionMin(new VersionNumber("3.4.0"));  //current war version
        installingModuleDetails.setRepoVersionMax(new VersionNumber("4.1.0"));  //current war version
        this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception  
        
        try
        {
            installingModuleDetails.setRepoVersionMin(new VersionNumber("3.4.0"));  //current war version
            installingModuleDetails.setRepoVersionMax(new VersionNumber("4.0.999"));  //current war version
            this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception
            fail("Should not pass as current version is 4.1.0 and the max value is 4.0.999"); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("cannot be installed on a war version greater than 4.0.999"));
        }
    }
    
    @Test
    public void testCheckCompatibleVersionUsingManifest() throws IOException
    {
        //Now check the compatible versions using the manifest
    	TFile theWar = getFile(".war", "module/share-3.4.11.war");
    	ModuleDetails installingModuleDetails = new ModuleDetailsImpl("test_it",  new ModuleVersionNumber("9999"), "Test Mod", "Testing module");
        installingModuleDetails.setRepoVersionMin(new VersionNumber("10.1"));
        try
        {
            this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("must be installed on a war version equal to or greater than 10.1"));
        }

        installingModuleDetails.setRepoVersionMin(new VersionNumber("1.1"));
        this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails); //does not throw exception     
     
        installingModuleDetails.setRepoVersionMax(new VersionNumber("3.0"));
        try
        {
            this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("cannot be installed on a war version greater than 3.0"));
        }        

        installingModuleDetails.setRepoVersionMax(new VersionNumber("99"));
        this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails); //does not throw exception
        
        installingModuleDetails.setRepoVersionMin(new VersionNumber("3.4.11"));  //current war version
        installingModuleDetails.setRepoVersionMax(new VersionNumber("3.4.11"));  //current war version
        this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails); //does not throw exception  
        
        installingModuleDetails.setRepoVersionMin(new VersionNumber("3.4.7"));  //current war version
        installingModuleDetails.setRepoVersionMax(new VersionNumber("3.4.11"));  //current war version
        this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails); //does not throw exception  
        
        try
        {
            installingModuleDetails.setRepoVersionMin(new VersionNumber("3.4.0"));  //current war version
            installingModuleDetails.setRepoVersionMax(new VersionNumber("3.4.10"));  //current war version
            this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails); //does not throw exception
            fail("Should not pass as current version is 3.4.11 and the max value is 3.4.10"); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("cannot be installed on a war version greater than 3.4.10"));
        }
        
    	theWar = getFile(".war", "module/share-4.2.a.war");
    	installingModuleDetails = new ModuleDetailsImpl("test_it",  new ModuleVersionNumber("9999"), "Test Mod", "Testing module");
        installingModuleDetails.setRepoVersionMin(new VersionNumber("101.1"));
        //this should fail BUT we are using a non-numeric version number so instead it passes without validation
        this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails);
        
        
    	theWar = getFile(".war", "module/alfresco-4.2.a.war");
        //this should fail BUT we are using a non-numeric version number so instead it passes without validation
    	this.checkCompatibleVersionUsingManifest(theWar, installingModuleDetails);
    	

	}

    @Test
    public void testCheckCompatibleEdition()
    {
        Properties props = dummyModuleProperties();
        ModuleDetails installingModuleDetails = new ModuleDetailsImpl(props);
        TFile theWar = getFile(".war", "module/test.war");   //Community Edition
        
        //Test for no edition specified
        this.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception 
        
        //Test for invalid edition
        props.setProperty(ModuleDetails.PROP_EDITIONS, "CommuniT");
        installingModuleDetails = new ModuleDetailsImpl(props);
        
        try
        {
            this.checkCompatibleEdition(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("can only be installed in one of the following editions[CommuniT]"));
        }
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("CoMMunity"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);
        this.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception 
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("enterprise,community,bob"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);
        this.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("enterprise,Community"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);      
        this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception 

    }
    
    @Test
    public void testCheckCompatibleEditionUsingManifest() throws IOException
    {
        Properties props = dummyModuleProperties();
        ModuleDetails installingModuleDetails = new ModuleDetailsImpl(props);
        TFile theWar = getFile(".war", "module/share-3.4.11.war"); //enterprise edition
        
        //Test for no edition specified
        this.checkCompatibleEditionUsingManifest(theWar, installingModuleDetails); //does not throw exception 
        
        //Test for invalid edition
        props.setProperty(ModuleDetails.PROP_EDITIONS, "CommuniT");
        installingModuleDetails = new ModuleDetailsImpl(props);
        try
        {
            this.checkCompatibleEditionUsingManifest(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("can only be installed in one of the following editions[CommuniT]"));
        }
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("Enterprise"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);      
        this.checkCompatibleEditionUsingManifest(theWar, installingModuleDetails); //does not throw exception 
        
        props.setProperty(ModuleDetails.PROP_EDITIONS, ("Community"));  //should ignore case
        installingModuleDetails = new ModuleDetailsImpl(props);    
        try
        {
            this.checkCompatibleEditionUsingManifest(theWar, installingModuleDetails);
            fail(); //should never get here
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("can only be installed in one of the following editions[Community]"));
        }
        
    	theWar = getFile(".war", "module/share-4.2.a.war");
        this.checkCompatibleEditionUsingManifest(theWar, installingModuleDetails);
        
		String propertiesLocation = getFile(".amp", "module/test_v5.amp") + "/module.properties";
		installingModuleDetails = ModuleDetailsHelper.createModuleDetailsFromPropertyLocation(propertiesLocation);
		
		try {
			this.checkCompatibleEdition(theWar, installingModuleDetails);
			fail(); //should never get here
		} catch (ModuleManagementToolException exception) {
			assertTrue(exception.getMessage().endsWith("can only be installed in one of the following editions[Enterprise]"));
		}
		
        theWar = getFile(".war", "module/share-3.4.11.war");
        this.checkCompatibleEdition(theWar, installingModuleDetails);//should succeed
        
		try {
			theWar = getFile(".war", "module/share-4.2.a.war");
			this.checkCompatibleEdition(theWar, installingModuleDetails);
			fail(); //should never get here
		} catch (ModuleManagementToolException exception) {
			assertTrue(exception.getMessage().endsWith("can only be installed in one of the following editions[Enterprise]"));
		}
        
    }


    @Test
    public void testfindManifest() throws Exception {
        //Now check the compatible versions using the manifest
        TFile theWar = getFile(".war", "module/share-3.4.11.war");
        Manifest manifest = this.findManifest(theWar);

        assertNotNull(manifest);
        assertEquals("Alfresco Share Enterprise", manifest.getMainAttributes().getValue(MANIFEST_IMPLEMENTATION_TITLE));
        assertEquals("3.4.11", manifest.getMainAttributes().getValue(MANIFEST_SPECIFICATION_VERSION));

        theWar = getFile(".war", "module/alfresco-4.2.a.war");
        manifest = this.findManifest(theWar);

        assertNotNull(manifest);
        assertEquals("Alfresco Repository Community", manifest.getMainAttributes().getValue(MANIFEST_IMPLEMENTATION_TITLE));
        assertEquals("4.2.a", manifest.getMainAttributes().getValue(MANIFEST_SPECIFICATION_VERSION));
    }

    @Test
    public void testListModules() throws Exception
    {
        TFile theWar =  getFile(".war", "module/test.war");

        List<ModuleDetails> details = this.listModules(theWar);
        assertNotNull(details);
        assertEquals(details.size(), 0);

        theWar =  getFile(".war", "module/share-4.2.a.war");
        details = this.listModules(theWar);
        assertNotNull(details);
        assertEquals(details.size(), 1);
        ModuleDetails aModule = details.get(0);
        assertEquals("alfresco-mm-share", aModule.getId());
        assertEquals("0.1.5.6", aModule.getModuleVersionNumber().toString());
        assertEquals(ModuleInstallState.INSTALLED, aModule.getInstallState());

    }

	private Properties dummyModuleProperties() {
		Properties props = new Properties();
        props.setProperty(ModuleDetails.PROP_ID, "TestComp");
        props.setProperty(ModuleDetails.PROP_VERSION, "9999");
        props.setProperty(ModuleDetails.PROP_TITLE, "Test for Compatiblity");
        props.setProperty(ModuleDetails.PROP_DESCRIPTION, "Test for Compatible Editions");
		return props;
	}
    
    @Test
    public void testNoVersionProperties()
    {
        TFile theWar = getFile(".war", "module/empty.war");  

        ModuleDetails installingModuleDetails = new ModuleDetailsImpl("test_it",  new ModuleVersionNumber("9999"), "Test Mod", "Testing module");
        installingModuleDetails.setRepoVersionMin(new VersionNumber("10.1"));
        this.checkCompatibleVersion(theWar, installingModuleDetails); //does not throw exception
        this.checkCompatibleEdition(theWar, installingModuleDetails); //does not throw exception 
        
    }
    
    /**
     * Tests to see if the war is a share war.
     */
    @Test
    public void testIsShareWar()
    {
    	TFile theWar = getFile(".war", "module/test.war");   //Version 4.1.0
    	assertFalse(this.isShareWar(theWar));

    	theWar = getFile(".war", "module/empty.war");  
    	assertFalse(this.isShareWar(theWar));
    	
    	theWar = getFile(".war", "module/alfresco-4.2.a.war");
    	assertFalse(this.isShareWar(theWar));
    	
    	theWar = getFile(".war", "module/share-4.2.a.war");
    	assertTrue(this.isShareWar(theWar));
    	
    	
    }
    
    private TFile getFile(String extension, String location) 
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
        return new TFile(file);
}
}
