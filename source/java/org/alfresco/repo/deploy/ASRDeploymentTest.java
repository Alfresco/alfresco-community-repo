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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.avm.AVMServiceTestBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentReportCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.NameMatcher;

/**
 * End to end test of deployment to an alfresco system receiver (ASR).
 * @author britt
 * @author mrogers
 */
public class ASRDeploymentTest extends AVMServiceTestBase
{
   
    DeploymentService service = null;
    
	
    @Override
    protected void setUp() throws Exception
    {
    	super.setUp();
    	
        service = (DeploymentService)fContext.getBean("DeploymentService");   
        
    }
    
    protected void tearDown() throws Exception
    {
    	super.tearDown();
    	
    }
    
    public void testBasic()
        throws Exception
    {

            NameMatcher matcher = (NameMatcher)fContext.getBean("globalPathExcluder");
            
            String destStore = "ASRDeploymentTest"; 
            try 
            {
            	fService.purgeStore(destStore);
            }
            catch (AVMNotFoundException e)
            {
            	// nothing to do - store did not exist
            }
            /**
             *  set up our test tree
             */
            fService.createDirectory("main:/", "a");
            
            String rootText = "Angel is an American television series, a spin-off of the television series Buffy the Vampire Slayer. The series was created by Buffy's creator, Joss Whedon, in collaboration with David Greenwalt, and first aired on October 5, 1999. Like Buffy, it was produced by Whedon's production company, Mutant Enemy.";
            fService.createFile("main:/", "rootFile").close();
            ContentWriter writer = fService.getContentWriter("main:/rootFile", true);
            
            // Force a conversion
            writer.setEncoding("UTF-16");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);         
            writer.putContent(rootText);
            
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            
            fService.createFile("main:/a/b/c", "foo").close();
            String fooText="I am main:/a/b/c/foo";
            writer = fService.getContentWriter("main:/a/b/c/foo", true);
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
            String destRef = destStore + ":/www/avm_webapps";
            service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "admin", destRef, matcher, true, false, false, callbacks);
        	
            
            Set<DeploymentEvent> firstDeployment = new HashSet<DeploymentEvent>();
        	firstDeployment.addAll(report.getEvents());
        	// validate the deployment report
        	assertTrue("first deployment no start", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, destRef)));
        	assertTrue("first deployment no finish", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, destRef)));
        	assertTrue("first deployment wrong size", firstDeployment.size() == 12);
        	assertTrue("Update missing: /a", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/a")));
        	assertTrue("Update missing: /rootFile", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/rootFile")));
        	assertTrue("Update missing: /a/b", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/a/b")));
        	assertTrue("Update missing: /a/b/c", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef  + "/a/b/c")));
        	assertTrue("Update missing: /d/e", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/d/e")));
        	assertTrue("Update missing: /a/b/c/foo", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/a/b/c/foo")));
        	assertTrue("Update missing: /a/b/c/bar", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/a/b/c/bar")));        	
        	assertTrue("Update missing: /a/b/buffy", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/a/b/buffy")));
        	assertFalse("Fudge has not been excluded", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.CREATED, null, destRef + "/a/b/fudge.bak")));
        	
        	// Check that files exist in the destination AVM Store	
        	{
        		fService.getNodeProperties(-1, destRef + "/a");
        		
        		Map<QName, PropertyValue> props = fService.getNodeProperties(-1, destRef + "/a/b/buffy");
        	    ContentReader reader = fService.getContentReader(-1, destRef + "/a/b/buffy");
        	    assertNotNull("content reader is null", reader);
         		assertTrue("UTF-16 buffy text is not correct", reader.getContentString().equals(buffyText));
        	}
          
            /**
              *  Now do the same deployment again - should just get start and end events.
              */
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));
            service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "admin", destRef, matcher, true, false, false, callbacks);
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
            service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "admin", destRef, matcher, true, false, false, callbacks);           	
            Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
        	smallUpdate.addAll(report.getEvents());
            for (DeploymentEvent event : report)
            {
               System.out.println(event);
            }
//            assertEquals(3, smallUpdate.size());
        	assertTrue("Bar not deleted", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, destRef + "/a/b/c/bar")));
        	
        	// Check that files exist in the destination AVM Store	
        	{    		
        	    fService.getNodeProperties(-1, destRef + "/a/b/buffy");
        	    ContentReader reader = fService.getContentReader(-1, destRef + "/a/b/buffy");
        	    assertNotNull("content reader is null", reader);
         		assertTrue("UTF-16 buffy text is not correct", reader.getContentString().equals(buffyText));
        	}
            
            /**
             *  Now create a new dir and file and remove a node in a single deployment 
             */
            fService.createFile("main:/d", "jonathan").close();
            fService.removeNode("main:/a/b");
            
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));
            
            service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "admin", destRef, matcher, true, false, false, callbacks);
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
            
            service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "admin", destRef, matcher, true, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(3, count);
            
        	// Check that files exist in the destination AVM Store	
        	{

        	    AVMNodeDescriptor desc = fService.lookup(-1, destRef + "/d/e");
        	    assertTrue("e is not a file", desc.isFile());

        	}

                        
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
            
            service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "admin", destRef, matcher, true, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(5, count);
            
            try 
            {
            	fService.purgeStore(destStore);
            }
            catch (AVMNotFoundException e)
            {
            	// nothing to do - store did not exist
            }
            
    }
	   
    /**
     * Wrong password
     * Negative test
     */
    public void testWrongPassword() 
    {
    	  NameMatcher matcher = (NameMatcher)fContext.getBean("globalPathExcluder");
    	  String destStore = "Junk";
          String destRef = destStore + ":/www/avm_webapps";
        

            try {
            	   service.deployDifference(-1, "main:/", "localhost", 50500, "admin", "wronky", destRef, matcher, true, false, false, null);
            	
            	fail("Wrong password should throw exception");
            } 
            catch (AVMException de)
            {
            	// pass
            	de.printStackTrace();
            }
    }
        
}
