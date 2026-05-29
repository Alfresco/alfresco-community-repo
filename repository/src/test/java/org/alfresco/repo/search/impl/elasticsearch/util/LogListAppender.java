/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;

/**
 * This appender is only for test purpose and can be use to verify if the system logs correctly. In order to use it add as appender in the test setup as below:
 *
 * ListAppender listAppender = new ListAppender(Level.INFO); Logger.getLogger(ClassUnderTest.class).addAppender(listAppender);
 *
 * Then you can do assertions on log messages using the getLogMessages() method:
 *
 * assertEquals(1, listAppender.getLogMessages().size());
 *
 */
@Plugin(name = "LogListAppender", category = "Core")
@SuppressWarnings({"PMD.NonThreadSafeSingleton", "PMD.UseDiamondOperator"})
public class LogListAppender extends AbstractAppender
{

    private final List<LogEvent> logMessages = new ArrayList<>();
    private Level expectedLevel;

    private static LogListAppender logListAppenderInstance;

    /**
     * @param expectedLevel
     *            only messages with the specified level will be collected
     */
    private LogListAppender(Level expectedLevel)
    {
        super("LogListAppender", null, null, true, null);
        this.expectedLevel = expectedLevel;
    }

    public static LogListAppender getInstance(Level expectedLevel)
    {
        if (logListAppenderInstance == null)
        {
            logListAppenderInstance = new LogListAppender(expectedLevel);
        }

        logListAppenderInstance.expectedLevel = expectedLevel;
        return logListAppenderInstance;
    }

    @Override
    public void append(final LogEvent loggingEvent)
    {

        if (loggingEvent.getLevel() == expectedLevel)
        {
            logMessages.add(loggingEvent);
        }
    }

    /**
     * @return all collected log messages
     */
    public List<LogEvent> getLogMessages()
    {
        return new ArrayList<LogEvent>(logMessages);
    }

    /**
     * Clear the collected log messages list
     */
    public void clear()
    {
        this.logMessages.clear();
    }

}
