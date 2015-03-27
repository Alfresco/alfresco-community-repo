/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.MissingThrowableException;
import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.UnexpectedThrowableException;
import org.junit.Test;

import java.io.IOException;

import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.intercept;
import static org.junit.Assert.*;

/**
 * Unit tests showing usage of {@link ExceptionUtils}.
 *
 * @since 3.0
 * @author Neil Mc Erlean
 */
public class ExceptionUtilsUsageExamplesTest
{
    private String goodMethod() { return "hello"; }

    private String badMethod1()  { throw new RuntimeException("Bad method"); }

    private String badMethod2() { throw new UnsupportedOperationException("Bad method", new RuntimeException("root cause")); }

    @Test public void swallowExpectedExceptions()
    {
        // Calling a local method. (An expression lambda)
        intercept(RuntimeException.class, () -> badMethod1() );

        // Executing a block of code. (Requires return statement)
        intercept(RuntimeException.class, () ->
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
        UnsupportedOperationException e = intercept(UnsupportedOperationException.class, () -> badMethod2() );
        assertEquals(RuntimeException.class, e.getCause().getClass());
    }

    @Test(expected=MissingThrowableException.class)
    public void expectedExceptionNotThrown()
    {
        intercept(IOException.class, () ->
        {
            // Do nothing
            return null;
        });
    }

    @Test(expected=UnexpectedThrowableException.class)
    public void unexpectedExceptionThrown()
    {
        intercept(IOException.class, () ->
        {
            throw new UnsupportedOperationException();
        });
    }

    private void onlySideEffectsHere() { throw new IllegalStateException(); }

    private void onlySideEffectsHere(String s) { throw new IllegalStateException(); }

    // If you use lambdas that return void, then they cannot be lambda expressions. They must be blocks.
    @Test public void usingVoidLambdas()
    {
        intercept(IllegalStateException.class, () -> {
            onlySideEffectsHere();
            return null;
        });

        intercept(IllegalStateException.class, () -> {
            onlySideEffectsHere("hello");
            return null;
        });
    }
}