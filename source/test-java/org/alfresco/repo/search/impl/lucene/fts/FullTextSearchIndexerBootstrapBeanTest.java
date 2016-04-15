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
package org.alfresco.repo.search.impl.lucene.fts;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.surf.util.I18NUtil;
import org.junit.BeforeClass;
import static org.hamcrest.CoreMatchers.is;

public class FullTextSearchIndexerBootstrapBeanTest
{
    private FullTextSearchIndexerBootstrapBean bean;

    private final static Appender appender = mock(Appender.class);

    private final static Logger logger = Logger.getRootLogger();

    @BeforeClass
    public static void setupBeforeClass()
    {
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
        I18NUtil.registerResourceBundle("alfresco.version");
        logger.addAppender(appender);
    }

    @Test
    public void test()
    {
        // when
        bean = new FullTextSearchIndexerBootstrapBean();
        try
        {
            bean.onBootstrap(null);
        }
        catch (NullPointerException n)
        {
            // expected for this test, since there is no nodeService
        }

        // then
        ArgumentCaptor<LoggingEvent> argument = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(appender).doAppend(argument.capture());
        assertThat(argument.getValue().getLevel(), is(Level.ERROR));

        String majorVersion = I18NUtil.getMessage("version.major");
        String minorVersion = I18NUtil.getMessage("version.minor");
        String pattern = "docs.alfresco.com/{0}";
        pattern = MessageFormat.format(pattern, majorVersion + "." + minorVersion);
        assertTrue(String.valueOf(argument.getValue().getMessage()).contains(pattern));
    }

    @AfterClass
    public static void cleanupAfterClass()
    {
        logger.removeAppender(appender);
    }
}
