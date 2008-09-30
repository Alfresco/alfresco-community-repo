/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.avm.AVMServiceTestBase;
import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentReportCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.util.Deleter;
import org.alfresco.util.NameMatcher;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * End to end test of filesystem deployment.
 * @author britt
 */
public class FSDeploymentTest extends AVMServiceTestBase
{
    private File log = null;
    private File metadata = null;
    private File data = null;
    private File target = null;
    
    DeploymentService service = null;
    
	
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
        target = new File("target");
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
            setupBasicTree();

            /*
            BasicTree has the following format
            "main:/", "a"
            "main:/a", "b"
            "main:/a/b", "c"
            "main:/", "d"
            "main:/d", "e"
            "main:/d/e", "f"           
            "main:/a/b/c", "foo").close()
            "main:/a/b/c", "bar").close()
            "main:/a/b", "fudge.bak").close()
            */
            
            fService.createFile("main:/a/b", "fudge.bak").close();
            DeploymentReport report = new DeploymentReport();
            List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));
            
            /**
             * Do our first deployment - should deploy the basic tree defined above
             * fudge.bak should be excluded due to the matcher.
             */
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", matcher, false, false, false, callbacks);
        	Set<DeploymentEvent> firstDeployment = new HashSet<DeploymentEvent>();
        	firstDeployment.addAll(report.getEvents());
        	assertTrue("first deployment no start", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, "sampleTarget")));
        	assertTrue("first deployment no finish", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, "sampleTarget")));
        	assertTrue("first deployment wrong size", firstDeployment.size() == 10);
        	assertTrue("Update missing: /a", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/a")));
        	assertTrue("Update missing: /a/b", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/a/b")));
        	assertTrue("Update missing: /a/b/c", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/a/b/c")));
        	assertTrue("Update missing: /d/e", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/d/e")));
        	assertTrue("Update missing: /a/b/c/foo", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/a/b/c/foo")));
        	assertTrue("Update missing: /a/b/c/bar", firstDeployment.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/a/b/c/bar")));

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
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", matcher, false, false, false, callbacks);
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
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", matcher, false, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(3, count);
            
            /**
             *  Now create a new dir and file and remove a node in a single deployment 
             */
            fService.createFile("main:/d", "jonathan").close();
            fService.removeNode("main:/a/b");
            
            report = new DeploymentReport();
            callbacks = new ArrayList<DeploymentCallback>();
            callbacks.add(new DeploymentReportCallback(report));

            
            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", matcher, false, false, false, callbacks);
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

            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", matcher, false, false, false, callbacks);
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

            service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", matcher, false, false, false, callbacks);
            count = 0;
            for (DeploymentEvent event : report)
            {
                System.out.println(event);
                count++;
            }
            assertEquals(5, count);
            
            /**
             *  Negative tests
             *	 Wrong password
             */
            try {
            	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Wrong!", "sampleTarget", matcher, false, false, false, callbacks);
            	fail("Wrong password should throw exception");
            } 
            catch (AVMException de)
            {
            	// pass
            	de.printStackTrace();
            }
            
            /**
             *  Negative tests
             *	 Wrong target
             */
            try {
            	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "crapTarget", matcher, false, false, false, callbacks);
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
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	
    	fService.createDirectory("main:/", "a");
    	fService.createDirectory("main:/a", "b");
    	fService.createFile("main:/a/b", "fudge.bak").close();
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", null, false, false, false, callbacks);
    	Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
    	smallUpdate.addAll(report.getEvents());

    	
    	for (DeploymentEvent event : report)
    	{
    		System.out.println(event);
    	}
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.COPIED, null, "/a/b/fudge.bak")));
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
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
    	BulkLoader loader = new BulkLoader();
    	loader.setAvmService(fService);
    	loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100, "Giles", "Watcher", "sampleTarget", null, false, false, false, callbacks);
    	Set<DeploymentEvent> bigUpdate = new HashSet<DeploymentEvent>();
    	bigUpdate.addAll(report.getEvents());
    	assertTrue("big update no start", bigUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, "sampleTarget")));
    	assertTrue("big update no finish", bigUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, "sampleTarget")));
    	assertTrue("big update too small", bigUpdate.size() > 100);
    
    	/**
    	 * Now do a smaller update and check that just a few files update
    	 */
    	fService.removeNode("main:/avm/hibernate");
    	fService.getFileOutputStream("main:/avm/AVMServiceTest.java").close();
    	report = new DeploymentReport();
    	callbacks = new ArrayList<DeploymentCallback>();
    	callbacks.add(new DeploymentReportCallback(report));
    	service.deployDifferenceFS(-1, "main:/", "default", "localhost", 44100,  "Giles", "Watcher", "sampleTarget", null, false, false, false, callbacks);
    	
    	Set<DeploymentEvent> smallUpdate = new HashSet<DeploymentEvent>();
    	smallUpdate.addAll(report.getEvents());
    	for (DeploymentEvent event : report)
    	{
    		System.out.println(event);
    	}
    	assertEquals(4, smallUpdate.size());
    	
    	assertTrue("Start missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.START, null, "sampleTarget")));
    	assertTrue("End missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.DELETED, null, "/avm/hibernate")));
    	assertTrue("Update missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.UPDATED, null, "/avm/AVMServiceTest.java")));
    	assertTrue("Delete Missing", smallUpdate.contains(new DeploymentEvent(DeploymentEvent.Type.END, null, "sampleTarget")));	
    }
}
