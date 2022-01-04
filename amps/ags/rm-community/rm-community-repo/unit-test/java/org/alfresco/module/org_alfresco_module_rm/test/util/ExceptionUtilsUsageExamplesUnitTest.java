/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.smuggleCheckedExceptions;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.MissingThrowableException;
import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.SmuggledException;
import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.UnexpectedThrowableException;
import org.junit.Test;

/**
 * Unit tests showing usage of {@link ExceptionUtils}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ExceptionUtilsUsageExamplesUnitTest
{
    private String goodMethod() { return "hello"; }

    private String badMethod1()  { throw new RuntimeException("Bad method"); }

    private String badMethod2() { throw new UnsupportedOperationException("Bad method", new RuntimeException("root cause")); }

    @Test public void swallowExpectedExceptions()
    {
        // Calling a local method. (An expression lambda)
        expectedException(RuntimeException.class, () -> badMethod1() );

        // Executing a block of code. (Requires return statement)
        expectedException(RuntimeException.class, () ->
        {
            for (int i = 0; i < 10; i++) {
                goodMethod();
            }
            // Also works for subtypes of expected exception.
            badMethod2();
            return null;
        });
    }

    @Test public void examineTheExpectedException()
    {
        UnsupportedOperationException e = expectedException(UnsupportedOperationException.class, () -> badMethod2() );
        assertEquals(RuntimeException.class, e.getCause().getClass());
    }

    @Test(expected=MissingThrowableException.class)
    public void expectedExceptionNotThrown()
    {
        expectedException(IOException.class, () ->
        {
            // Do nothing
            return null;
        });
    }

    @Test(expected=UnexpectedThrowableException.class)
    public void unexpectedExceptionThrown()
    {
        expectedException(IOException.class, () ->
        {
            throw new UnsupportedOperationException();
        });
    }

    private void onlySideEffectsHere() { throw new IllegalStateException(); }

    private void onlySideEffectsHere(String s) { throw new IllegalStateException(); }

    // If you use lambdas that return void, then they cannot be lambda expressions. They must be blocks.
    @Test public void usingVoidLambdas()
    {
        expectedException(IllegalStateException.class, () -> {
            onlySideEffectsHere();
            return null;
        });

        expectedException(IllegalStateException.class, () -> {
            onlySideEffectsHere("hello");
            return null;
        });
    }

    // If you use lambdas that throw checked exceptions, the standard Java 8 types are insufficient.
    @Test public void smuggleCheckedExceptionsShouldHideCheckedExceptionsInAnUncheckedException()
    {
        SmuggledException e = expectedException(SmuggledException.class, () -> smuggleCheckedExceptions(() -> methodThrowsException()));

        assertEquals(Exception.class, e.getCheckedException().getClass());
        assertEquals("Checked", e.getCheckedException().getMessage());
    }

    /** This method declares that it throws `java.lang.Exception`. */
    private Object methodThrowsException() throws Exception
    {
        throw new Exception("Checked");
    }
}
