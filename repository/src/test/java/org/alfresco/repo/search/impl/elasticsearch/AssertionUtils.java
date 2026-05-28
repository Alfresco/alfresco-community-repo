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
package org.alfresco.repo.search.impl.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.alfresco.util.log4j.Log4jAppenderUtil.addAbstractAppenderToLogger;
import static org.alfresco.util.log4j.Log4jAppenderUtil.removeAbstractAppenderFromLogger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configurator;

import org.alfresco.repo.search.impl.elasticsearch.util.LogListAppender;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryParser;

public class AssertionUtils
{
    public static void assertHasIgnoredFields(Runnable func)
    {
        assertEquals(1, countIgnoredFields(func));
    }

    public static long countIgnoredFields(Runnable func)
    {
        LogListAppender logListAppender = LogListAppender.getInstance(Level.WARN);
        Logger logger = LogManager.getLogger(LuceneQueryParser.class);
        Configurator.setLevel(logger, Level.ALL);

        addAbstractAppenderToLogger(logListAppender, logger);

        func.run();

        long ignoredFieldCount = logListAppender.getLogMessages()
                .stream()
                .map(LogEvent::getMessage)
                .filter(message -> message.getFormattedMessage().contains("Ignoring query condition"))
                .count();
        logListAppender.clear();
        removeAbstractAppenderFromLogger(logListAppender, logger);
        return ignoredFieldCount;
    }

    public static void assertThatThrownBy(Runnable func, Class<? extends Exception> exceptionClass)
    {
        try
        {
            func.run();
            fail("Current test should thrown a " + exceptionClass + " exception");
        }
        catch (Exception e)
        {
            assertTrue(exceptionClass + " is not assignable from " + e.getClass(), exceptionClass.isAssignableFrom(e.getClass()));
        }
    }
}
