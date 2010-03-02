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
            result = helper.getContentStream(documentId, 0, 0);
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
