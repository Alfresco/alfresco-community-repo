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
package org.alfresco.util.test.junitrules;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This JUnit rule can be used to turn existing test code into retryable tests.
 * The test methods marked with the {@link RetryAtMost} annotation will be attempted at most the specified
 * amount of times, stopping at the first successful execution.
 *
 * @author Domenico Sibilio
 */
public class RetryAtMostRule implements TestRule
{
    private static final Log LOG = LogFactory.getLog(RetryAtMostRule.class);

    @Override
    public Statement apply(final Statement statement, final Description description)
    {
        RetryAtMost retryAtMost = description.getAnnotation(RetryAtMost.class);

        if (retryAtMost != null)
        {
            return new RetryAtMostTestStatement(statement, description, retryAtMost.value());
        }

        return statement;
    }

    private static class RetryAtMostTestStatement extends Statement
    {
        private final Statement statement;
        private final Description description;
        private final int retryCount;

        private RetryAtMostTestStatement(Statement statement, Description description, int retryCount)
        {
            this.statement = statement;
            this.description = description;
            this.retryCount = retryCount;
        }

        @Override
        public void evaluate() throws Throwable
        {
            validate();
            for (int i = 0; i < retryCount; i++)
            {
                try
                {
                    LOG.debug(
                        "Retryable testing configured for method: " + description.getMethodName()
                            + " // Attempt #" + (i + 1));
                    statement.evaluate();
                    break; // stop at the first successful execution
                }
                catch (Throwable t)
                {
                    // ignore failed test runs unless it's the last possible execution
                    if (isLastExecution(i))
                    {
                        throw t;
                    }
                }
            }
        }

        private void validate()
        {
            if (retryCount < 1)
            {
                String methodName = description.getMethodName();
                throw new IllegalArgumentException(
                    "Invalid value for @RetryAtMost on method " + methodName + ": " + retryCount + " is less than 1.");
            }
        }

        private boolean isLastExecution(int i)
        {
            return i == retryCount - 1;
        }
    }

    /**
     * This annotation is a marker used to identify a JUnit &#64;{@link Test} method as a retryable test.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RetryAtMost
    {
        /**
         * @return The amount of times a test will be attempted, at most.
         */
        int value() default 1;
    }
}
