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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.util.test.junitrules.RetryAtMostRule.RetryAtMost;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for {@link RetryAtMostRule}.
 *
 * @author Domenico Sibilio
 */
@RunWith(MockitoJUnitRunner.class)
public class RetryAtMostRuleTest
{

    private static final String ANNOTATION_WITH_NEGATIVE_VALUE = "annotationRetryAtMostNegativeTimes";
    private static final String ANNOTATION_RETRY_AT_MOST_THRICE = "annotationRetryAtMostThrice";
    private static final AtomicInteger EXECUTION_COUNT = new AtomicInteger(0);
    @Rule
    public RetryAtMostRule retryAtMostRule = new RetryAtMostRule();
    @Rule
    public TestName testNameRule = new TestName();
    @Mock
    private Statement statementMock;

    @Test
    public void testSucceedOnFirstAttempt() throws Throwable
    {
        Description description = Description.createTestDescription(RetryAtMostRuleTest.class.getSimpleName(),
            testNameRule.getMethodName(), getAnnotationByMethodName(ANNOTATION_RETRY_AT_MOST_THRICE));

        Statement statement = retryAtMostRule.apply(statementMock, description);
        statement.evaluate();
        verify(statementMock, times(1)).evaluate();
    }

    @Test
    public void testSucceedOnSecondAttempt() throws Throwable
    {
        doThrow(new AssertionError("First execution should fail")).doNothing().when(statementMock).evaluate();

        Description description = Description.createTestDescription(RetryAtMostRuleTest.class.getSimpleName(),
            testNameRule.getMethodName(), getAnnotationByMethodName(ANNOTATION_RETRY_AT_MOST_THRICE));

        Statement statement = retryAtMostRule.apply(statementMock, description);
        statement.evaluate();
        verify(statementMock, times(2)).evaluate();
    }

    @Test
    @RetryAtMost(3)
    public void testSucceedOnThirdAttempt()
    {
        int currentExecution = EXECUTION_COUNT.incrementAndGet();
        assertSame("This test should be executed 3 times", 3, currentExecution);
    }

    @Test(expected = AssertionError.class)
    public void testFailAfterMaxAttempts() throws Throwable
    {
        doThrow(new AssertionError("All executions should fail")).when(statementMock).evaluate();

        Description description = Description.createTestDescription(RetryAtMostRuleTest.class.getSimpleName(),
            testNameRule.getMethodName(), getAnnotationByMethodName(ANNOTATION_RETRY_AT_MOST_THRICE));

        Statement statement = retryAtMostRule.apply(statementMock, description);
        statement.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRetryAtMostTimes() throws Throwable
    {
        Description description = Description.createTestDescription(RetryAtMostRuleTest.class.getSimpleName(),
            testNameRule.getMethodName(), getAnnotationByMethodName(ANNOTATION_WITH_NEGATIVE_VALUE));

        Statement statement = retryAtMostRule.apply(statementMock, description);
        statement.evaluate();
        verifyNoInteractions(statementMock);
    }

    private Annotation getAnnotationByMethodName(String methodName) throws NoSuchMethodException
    {
        return this.getClass().getMethod(methodName).getAnnotation(RetryAtMost.class);
    }

    @RetryAtMost(-1)
    public void annotationRetryAtMostNegativeTimes()
    {
        // intentionally empty
    }

    @RetryAtMost(3)
    public void annotationRetryAtMostThrice()
    {
        // intentionally empty
    }

}