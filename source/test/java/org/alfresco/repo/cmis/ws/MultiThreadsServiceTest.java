/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.alfresco.cmis.CMISDictionaryModel;

public class MultiThreadsServiceTest extends AbstractServiceTest
{
    private String lastName;
    private String lastContent;
    private Random generator = new Random();
    private boolean isRunning;
    private Thread thread;

    public MultiThreadsServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    public MultiThreadsServiceTest()
    {
        super();
    }

    @Override
    protected Object getServicePort()
    {
        return helper.navigationServicePort;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        createInitialContent();

    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        deleteInitialContent();
    }

    public void testUpdateDocumentMultiThreaded() throws Exception
    {
        ThreadGroup threadGroup = new ThreadGroup("testUpdateDocumentMultiThreaded");
        List<AbstractBaseRunner> threads = new LinkedList<AbstractBaseRunner>();
        for (int i = 0; i < 5; i++)
        {
            AbstractBaseRunner updater = new DocumentUpdater();

            Thread updateDocumentTread = new Thread(threadGroup, updater, "updateDocument " + i);

            threads.add(updater);

            updateDocumentTread.start();
        }

        isRunning = true;
        while (isRunning)
        {
            isRunning = threadGroup.activeCount() != 0;
        }
        if (isRunning == false)
        {
            assertTrue("All threads done their work normally", anyFailed(threads));

            CmisObjectType propertiesObject = helper.getObjectProperties(documentId);
            assertObjectPropertiesNotNull(propertiesObject);
            assertEquals(lastName, getStringProperty(propertiesObject.getProperties(), CMISDictionaryModel.PROP_NAME));
        }
    }

    public void testSetTextContentStreamMultiThreaded() throws Exception
    {
        ThreadGroup threadGroup = new ThreadGroup("testSetContentStreamMultiThreaded");
        List<AbstractBaseRunner> threads = new LinkedList<AbstractBaseRunner>();
        for (int i = 0; i < 5; i++)
        {
            // thread = new Thread(threadGroup, updateDocument, "updateDocument again " + i);
            // thread.start();

            AbstractBaseRunner runner = new TextContentStreamSetter();

            thread = new Thread(threadGroup, runner, "setTextContentStream" + i);

            threads.add(runner);

            thread.start();
        }

        isRunning = true;
        while (isRunning)
        {
            isRunning = threadGroup.activeCount() != 0;
        }
        if (isRunning == false)
        {
            CmisContentStreamType result;
            result = helper.getContentStream(documentId);
            if (result.getLength().intValue() == 0)
            {
                fail("Content Stream is empty");
            }

            assertTrue("All threads done their work normally", anyFailed(threads));

            assertEquals(lastContent, result.getStream().getContent());
        }
    }

    private boolean anyFailed(List<AbstractBaseRunner> threads)
    {

        for (AbstractBaseRunner runner : threads)
        {
            if (runner.isExecutionFailed())
            {
                return true;
            }
        }

        return false;
    }

    private abstract class AbstractBaseRunner implements Runnable
    {
        protected boolean executionFailed;

        public boolean isExecutionFailed()
        {

            return executionFailed;
        }
    }

    private class DocumentUpdater extends AbstractBaseRunner
    {
        public void run()
        {
            try
            {
                String newName = "New Name" + System.currentTimeMillis() + generator.nextDouble();
                helper.updateProperty(documentId, CMISDictionaryModel.PROP_NAME, newName);
                lastName = newName;
            }
            catch (Exception e)
            {
                // this fail() does not fail the test, just print exception message to the console
                executionFailed = true;
            }
        }
    }

    private class TextContentStreamSetter extends AbstractBaseRunner
    {
        public void run()
        {
            try
            {
                String newContent = "New text content for testing." + generator.nextDouble();
                helper.setTextContentStream(documentId, newContent);
                lastContent = newContent;
            }
            catch (Exception e)
            {
                // this fail() does not fail the test, just print exception message to the console
                executionFailed = true;
            }
        }
    }
}
