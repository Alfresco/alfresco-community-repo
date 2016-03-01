 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.MissingThrowableException;
import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.UnexpectedThrowableException;
import org.junit.Test;

import java.io.IOException;

import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.junit.Assert.*;

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
}