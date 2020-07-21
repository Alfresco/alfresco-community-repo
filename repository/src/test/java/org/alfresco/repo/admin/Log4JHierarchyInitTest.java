/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import junit.framework.TestCase;

import org.alfresco.repo.model.filefolder.FileFolderPerformanceTester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Derek Hulley
 * @see Log4JHierarchyInit
 * @since 2.2.3
 */
public class Log4JHierarchyInitTest extends TestCase
{
    private PrintStream sysErr;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private static ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "classpath:log4j/log4j-test-context.xml" });

    public void setUp() throws Exception
    {
    }

    public void testSetUp() throws Throwable
    {
        // Check that the bean is present
        ctx.getBean("log4JHierarchyInit");
        // Make sure that the default log4j.properties is being picked up
        Log log = LogFactory.getLog("log4j.logger.org.alfresco");
        assertFalse("Expect log level ERROR for 'org.alfresco'.", log.isWarnEnabled());
    }

    public void testAddingLog4jProperties() throws Throwable
    {
        Log log = LogFactory.getLog(this.getClass());
        // We expect DEBUG to be on
        assertTrue("DEBUG was not enabled for logger " + this.getClass(), log.isDebugEnabled());
    }

    public void setUpStreams() throws UnsupportedEncodingException
    {
        sysErr = System.err;
        System.setErr(new PrintStream(errContent, false, "UTF-8"));
    }

    public void revertStreams()
    {
        System.setErr(sysErr);
    }

    public void testLog4jAppenderClosedError() throws Throwable
    {
        setUpStreams();
        Log log = LogFactory.getLog(this.getClass());

        Log4JHierarchyInit log4JHierarchyInit = (Log4JHierarchyInit) ctx.getBean("log4JHierarchyInit");

        Log log2 = LogFactory.getLog(FileFolderPerformanceTester.class);
        log4JHierarchyInit.init();

        log2.info("test");

        // We expect DEBUG to be on
        assertTrue("DEBUG was not enabled for logger " + this.getClass(), log.isDebugEnabled());
        assertFalse(errContent.toString().contains("Attempted to append to closed appender named"));

        revertStreams();
    }
}
