/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.deploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.avm.AVMServiceTestBase;
import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentReportCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.Deleter;
import org.alfresco.util.NameMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * End to end test of deployment to a file system receiver (FSR).
 * @author britt
 * @author mrogers
 */
public class DeploymentServiceImplFSTest extends AVMServiceTestBase
{
    private File log = null;
    private File metadata = null;
    private File data = null;
    private File target = null;
    
    private String TEST_USER = "Giles";
    private String TEST_PASSWORD = "Watcher";
    private String TEST_TARGET = "sampleTarget";
    
    DeploymentService service = null;
    
    private static Log logger = LogFactory.getLog(DeploymentServiceImplFSTest.class);
    
	
    @Override
    protected void setUp() throws Exception
    {
    	super.setUp();
        log = new File("deplog");
        log.mkdir();
        metadata = new File("depmetadata");
        metadata.mkdir();
        data = new File("depdata");
        data.mkdir();
        target = new File("sampleTarget");
        target.mkdir();
        
    	/**
    	 * Start the FSR
    	 */
        @SuppressWarnings("unused")
        FileSystemXmlApplicationContext receiverContext =
            new FileSystemXmlApplicationContext("../deployment/config/application-context.xml");
        
        service = (DeploymentService)fContext.getBean("DeploymentService");    
    }
    
    protected void tearDown() throws Exception
    {
    	super.tearDown();
    	
        if(log != null)
        {
        	Deleter.Delete(log);
        }
        if(data != null)
        {
        	Deleter.Delete(data);
        }
        if(metadata != null)
        {
        	Deleter.Delete(metadata);
        }
        if(target != null)
        {
        	Deleter.Delete(target);
        }

        File dot = new File(".");
        String[] listing = dot.list();
        for (String name : listing)
        {
             if (name.startsWith("dep-record-"))
             {
                 File file = new File(name);
                 file.delete();
             }
        }
    }
    
    public void testBasic()
        throws Exception
    {

            NameMatcher matcher = (NameMatcher)fContext.getBean("globalPathExcluder");
            
            /**
             *  set up our test tree
             */
            fService.createDirectory("main:/", "a");

            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            
            fService.createFile("main:/a/b/c", "foo").close();
            String fooText="I am main:/a/b/c/foo";
            ContentWriter writer = fService.getContentWriter("main:/a/b/c/foo", true);
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo");
            
            fService.createFile("main:/a/b/c", "bar").close();
            writer = fService.getContentWriter("main:/a/b/c/bar", true);
            // Force a conversion
            writer.setEncoding("UTF-16");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/bar");
            
            String buffyText = "This is test data: Buffy the Vampire Slayer is an Emmy Award-winning and Golden Globe-nominated American cult television series that aired from March 10, 1997 until May 20, 2003. The series was created in 1997 by writer-director Joss Whedon under his production tag, Mutant Enemy Productions with later co-executive producers being Jane Espenson, David Fury, and Marti Noxon. The series narrative follows Buffy Summers (played by Sarah Michelle Gellar), the latest in a line of young women chosen by fate to battle against vampires, demons, and the forces of darkness as the Slayer. Like previous Slayers, Buffy is aided by a Watcher, who guides and trains her. Unlike her predecessors, Buffy surrounds herself with a circle of loyal friends who become known as the Scooby Gang.";
            fService.createFile("main:/a/b", "buffy").close();
            writer = fService.getContentWriter("main:/a/b/buffy", true);
            // Force a conversion
            writer.setEncoding("UTF-16");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            
            writer.putContent(buffyText);
            
            fService.createFile("main:/a/b", "fudge.bak").close();
            DeploymentReport report = new DeploymentReport();
            List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));
            
            /**
             * Do our first deployment - should deploy the basic tree defined above
             * fudge.bak should be excluded due to the matcher.
             */
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
        	Set<DeploymentEvent> firstDeployment = new HashSet<DeploymentEvent>();
        	firstDeployment.addAll(report.getEvents());
        	// validate the deployment report
        	assertTrue("first deployment no start", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, TEST_TARGET)));
        	assertTrue("first deployment no finish", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, TEST_TARGET)));
        	assertTrue("first deployment wrong size", firstDeployment.size() == 11);
        	assertTrue("Update missing: /a", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a")));
        	assertTrue("Update missing: /a/b", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b")));
        	assertTrue("Update missing: /a/b/c", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/c")));
        	assertTrue("Update missing: /d/e", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/d/e")));
        	assertTrue("Update missing: /a/b/c/foo", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/c/foo")));
        	assertTrue("Update missing: /a/b/c/bar", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/c/bar")));
        	assertTrue("Update missing: /a/b/buffy", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/buffy")));
        	assertFalse("Fudge has not been excluded", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/fudge.bak")));
        	
        	// Check that files exist on target
        	File target = new File("sampleTarget");
        	assertTrue("target dir does not extist", target.exists());
        	assertTrue("target dir is not a directory", target.isDirectory());
        	
        	{
        		File buffyFile = new File(target.getAbsolutePath()+ File.separator + "a" + File.separator+ "b" + File.separator + "buffy");
        		assertTrue("buffy file not created", buffyFile.exists());
        		assertTrue("buffy file is not a file", buffyFile.isFile());
        		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(buffyFile), "UTF-16"));
        		String text = reader.readLine();
        		assertTrue("UTF-16 buffy text is not correct", buffyText.equals(text));
        	}
        	
        	{
            	File fooFile = new File(target.getAbsolutePath()+ File.separator + "a" + File.separator+ "b" + File.separator + "c" + File.separator + "foo");
            	assertTrue("foo file not created", fooFile.exists());
            	assertTrue("foo file is not a file", fooFile.isFile());
            	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fooFile), "UTF-8"));
        		String text = reader.readLine();
        		assertTrue("UTF-8 foo text is not correct", fooText.equals(text));
        	}
     	

            for (DeploymentEvent event : report)
            {
                System.out.println(event);
            }
            
            /**
             *  Now do the same deployment again - should just get start and end events.
             */
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
            int count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(2, count);
            
            /**
             * now remove a single file in a deployment
             */
            fService.removeNode("main:/a/b/c", "bar");
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
           	Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
        	smallUpdate.addAll(report.getEvents());
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
            }
            assertEquals(3, smallUpdate.size());
        	assertTrue("Bar not deleted", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/a/b/c/bar")));
            
            /**
             *  Now create a new dir and file and remove a node in a single deployment 
             */
            fService.createFile("main:/d", "jonathan").close();
            fService.removeNode("main:/a/b");
            
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));

            
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(4, count);
            
            /**
             * Replace a single directory with a file
             */
            fService.removeNode("main:/d/e");
            fService.createFile("main:/d", "e").close();
            
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));

            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(3, count);
            
            /**
             * Create a few files
             */
            fService.removeNode("main:/d/e");
            fService.createDirectory("main:/d", "e");
            fService.createFile("main:/d/e", "Warren.txt").close();
            fService.createFile("main:/d/e", "It's a silly name.txt").close();
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));

            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(5, count);
            
             
            
    }
	
    /**
     * Test for ETWOTWO-507
     * 1. In a web project, create files called test01.html and test03.html.
	 * 3. Deploy using the FSR.
	 * 5. Add a new file called test02.html.
	 * 6. Delete the file called test03.html.
	 * 8. Deploy using the FSR. 
     */
    public void testEtwoTwo507() throws Exception
    {
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
    	fService.createDirectory("main:/", "a");
    	fService.createFile("main:/a", "test01.html").close();
    	fService.createFile("main:/a", "test03.html").close();
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
        for (DeploymentEvent event : report)
    	{
            System.out.println(event);
        }
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	fService.createFile("main:/a", "test02.html").close();
    	fService.removeNode("main:/a", "test03.html");

    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	Set<DeploymentEvent> firstDeployment = new HashSet<DeploymentEvent>();
    	firstDeployment.addAll(report.getEvents());
    	
    	assertTrue("Update missing: /a/test02.html", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/test02.html")));
    	assertTrue("delete missing: /a/test03.html", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/a/test03.html")));
        for (DeploymentEvent event : report)
        {
            System.out.println(event);
        }

    	
    }
    
    /**
     * Test for ETWOTWO-1236
     * 1. create a file in a Web Project called "CamelCase.txt"
	 * 2. submit the file to staging
	 * 3. deploy it to an FSR, ideally on a *nix OS (the issues are more severe on *nix than on Windows)
	 * 4. rename the file to "cAMELcASE.TXT"
     * 5. submit the change to staging
     * 6. deploy it to the same FSR
     */ 

    public void testCaseSensitivity() throws Exception
    {
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
        /**
         * Deploy CamelCase.txt
         */
    	fService.createFile("main:/", "CamelCase.txt").close();
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
        for (DeploymentEvent event : report)
    	{
            System.out.println(event);
        }
    	Set<DeploymentEvent> firstDeployment = new HashSet<DeploymentEvent>();
    	firstDeployment.addAll(report.getEvents());
    	assertTrue("Create missing: /CamelCase.txt", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/CamelCase.txt")));
        
        
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	
    	//fService.rename("main:/", "CamelCase.txt", "main:/", "cAMELcASE.TXT");
    	fService.removeNode("main:/", "CamelCase.txt");
    	fService.createFile("main:/", "cAMELcASE.TXT").close();
    	
    
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	Set<DeploymentEvent> secondDeployment = new HashSet<DeploymentEvent>();
    	secondDeployment.addAll(report.getEvents());
        for (DeploymentEvent event : report)
        {
            System.out.println(event);
        }																												

    	assertTrue("delete missing: /CamelCase.txt", secondDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/CamelCase.txt")));
    	assertTrue("Create missing: /cAMELcASE.TXT", secondDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/cAMELcASE.TXT")));
    	
    }



    
    /**
     * Wrong password
     * Negative test
     */
    public void testWrongPassword() 
    {
        logger.debug("Start testWrongPassword");

            try {
            	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, "Wrong!", TEST_TARGET, null, false, false, false, null);
            	fail("Wrong password should throw exception");
            } 
            catch (AVMException de)
            {
            	// pass
            	de.printStackTrace();
            }
    }
    
    /**
     *  Wrong target
     *  Negative test
     */
    public void testWrongTarget()
    {
        logger.debug("Start testWrongTarget");
            try {
            	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, "crapTarget", null, false, false, false, null);
            	fail("Wrong target should have thrown an exception");
            } 
            catch (AVMException de)
            {
            	// pass
            }     
    }
    
    /**
     *  Now do the same deployment again - without the matcher - should deploy fudge.bak 
     */
    public void testNoExclusionFilter() throws Exception
    {
        logger.debug("Start testNoExclusionFilter");
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	
    	fService.createDirectory("main:/", "a");
    	fService.createDirectory("main:/a", "b");
    	fService.createFile("main:/a/b", "fudge.bak").close();
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
    	smallUpdate.addAll(report.getEvents());

    	for (DeploymentEvent event : report)
    	{
    		System.out.println(event);
    	}
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/fudge.bak")));
    	assertEquals(5, smallUpdate.size());
    }
    
    /**
     *  Deploy a website, update it, then revert to the first version 
     */
    public void testRevertToPreviousVersion() throws Exception
    {
        logger.debug("Start testRevertToPreviousVersion");
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	
    	fService.createDirectory("main:/", "a");
    	fService.createDirectory("main:/a", "b");
    	fService.createFile("main:/a/b", "Zander").close();
    	fService.createFile("main:/a/b", "Cordelia").close();
    	fService.createFile("main:/a/b", "Buffy").close();
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	int version = report.getEvents().get(0).getSource().getFirst();
    	assertTrue("version is not set", version > 0);
    	
    	// Now do some updates
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	fService.createFile("main:/a/b", "Master").close();
        fService.createFile("main:/a/b", "Drusilla").close();
        fService.removeNode("main:/a/b", "Zander");
       	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	
        // now do the restore to previous version
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	service.deployDifferenceFS(version, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
    	smallUpdate.addAll(report.getEvents());   	
    	for (DeploymentEvent event : report)
    	{
    		System.out.println(event);
    	}
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/a/b/Zander")));
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/a/b/Drusilla")));
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/a/b/Master")));
    	assertEquals(5, smallUpdate.size());    	
    		



    }
    
	/**
	 *  Now load a large number of files.
	 *  Do a deployment - should load successfully
	 *  
	 *  Remove a node and update a file
	 *  Do a deployment - should only see start and end events and the two above. 
	 */
    public void testBulkLoad() throws Exception
    {   
        logger.debug("Start testBulkLoad");
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
    	BulkLoader loader = new BulkLoader();
    	loader.setAvmService(fService);
    	loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	Set<DeploymentEvent> bigUpdate = new HashSet<DeploymentEvent>();
    	bigUpdate.addAll(report.getEvents());
    	assertTrue("big update no start", bigUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, TEST_TARGET)));
    	assertTrue("big update no finish", bigUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, TEST_TARGET)));
    	assertTrue("big update too small", bigUpdate.size() > 100);
    	assertTrue("Update missing /avm/AVMServiceTest.java", bigUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, "/avm/AVMServiceTest.java")));
    
    	/**
    	 * Now do a smaller update and check that just a few files update
    	 * Start
    	 * Delete /avm/ibatis
    	 * Update /avm/AVMServiceTest.java
    	 * End
    	 */
    	fService.removeNode("main:/avm/ibatis");
    	fService.getFileOutputStream("main:/avm/AVMServiceTest.java").close();
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100,  TEST_USER, TEST_PASSWORD, TEST_TARGET, null, false, false, false, callbacks);
    	
    	Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
    	smallUpdate.addAll(report.getEvents());
    	for (DeploymentEvent event : report)
    	{
    		System.out.println(event);
    	}
    	
    	assertEquals(4, smallUpdate.size());
    	assertTrue("Start missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, TEST_TARGET)));
    	assertTrue("End missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/avm/ibatis")));
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.UPDATED, null, "/avm/AVMServiceTest.java")));
    	assertTrue("Delete Missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, TEST_TARGET)));	
    }
    
}
